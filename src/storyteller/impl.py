from abc import ABC, abstractmethod, abstractproperty
from collections import namedtuple
from dataclasses import dataclass
from datetime import datetime
from enum import Enum, auto, IntEnum
from shelve import Shelf
from typing import Callable, TypeVar

import ZODB as ZODB
import persistent
from BTrees.OOBTree import BTree
from BTrees.IOBTree import BTree as IntObjTree

from storyteller.code_extractor import extract_code
from storyteller.tasktree_api import TreePath
from storyteller.workspace_api import WorkspaceAPI


# class Event(ABC):
#     timestamp: datetime
# class Event: ...
#
# class EventEmitter(ABC):
#     @abstractmethod
#     def emit(self, event: Event): ...
#
# class PersistentEvent(persistent.Persistent):
#     def __init__(self, event: Event):
#         self.persisted = event
#
# class ListeningEventEmitter(EventEmitter):
#     def __init__(self, root, workspace: WorkspaceAPI):
#         self.root = root
#         if
#         self.workspace = workspace
#         self.listeners = [] # (workspace, event) -> None
#
#     def emit(self, event: Event):
#         if self.db.events is None
#         self.db.events

# class Run: ... #todo

class ExecutionContext(ABC):
    @abstractmethod
    #todo foo is (P... -> V) and this method returns V, while params default to values of P... as named tuple
    def root(self, foo: Callable, name: str, params: namedtuple, code): ...

    def sequence(*args, **kwargs):
        ...

    def leaf(*args, **kwargs):
        ...

    def on_spawned(self, run: Run): ...

    def on_finished(self, run: Run): ...

class Session: pass #todo

@dataclass
class StorytellerRuntime:
    db_root: object
    workspace: WorkspaceAPI
    session: Session

    def get_collection(self, name):
        while True:
            try:
                return self.db_root[name]
            except AttributeError:
                self.db_root[name] = BTree()

TaskCoordinates = namedtuple("TaskCoordinates", ["position", "name"])
ExecutionCoordinates = namedtuple("ExecutionCoordinates", ["task_coordinates", "params"])

class AuditEvent:
    def __init__(self):
        self.timestamp = datetime.utcnow()

class StartedAt(AuditEvent):
    def __init__(self, commit: str):
        super().__init__()
        self.commit = commit

# when we follow a main revision and suddenly theres a mismatch, we start a new dedicated revision with same trail, and we add this to the trail
class RevisedFrom(AuditEvent):
    def __init__(self, prev_revision_number: int):
        super().__init__()
        self.prev_revision_number = prev_revision_number

class Spawned(AuditEvent):
    def __init__(self, subexecution_coordinates: ExecutionCoordinates):
        super().__init__()
        self.subexecution_coordinates = subexecution_coordinates


class Merged(AuditEvent):
    def __init__(self, subexecution_coordinates: ExecutionCoordinates, revision_number: int, merged_commit: str):
        super().__init__()
        self.subexecution_coordinates = subexecution_coordinates
        self.revision_number = revision_number,
        self.merged_commit = merged_commit

class Interrupted(AuditEvent):
    def __init__(self, exception: Exception):
        super().__init__()
        self.exception = exception

class InterruptionBubbled(AuditEvent):
    def __init__(self, bubbled_from: ExecutionCoordinates):
        super().__init__()
        self.bubbled_from = bubbled_from

class Finished(AuditEvent):
    def __init__(self, result):
        super().__init__()
        self.result = result


class RunMode(IntEnum):
    RESOLVING_CANDIDATES = auto()
    FOLLOWING_MAIN = auto()
    REVISING = auto()
    INTERRUPTED = auto()
    INTERRUPTION_BUBBLED = auto()
    FINISHED = auto()

class Run(persistent.Persistent):
    def __init__(self, number: int, coordinates: ExecutionCoordinates, start_from: str, version: int, candidate_revisions: [int], trail: [AuditEvent]=[]):
        self.number = number
        self.coordinates = coordinates
        self.start_from = start_from
        self.current_commit = start_from
        self.version = version
        self.mode = None
        self.main_revision = None
        # following setter takes care of mode and main_revision
        self.candidate_revisions = candidate_revisions
        self.trail = tuple(trail)

    @property
    def result(self):
        assert self.mode == RunMode.FINISHED
        return self.trail[-1].result

    @property
    def exception(self):
        assert self.mode in (RunMode.INTERRUPTED, RunMode.INTERRUPTION_BUBBLED)
        return self.trail[-1].exception

    @property
    def candidate_revisions(self):
        return self._candidate_revisions

    @candidate_revisions.setter
    def candidate_revisions(self, val: [int]):
        self._candidate_revisions = tuple(val or [])
        if self._candidate_revisions:
            if len(self._candidate_revisions) == 1: # todo and that revision is not finished
                self.main_revision = self._candidate_revisions[0]
                self.mode = RunMode.FOLLOWING_MAIN
            else:
                self.main_revision = None
                self.mode = RunMode.RESOLVING_CANDIDATES

        else:
            self.main_revision = None
            self.create_dedicated_revision() # todo it should set main_revision
            self.mode = RunMode.REVISING

    def _create_dedicated_revision(self): ... #todo


class Version(persistent.Persistent):
    def __init__(self, number: int, coordinates: TaskCoordinates, source: str, extracted: bool=True):
        self.number = number
        self.coordinates = coordinates
        self.source = source
        self.extracted = extracted

def next_id(tree: IntObjTree) -> int:
    if tree:
        return tree.maxKey()+1
    return 0


class RevisionState(IntEnum):
    PENDING = auto()
    UNFINISHED = auto()
    INTERRUPTED = auto()
    INTERRUPTION_BUBBLED = auto()
    FINISHED = auto()

class Revision(persistent.Persistent):
    def __init__(self, number: int, coordinates: ExecutionCoordinates, started_from: str, trail: [AuditEvent]=[]):
        self.number = number
        self.coordinates = coordinates
        self.started_from = started_from
        self.trail = trail

    @property
    def trail(self):
        return self._trail

    @trail.setter
    def trail(self, val: [AuditEvent]):
        self._trail = tuple(val)
        assert self._trail and isinstance(self._trail[0], StartedAt) and self._trail[0].started_from == self.started_from
        if len(self._trail) == 1:
            self.state = RevisionState.PENDING
        elif isinstance(self._trail[-1], Finished):
            self.state = RevisionState.FINISHED
        elif isinstance(self._trail[-1], Interrupted):
            self.state = RevisionState.INTERRUPTED
        elif isinstance(self._trail[-1], InterruptionBubbled):
            self.state = RevisionState.INTERRUPTION_BUBBLED
        else:
            self.state = RevisionState.UNFINISHED

    @property
    def closed(self) -> bool:
        return self.state in { RevisionState.FINISHED, RevisionState.INTERRUPTED, RevisionState.INTERRUPTION_BUBBLED };

class TaskData(persistent.Persistent):

    def __init__(self, coordinates: TaskCoordinates, versions: IntObjTree=None, revisions: IntObjTree=None, runs: IntObjTree=None):
        assert coordinates.position is not None
        assert coordinates.name is not None
        self.coordinates = coordinates
        self.versions = versions or IntObjTree()
        self.revisions = revisions or IntObjTree()
        self.runs = runs or IntObjTree()

    def make_version(self, source: str, extracted: bool=True) -> Version:
        matching = list(filter(lambda x: x.source == source, self.versions.values()))
        if matching:
            assert len(matching) == 1
            return matching[0]
        new_version = Version(next_id(self.versions), self.coordinates, source, extracted)
        self.versions[new_version.number] = new_version
        # todo persist?

    def make_run(self, start_from: str, version: int, params: dict) -> Run:
        exec_coord = ExecutionCoordinates(self.coordinates, tuple(params.items()))
        return Run(
            next_id(self.runs),
            exec_coord,
            start_from,
            version,
            [
                i
                for i, k in self.revisions.items()
                if k.coordinates == exec_coord
                and k.start_from == start_from
            ]
        )

TaskView = TypeVar("TaskView")

@dataclass(frozen=False)
class VersionView:
    versionNo: int
    source: str
    extracted: bool
    task: TaskView

@dataclass(frozen=False)
class RevisionView:
    revisionNo: int
    started_from: str
    trail: [AuditEvent]
    task: TaskView

@dataclass(frozen=False)
class RunView:
    runNo: int
    started_from: str
    started_from: str
    current_commit: str
    version: VersionView
    mode: RunMode
    main_revision: RevisionView
    candidate_revisions: [ RevisionView ]
    trail: [AuditEvent]
    task: TaskView

@dataclass
class TaskView:
    coordinates: TaskCoordinates
    versions: [ VersionView ]
    revisions: [ RevisionView ]
    runs: [ RunView ]

class NotFinishedExcetion(BaseException): ... #todo?
class WrongOutcomeException(BaseException): ... #todo?

class SimpleFuture(ABC):
    def join(self) -> object:
        """Returns self.result or throws self.exception; doesn't return until self.finished is True"""

    @abstractmethod
    @property
    def finished(self) -> bool: ...

    @abstractmethod
    @property
    def succesful(self) -> bool:
        """May throw NotFinishedException"""

    @abstractmethod
    @property
    def result(self) -> object:
        """May throw NotFinishedException or WrongOutcomeException"""

    @abstractmethod
    @property
    def exception(self) -> Exception:
        """May throw NotFinishedException or WrongOutcomeException"""

class SimpleExecutor(ABC):
    @abstractmethod
    def submit(self, body: Callable) -> SimpleFuture: ...

class HereAndNowFuture(SimpleFuture):
    def __init__(self, success: bool, result_or_exception):
        self._success = success
        if success:
            self._result = result_or_exception
        else:
            self._exception = result_or_exception

    @property
    def succesful(self) -> bool or None:
        return self._success

    @property
    def finished(self) -> bool:
        return True

    @property
    def result(self) -> object:
        if self._success:
            return self._result
        raise WrongOutcomeException()

    @property
    def exception(self) -> Exception:
        if not self._success:
            return self._exception
        raise WrongOutcomeException()


class HereAndNowExecutor(SimpleExecutor):
    def submit(self, body: Callable) -> SimpleFuture:


class RunHandle:
    def __init__(self, future: SimpleFuture, coordinates: ExecutionCoordinates, number: int):
        self.future = future
        self.coordinates = coordinates
        self.number = number

    def _get_run(self):
        global_runtime.db_root["tree"]

def to_view(task: TaskData) -> TaskView:
    out = TaskView(
        task.coordinates,
        [ None ] * len(task.versions),
        [ None ] * len(task.revisions),
        [ None ] * len(task.runs)
    )
    for i, v in task.versions.items():
        assert i == v.number
        out.versions[i] = VersionView(
            i,
            v.source,
            v.extracted,
            out
        )
    for i, r in task.revisions.items():
        assert i == r.number
        out.revisions[i] = RevisionView(
            i,
            r.started_from,
            list(r.trail),
            out
        )
    for i, r in task.runs.items():
        assert i == r.number
        out.runs[i] = RunView(
            i,
            r.started_from,
            r.current_commit,
            out.versions[r.version.number],
            r.mode,
            out.revisions[r.main_revision.number] if r.main_revision else None,
            [ out.revisions[x.number] for x in r.candidate_revisions ],
            list(r.trail),
            out
        )



def code_str(o) -> str:
    """
    Takes an object, tries to invoke it.__code__str__() and falls back to str(it).
    It's a way of representing code with an arbitrary object - you have dedicated dunder for that, but
    any object (usually, a string) can be treated as such as well.
    """
    val = None
    try:
        val = o.__code_str__()
    except AttributeError:
        val = str(o)
    lines = val.splitlines()
    first_unstripped = lines[0]
    first_stripped = first_unstripped.lstrip()
    diff = len(first_unstripped) - len(first_stripped)
    # this part unindents the code; only if first line is the leftmost; works only with spaces, but there's no
    # limitation on their number (as long as there is a common prefix of spaces between the lines)
    if diff:
        prefix = " "*diff
        if all(x.startswith(prefix) for x in lines):
            lines = [ x[diff:] for x in lines]
    return "\n".join(lines)

# def view(t: type[persistent.Persistent], name: str=None, indexed: bool=False) -> type:
#     name = name or t.__name__
#     def __type__code():
#         varnames = list(t.__init__.__code__.co_varnames[1:]) # 1: to skip "self"
#         annotated_params_with_defaults = []
#         # init_lines = []
#         of_params = []
#
#         if indexed:
#             annotated_params_with_defaults += [ ("i", "int", None) ]
#             # init_lines += [ "self.i = i" ]
#             of_params += [ "inst.i" ]
#         #fixme works only with positional! but otoh is expected to work with my simple persistence classes
#         vars_without_defaults = t.__init__.__code__.co_argcount - len(t.__init__.__defaults__)
#         for i, varname in enumerate(varnames):
#             ann = None
#             if varname in t.__init__.__annotations__:
#                 ann = t.__init__.__annotations__[varname].__name__
#             def_ = None
#             if i - vars_without_defaults >=0:
#                 def_ = str(t.__init__.__defaults__[i-vars_without_defaults])
#             annotated_params_with_defaults += [ (varname, ann, def_) ]
#             # init_lines += [ "self."+varname+" = "+varnames ]
#             of_params += [ "inst."+varname ]
#
#         def apwd_to_str(apwd):
#             out = apwd[0]
#             if apwd[1]:
#                 out+= ": "+apwd[1]
#             if apwd[2]:
#                 out+= "="+apwd[1]
#             return out
#
#         annotated_params_with_defaults = [ apwd_to_str(x) for x in annotated_params_with_defaults ]
#
#
#
#         # lines = ["from dataclasses import dataclass"]
#         lines = ["@dataclass"]
#         lines += [ "class "+name+"View:" ]
#         for l in annotated_params_with_defaults:
#             lines += [ "    "+l ]
#         lines += [ "    " ]
#         lines += [ "    @staticmethod" ]
#         lines += [ "    def of(inst: "+t.__name__+"):" ]
#         lines += [ "        return "+t.__name__+"("+(", ".join(of_params))+")" ]
#         lines += [ "" ]
#         lines += [ "result = dataclasses.dataclass("+name+"View)" ]
#         lines += [ "result = "+name+"View" ]
#
#         return "\n".join(lines)
#         # return "\n".join("    "+l for l in lines)
#
#     result = None
#     to_eval = __type__code()
#     from dataclasses import dataclass
#     print(to_eval)
#     exec(to_eval)
#     return result
#
# TaskView = view(TaskData, "Task")

    #
    #
    #
    # @wraps(t.__init__)
    # def __init__(self, *args):
    #     for i, varname in enumerate(t.__init__.__code__.co_varnames, start=1):
    #         setattr(self, varname, args[i-1])
    #
    # __init__.__qualname__ = t.__module__+name+"View.__init__"
    #
    # def of(inst: t):
    #     return result(*tuple(getattr(inst, varname) for varname in t.__init__.__code__.co_varnames[1:]))
    #
    # of.__qualname__ = t.__module__+name+"View.of"
    #
    # result = type(
    #     name+"View",
    #     tuple(object, ),
    #     {
    #         "__init__": __init__,
    #         "of": of
    #     }
    # )
    # return result

class WrapperException(Exception):
    def __init__(self, wrapped: Exception, origin_exec_coordinates: ExecutionCoordinates):
        self.wrapped = wrapped
        self.origin_exec_coordinates = origin_exec_coordinates

class StorytellerData:
    @staticmethod
    def ensure_task_structure(coordinates: TaskCoordinates):
        encoded = coordinates.position.encode()
        tree = global_runtime.get_collection("tree")
        if coordinates.position not in tree:
            tree[encoded] = BTree()
        position_scope = tree[encoded]
        name = coordinates.name
        if name not in position_scope:
            position_scope[name] = TaskData(coordinates)
            tree[encoded] = position_scope

    @staticmethod
    def get_task(coordinates: TaskCoordinates) -> TaskData:
        return global_runtime.get_collection("tree")[coordinates.position.encode()][coordinates.name]

    @staticmethod
    def _get_numbered(coordinates: TaskCoordinates, number: int, collection_getter: Callable):
        return collection_getter(StorytellerData.get_task(coordinates))[number]

    @staticmethod
    def get_run(coordinates: TaskCoordinates, number: int):
        return StorytellerData._get_numbered(coordinates, number, lambda td: td.runs)

class BaseExecutionContext(ExecutionContext):
    parent: ExecutionContext = None

    # type is included in position, that's why it's not present in signature
    def _prepare_task_data_and_run(self, foo: Callable, params: dict, position: TreePath, name: str=None, code: str=None):
        # encoded = position.encode()
        # tree = global_runtime.get_collection("tree")
        # if encoded not in tree:
        #     tree[encoded] = BTree()
        # position_scope = tree[encoded]
        # name = name or foo.__name__
        # if name not in position_scope:
        #     position_scope[name] = TaskData(TaskCoordinates(position, name))
        #     tree[encoded] = position_scope
        coordinates = TaskCoordinates(position, name)
        StorytellerData.ensure_task_structure(coordinates)
        task_data = StorytellerData.get_task(coordinates)
        source = code_str(code) or extract_code(foo)
        version = task_data.make_version(source, code is None)
        run = task_data.make_run(global_runtime.workspace.current_commit, version.number, params)
        return (task_data, run)

    def _signal_on_spawned(self):
        if self.parent:
            self.parent.on_spawned(self)

    def _signal_on_finished(self):
        if self.parent:
            self.parent.on_finished(self)

    def _execute_task(self, foo: Callable, params: dict, position: TreePath, name: str=None, code: str=None):
        (task_data, run) = self._prepare_task_data_and_run(foo, params, position, name, code)
        try:
            global current_execution_context
            global_runtime.workspace.branch_out(self._branch_name(run))
            current_execution_context = self._make_handler(position)
            result = self._execute_body(foo, params)
            run.emit(Finished(result)) #todo
            run.mode = RunMode.FINISHED
        except WrapperException as e:
            run.emit(InterruptionBubbled(e.origin_exec_coordinates))
            run.mode = RunMode.INTERRUPTION_BUBBLED
        except Exception as e:
            run.emit(Interrupted(e))
            run.mode = RunMode.INTERRUPTED
        finally:
            run.current_commit = global_runtime.workspace.current_commit
            current_execution_context = self

    #todo need something like "make handle" and handle would have view on the run, and commands like join and merge


class NoExecutionContext(ExecutionContext):
    def root(self, foo: Callable, name: str, params: namedtuple, code: str=None):
        position = TreePath.root()
        run = self._prepare_run(foo, name, params, code)

current_execution_context = NoExecutionContext()
global_runtime = StorytellerRuntime() # todo
current_session = Session()

def root(*args, **kwargs):
    current_execution_context.root(*args, **kwargs)

def sequence(*args, **kwargs):
    current_execution_context.root(*args, **kwargs)

def leaf(*args, **kwargs):
    current_execution_context.root(*args, **kwargs)

