import dataclasses
from collections import defaultdict
from enum import IntEnum, auto
from functools import wraps
from types import new_class
from typing import TypeVar, Generic, Callable, get_type_hints, NamedTuple
# from typing import TypeVar, Generic, Callable, Self, get_type_hints, NamedTuple


class InterceptedProperty:
    def __set_name__(self, owner, name):
        self.name = name

    def __get__(self, obj, objtype=None):
        interceptor = obj.__interceptor__
        return interceptor.around_get(obj, self.name, lambda x: object.__getattribute__(x.__data__, self.name))

    def __set__(self, obj, value):
        interceptor = obj.__interceptor__
        return interceptor.around_set(obj, self.name, value, lambda x, v: object.__setattr__(x.__data__, self.name, v))

    def __delete__(self, instance):
        raise TypeError("Intercepted properties does not support deletion!")

def intercepted_method(m: Callable[["Self", ...], object]) -> Callable[["Self", ...], object]:
    @wraps(m)
    def wrapper(self, *args, **kwargs):
        data = self.__data__
        interceptor = self.__interceptor__
        return interceptor.around_invoke(self, m.__name__, (args, kwargs), m)
    return wrapper


T = TypeVar("T")
V = TypeVar("V")
U = TypeVar("U")

class Interceptor(Generic[T]):
    def around_get(self, target: T, name: str, wrapped: Callable[[T], V]) -> V:
        return wrapped(target)

    def around_set(self, target: T, name: str, value: V, wrapped: Callable[[T, V], U]) -> U:
        return wrapped(target, value)

    def around_invoke(self, target: T, name: str, params: (tuple, dict), wrapped: Callable[[T, ...], V]) -> V:
        return wrapped(target, *params[0], **params[1])

class ActionType(IntEnum):
    GET = auto()
    SET = auto()
    INVOKE = auto()

class Action(Generic[T]):
    def around(self, target: T, name: str, action: ActionType, params: None | object | tuple[tuple, dict], wrapped: Callable[[T, ...], V]) -> V:
        return {
            ActionType.GET: lambda : wrapped(target),
            ActionType.SET: lambda : wrapped(target, params),
            ActionType.INVOKE: lambda : wrapped(target, *params[0], **params[1])
        }[action]()

class ActionWrapper(Action[T]):
    def __init__(self, delegate):
        self.delegate  = delegate

    def around(self, *args, **kwargs):
        return self.delegate(*args, **kwargs)

def action(anything):
    if isinstance(anything, Action):
        return anything
    return ActionWrapper(anything)

class DispatchingInterceptor(Interceptor):
    def __init__(self):
        self.data: dict[str, dict[ActionType, Action]] = defaultdict(dict)

    def register(self, delegate: Interceptor) -> "closure on(name: str, Action...=ANY)":
        class Closure:
            def on(cs, name: str, *actions: ActionType):
                if not actions:
                    actions = (ActionType.GET, ActionType.SET, ActionType.INVOKE)
                for a in actions:
                    self.data[name][a] = delegate
        return Closure()

    def _generic_around(self, target: T, name: str, action: ActionType, params, wrapped: Callable):
        delegate = self.data[name].get(action) # method returns None, [] raises
        if delegate:
            return delegate.around(target, name, action, params, wrapped)
        return wrapped(target)

    def around_get(self, target: T, name: str, wrapped: Callable[[T], V]) -> V:
        return self._generic_around(target, name, ActionType.GET, tuple(), wrapped)

    def around_set(self, target: T, name: str, value: V, wrapped: Callable[[T, V], U]) -> U:
        return self._generic_around(target, name, ActionType.SET, tuple(value, ), wrapped)

    def around_invoke(self, target: T, name: str, params: (tuple, dict), wrapped: Callable[[T, ...], V]) -> V:
        return self._generic_around(target, name, ActionType.INVOKE, params, wrapped)


def make_intercepted(t: type, customize: Callable[[], dict[str, object]]=None, initialize: Callable=None, bases: tuple[type]=()) -> type:
    dataclass_type = dataclasses.dataclass(t)
    @wraps(dataclass_type.__init__)
    def __init__(self, *args, **kwargs):
        self.__data__ = dataclass_type(*args, **kwargs)
        self.__interceptor__ = DispatchingInterceptor()
        self.__initialize__()

    __initialize__ = initialize

    if not __initialize__:
        def __initialize__(self): ...
        __initialize__ = __initialize__

    def __str__(self):
        return t.__name__+"[data: "+str(self.__data__)+", interceptor: "+str(self.__interceptor__)+"]"

    def __repr__(self):
        return t.__name__+"[data: "+repr(self.__data__)+", interceptor: "+repr(self.__interceptor__)+"]"

    def __eq__(self, other):
        return unintercept(self) == unintercept(other)

    def setup(cls):
        original_cls = dict(cls)
        cls["__init__"] = __init__
        cls["__str__"] = __str__
        cls["__repr__"] = __repr__
        cls["__eq__"] = __eq__
        type_hints = get_type_hints(t)
        for k, v in type_hints.items():
            cls[k] = InterceptedProperty()
            # setattr(cls, k, InterceptedProperty())
        methods = []
        for k, m in original_cls.items():
            if not k.startswith("_") and not k in type_hints:
                # m = getattr(t, k)
                if isinstance(m, Callable): #todo and not is property
                    # setattr(cls, k, intercepted_method(m))
                    cls[k] = intercepted_method(m)
                    methods.append(k)
        cls["__intercepted_properties__"] = type_hints
        cls["__intercepted_methods__"] = methods

        if customize:
            for k, v in customize(dict(cls)).items():
                if k not in cls:
                    cls[k] = v

        if "__initialize__" not in cls:
            cls["__initialize__"] = __initialize__

    intercepted_type = new_class(
        t.__name__,
        (t, ) + bases,
        {},
        setup
    )
    return intercepted_type

def is_intercepted(o: object or type) -> bool:
    t = o if isinstance(o, type) else type(o)
    try:
        return t.__intercepted_type__
    except AttributeError:
        return False

def unintercept(o: object) -> object:
    if is_intercepted(o):
        return o.__data__
    return o

class TraceType(IntEnum):
    BEFORE = auto()
    AFTER = auto()
    RAISED = auto()

class TraceEntry(NamedTuple):
    type_name: str
    action_type: ActionType
    target_name: str
    params: object
    entry_type: TraceType
    payload: object

class TraceCollectingInterceptor(Interceptor[T]):
    def __init__(self):
        self.data = []

    def _entry(self, target: T, action: ActionType, name: str, params, entry_type: TraceType, payload):
        self.data.append(TraceEntry(type(target).__name__, action, name, params, entry_type, payload))

    def _around(self, target: T, action: ActionType, name: str, params, delegate):
        try:
            self._entry(target, action, name, params, TraceType.BEFORE, None)
            out = delegate()
            self._entry(target, action, name, params, TraceType.AFTER, out)
            return out
        except Exception as e:
            self._entry(target, action, name, params, TraceType.RAISED, e)
            raise

    def around_get(self, target: T, name: str, wrapped: Callable[[T], V]) -> V:
        return self._around(target, ActionType.GET, name, tuple(), lambda : wrapped(target))

    def around_set(self, target: T, name: str, value: V, wrapped: Callable[[T, V], U]) -> U:
        return self._around(target, ActionType.SET, name, (value, ), lambda : wrapped(target, value))

    def around_invoke(self, target: T, name: str, params: (tuple, dict), wrapped: Callable[[T, ...], V]) -> V:
        return self._around(target, ActionType.INVOKE, name, params, lambda : wrapped(target, *params[0], **params[1]))

def __test__():
    @make_intercepted #todo rethink name
    class X:
        x: int
        y: str

        def foo(self, bar: bool):
            return str(self.x if bar else self.x * 2)+":"+self.y

    i = TraceCollectingInterceptor()
    x = X(2, "foo")
    x.__interceptor__ = i
    x.x = 3
    assert x.foo(False) == "6:foo"
    assert i.data == [
        TraceEntry(type_name='X', action_type=ActionType.SET, target_name='x', params=(3,), entry_type=TraceType.BEFORE, payload=None),
        TraceEntry(type_name='X', action_type=ActionType.SET, target_name='x', params=(3,), entry_type=TraceType.AFTER, payload=None),
        TraceEntry(type_name='X', action_type=ActionType.GET, target_name='x', params=(), entry_type=TraceType.BEFORE, payload=None),
        TraceEntry(type_name='X', action_type=ActionType.GET, target_name='x', params=(), entry_type=TraceType.AFTER, payload=3),
        TraceEntry(type_name='X', action_type=ActionType.GET, target_name='y', params=(), entry_type=TraceType.BEFORE, payload=None),
        TraceEntry(type_name='X', action_type=ActionType.GET, target_name='y', params=(), entry_type=TraceType.AFTER, payload='foo'),
    ]

if __name__ == "__main__":
    __test__()
