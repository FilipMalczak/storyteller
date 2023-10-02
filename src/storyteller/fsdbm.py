import json
import os.path
import uuid
from abc import ABC, abstractmethod
from dataclasses import asdict
from enum import IntEnum, auto
from functools import wraps
from os import makedirs, listdir
from os.path import exists, isfile, isdir, join, abspath
from pathlib import Path
from shutil import move, rmtree
from types import NoneType, GenericAlias, TracebackType
from typing import Callable, NamedTuple, TypeVar, Generator, get_type_hints, Protocol, Any

from storyteller.intercepted import make_intercepted, Interceptor


class JSONModeller(ABC):
    @abstractmethod
    def can_handle(self, t: type) -> bool: ...

    @abstractmethod
    def to_json_model(self, o: "instance of t that self.can_handle(t)") -> "dict, list, primitive; simple enough for JSON": ...

    @abstractmethod
    def from_json_model(self, m: "parsed JSON; dict, list, primitive", t: type or GenericAlias) -> "instance of t": ...

class SerDes(ABC):
    @abstractmethod
    def can_handle(self, t: type) -> bool: ...

    @abstractmethod
    def to_str(self, o: "instance of t that self.can_handle(t)") -> str: ...

    @abstractmethod
    def from_str(self, m: str, t: type or GenericAlias) -> "instance of t": ...

global_modelers = []
global_serdes = []
global_types = {}

def register_modeler(modeler: JSONModeller) -> JSONModeller:
    global_modelers.insert(0, modeler)
    return modeler

def register_serdes(serdes: SerDes) -> SerDes:
    global_serdes.insert(0, serdes)
    return serdes

primitive_types = [ NoneType, str, int, bool, float ]
collection_types = [ set, list, tuple, dict ]

def _simple(t: type or GenericAlias) -> type:
    try:
        return t.__origin__
    except AttributeError:
        assert isinstance(t, type)
        return t

def _issubclass(sub: type or GenericAlias, par: type or GenericAlias) -> bool:
    return issubclass(_simple(sub), _simple(par))

@register_modeler
class ObjectModeller(JSONModeller):
    def can_handle(self, t: type) -> bool:
        return True

    def to_json_model(self, o: "instance of t that self.can_handle(t)") -> "dict, list, primitive; simple enough for JSON":
        try:
            return o.__json__
        except AttributeError:
            try:
                return asdict(o) # for dataclasses
            except TypeError: #not a dataclass
                try:
                    return o._asdict() # for namedtuples
                except AttributeError: #not a namedtuple
                    return o # hope for the best

@register_modeler
class BuiltinsModeler(JSONModeller):
    def can_handle(self, t: type) -> bool:
        return any(_issubclass(t, x) for x in primitive_types + collection_types)

    @abstractmethod
    def to_json_model(self, o: "instance of t that self.can_handle(t)") -> "dict, list, primitive; simple enough for JSON":
        if any(isinstance(o, t) for t in primitive_types):
            return o
        if any(isinstance(o, t) for t in collection_types):
            if isinstance(o, dict):
                return { to_json_model(k): to_json_model(v) for k, v in o.items() }
            else:
                return type(o)(map(to_json_model, o))
        assert False

    @abstractmethod
    def from_json_model(self, m: "parsed JSON; dict, list, primitive", t: type or GenericAlias) -> "instance of t that self.can_handle(t)":
        if any(_issubclass(t, x) for x in primitive_types):
            return m
        if any(_issubclass(t, x) for x in collection_types):
            if _issubclass(t, dict):
                (kt, vt) = t.__args__
                return { from_json_model(k, kt): from_json_model(v, vt) for k, v in m.items() }
            else:
                return _simple(t)(map(from_json_model, m))
        assert False

def to_json_model(o: Any) -> "dict, list, primitive, etc":
    for modeler in global_modelers:
        if modeler.can_handle(type(o)):
            return modeler.to_json_model(o)
    assert False

def from_json_model(m: "dict, list, primitive, etc", t: type or GenericAlias) -> Any:
    for modeler in global_modelers:
        if modeler.can_handle(t):
            return modeler.from_json_model(m, t)
    assert False

def to_str(o: Any) -> str:
    for serdes in global_serdes:
        if serdes.can_handle(type(o)):
            return serdes.to_str(o)
    assert False

def from_str(s: str, t: type or GenericAlias) -> "instance of t":
    for serdes in global_serdes:
        if serdes.can_handle(t):
            return serdes.from_str(s, t)
    assert False

def _makedir(*path: tuple(str or Path)) -> Path:
    p = Path(*path)
    if not exists(p):
        makedirs(p)
    else:
        assert isdir(p)
    return p

class NodeType(IntEnum):
    ENTITY = auto()
    LIST = auto()
    SET = auto()
    MAPPING = auto()

_FsDBM = TypeVar("FsDBM")

class NodeMetadata(NamedTuple):
    path: Path # relative to directory holding root (root has non-empty path)
    #todo logical_path: str
    node_type: NodeType
    db: _FsDBM
    custom: dict[str, Any]

_Node = TypeVar("Node")

class GCMode(IntEnum):
    """When deleting a node (moving it to trash) how to behave if the trash location is already occupied."""
    OVERWRITE = auto()
    RAISE = auto()
    IGNORE = auto()

class Node:
    def __init__(self, path: Path, db: _FsDBM, node_type: NodeType=None):
        node_dir = Path(db.data, path)
        meta_path = join(node_dir, "meta.json")
        meta = None
        if exists(meta_path):
            with open(meta_path) as f:
                loaded = json.load(f)
            assert node_type is None or loaded["node_type"] == node_type
            meta = NodeMetadata(
                path,
                loaded["node_type"],
                db,
                loaded["custom"]
            )
            assert self._accepts_custom_data(meta.custom)
        else:
            _makedir(node_dir)
            meta = NodeMetadata(
                path,
                node_type,
                db,
                self._customize_new_metadata()
            )
            with open(meta_path, "w") as f:
                json.dump({"node_type": meta.node_type, "custom": meta.custom}, f)
        self._meta = meta
        self._payload = join(self._meta.path, "payload.json")
        self._tree = join(self._meta.path, "tree.json")

    # hook for extending todo seems to not be useful
    def _customize_new_metadata(self) -> dict[str, "json model"]:
        return {}

    def _accepts_custom_data(self, custom: dict[str, "json model"]) -> bool:
        return True

    @property
    def metadata(self) -> NodeMetadata:
        return self._meta

    @property
    def path(self) -> Path:
        '''Relative to parent of root (root has non-empty path); when interpreted against FS represents a directory'''
        return self.metadata.path

    @@property
    def name(self) -> str:
        return os.path.basename(self.path)

    @property
    def json_payload(self) -> "json primitive":
        if not os.path.exists(self._payload):
            return None
        with open(self._payload) as f:
            return json.load(f)

    @json_payload.setter
    def json_payload(self, p: "json primitive"):
        with open(self._payload, "w") as f:
            json.dump(p, f)
    @property
    def tree_payload(self) -> "json primitive; dict or list tbp":
        if not os.path.exists(self._tree):
            return None
        with open(self._tree) as f:
            return json.load(f)

    @tree_payload.setter
    def tree_payload(self, p: "json primitive"):
        with open(self._tree, "w") as f:
            json.dump(p, f)

    @property
    def child_nodes(self) -> dict[str, _Node]:
        if self.metadata.node_type == NodeType.MAPPING:
            return self.tree_payload
        return {x: x for x in self.tree_payload}

    @property
    def retrievable_nodes(self) -> dict[str, _Node]:
        node_dir = os.path.join(self.metadata.db.data, self.path)
        out = {}
        for child in listdir(node_dir):
            subnode_candidate = os.path.join(node_dir, child)
            subnode_meta = os.path.join(subnode_candidate, "meta.json") #todo move filenames to constants
            if os.path.exists(subnode_meta) and os.path.isfile(subnode_meta):
                out[child] = Node(subnode_candidate, self.metadata.db)
        return out

    @property
    def orphaned_nodes(self) -> dict[str, _Node]:
        c = self.child_nodes
        r = self.retrievable_nodes
        return {k: v for k, v in r.items() if any(x.node_dir == v.node_dir for x in c.values())}

    def gc(self, mode: GCMode = GCMode.OVERWRITE):
        for k, v in self.orphaned_nodes:
            trash_path = join(self.metadata.db.trash, v.metadata.path)
            if exists(trash_path):
                if mode == GCMode.OVERWRITE:
                    rmtree(trash_path)
                    #todo maybe to trashpath parent?
                    move(v.metadata.path, trash_path)
                elif mode == GCMode.RAISE:
                    assert False # todo dedicated exc
                elif mode == GCMode.IGNORE:
                    pass
                else:
                    assert False


def is_fsentity_type(t: type or GenericAlias):
    if issubclass(_simple(t), (list, set)):
        return is_fsentity_type(t.__args__[0])
    if issubclass(_simple(t), dict):
        if is_fsentity_type(t.__args__[1]):
            return t.__args__[0] == str
        return False
    try:
        return _simple(t).__fsentity__
    except AttributeError:
        return False

# #todo can properties be replaced with fields? read about protocols
class FSEntity(Protocol):
    @property
    def __node__(self) -> Node: ...

    @__node__.setter
    def __node__(self, var: Node): ...

    @property
    def __payload__(self) -> "json model": ...

    @property
    def __subnodes__(self) -> "json model": ...
#
#     def __on_node_loaded__(self, new_entity: "FSEntity"): ...

# class _BaseFSEntity:
#     __node_field__: Node = None
#     __node_load_callback__: Callable[[FSEntity], None] = None
#
#     def __on_node_loaded__(self, new_entity: "FSEntity; newly created entity instance for a subnode"):
#         if self.__node_load_callback__ is not None:
#             self.__node_load_callback__(new_entity)


def _fs_collection(base):
    '''
    Adds everything needed to satisfy FSEntity protocol but __subnodes__. Payload is empty for collections.
    '''
    def __init__(self, t: "usually type, sometimes tuple, at discretion of subclass", callback: Callable[["t"], None]):
        super(base).__init__(self)
        self.__type_info__ = t
        self.__node_field__ = None
        self.__on_node_loaded__ = callback

    @property
    def __node__(self) -> Node:
        return self.__node_field__

    @__node__.setter
    def __node__(self, var: Node):
        self.__on_node_set__(var)
        self.__node_field__ = var

    def __on_node_set__(self, var: Node): ...

    @property
    def __payload__(self) -> None:
        return None

    out = type(
        "FSCollection_of_"+base.__name__,
        tuple(),
        # tuple(_BaseFSEntity, ),
        {
            "__init__": __init__,
            "__payload__": __payload__,
            "__node__": __node__,
            "__on_node_set__": __on_node_set__,
        }
    )
    out.__doc__ = """A mixin for implementation details shared across FS collection types"""
    return out

def node_type(t: type or GenericAlias):
    #assumes is_fsentity_type(t)
    s = _simple(t)
    if issubclass(s, list):
        return NodeType.LIST
    elif issubclass(s, set):
        return NodeType.SET
    elif issubclass(s, dict):
        return NodeType.MAPPING
    else:
        return NodeType.ENTITY

class FSList(_fs_collection(list)):
    def __subnodes__(self) -> list[str]:
        return [ child.__node__.name for child in self ]

    def __on_node_set__(self, var: Node):
        children = var.child_nodes.values()
        order: list[str] = var.tree_payload
        for cn in order:
            new_instance = self.__type_info__()
            new_instance.__node__ = children[cn]
            new_instance.__on_node_loaded__= self.__on_node_loaded__ # this could be done in the next call
            self.__on_node_loaded__(new_instance)
            self.append(new_instance)


class FSSet(_fs_collection(set)):
    def __subnodes__(self) -> list[str]:
        # in case of sets we dont care about the order, but keeping same(ish) order is a microoptimization against JSON+filesystem
        return sorted([ child.__node__.name for child in self ])

    def __on_node_set__(self, var: Node):
        children = var.child_nodes.values()
        subset: list[str] = var.tree_payload
        for cn in subset:
            new_instance = self.__type_info__()
            new_instance.__node__ = children[cn]
            new_instance.__on_node_loaded__= self.__on_node_loaded__ # this could be done in the next call
            self.__on_node_loaded__(new_instance)
            self.add(new_instance)

class FSDict(_fs_collection(dict)):
    def __subnodes__(self) -> dict[str, str]:
        return { k, v.__node__.name for k, v in self.items() }

    def __on_node_set__(self, var: Node):
        children = var.child_nodes.values()
        mapping: dict[str, str] = var.tree_payload
        (key_type, val_type) = self.__type_info__
        for k, v in mapping.items():
            key = from_str(k, key_type)
            new_instance = val_type()
            new_instance.__node__ = children[v]
            new_instance.__on_node_loaded__= self.__on_node_loaded__ # this could be done in the next call
            self.__on_node_loaded__(new_instance)
            self[key] = new_instance

class FSEntityInterceptor(Interceptor):
    def __init__(self, target):
        super.__init__()
        self._target = target
        self._payload: list[str] = type(target).__payload_properties__
        self._subnodes: list[str] = type(target).__subnode_properties__
        self.__node: Node = None
        self.__on_node_loaded__: Callable[["some FSEntity"], None] = None
        #payload/subnodes loaded are handled by _node setter

    @property
    def __node__(self) -> Node:
        return self.__node

    @__node__.setter
    def __node__(self, val: Node):
        self.__node = val
        self._payload_loaded = False
        self._subnodes_loaded = False

    def _is_payload_property(self, name: str) -> bool:
        return name in self._payload

    def _is_subnode_property(self, name: str) -> bool:
        return name in self._subnodes

    def _load_payload(self, data):
        prop_types = get_type_hints(type(data))
        loaded = self._node.json_payload
        #todo this is a good place to introduce consistency checks, e.g. comparing if properties declared in code match keyset of loaded
        for name in self._payload:
            json_model = loaded.get(name) # using .get() and not [] to get a None if missing; todo decide if this is desired behaviour
            prop_type = prop_types[name] # but in this case types are supposed to come from the same place as name, so we cannot allow misses
            deserialized = from_json_model(json_model, prop_type)
            # todo other strategies are possible (e.g. if value missing from JSON, do not override, so previous value stays; but if JSON holds null/None -> overwrite with None)
            setattr(data, name, deserialized)

    def _on_new_subnode_entity(self, new_fsentity):
        if self.__on_node_loaded__ is not None:
            self.__on_node_loaded__(new_fsentity)

    def _make_entity(self, node: Node, t: type or GenericAlias) -> "instance of t":
        # assuming that t is fsentity type (this check happens when making types)
        out = None
        if isinstance(t, GenericAlias):
            if issubclass(t.__origin__, list):
                assert len(t.__args__) == 1
                out = FSList(t.__args__[0], self.__on_node_loaded__)
            elif issubclass(t.__origin__, set):
                assert len(t.__args__) == 1
                out = FSSet(t.__args__[0], self.__on_node_loaded__)
            elif issubclass(t.__origin__, dict):
                assert len(t.__args__) == 2
                out = FSDict(t.__args__, self.__on_node_loaded__)
            else:
                assert False
        else:
            out = _simple(t)()
        assert out is not None
        out.__node__ = node
        # out.__on_node_loaded__ = self.__on_node_loaded__
        return out


    def _load_subnodes(self, target):
        prop_types = get_type_hints(type(target.__data__))
        subnode_mapping = dict(self._node.child_nodes)
        #todo this is a good place to introduce consistency checks, e.g. comparing if properties declared in code match loaded subnodes
        for name in self._subnodes:
            prop_type = prop_types[name]
            if name not in subnode_mapping:
                subnode_mapping[name] = Node(os.path.join(target.__node__.path, name), target.__node__.db, node_type(prop_type))
            subnode = subnode_mapping[name]
            entity = self._make_entity(subnode, prop_type)
            self._on_new_subnode_entity(entity)
            setattr(target.__data__, name, entity)

    def around_get(self, target, name: str, wrapped: Callable):
        if self.__node__ is not None:
            if self._is_payload_property(name) and not self._payload_loaded:
                self._load_payload(target.__data__)
            elif self._is_subnode_property(name) and not self._subnodes_loaded:
                #todo load per-property, not all at once
                self._load_subnodes(target)
        return wrapped(target)

    def around_set(self, target, name: str, value, wrapped):
        if self.__node__ is not None:
            if self._is_payload_property(name) and not self._payload_loaded:
                self._load_payload(target.__data__)
            elif self._is_subnode_property(name):
                raise RuntimeError("Property "+name+" is managed by FSDBM, thus its setter is disabled") #todo dedicatd
        return wrapped(target, value)

def make_fsentity(t: type):
    def customize(d):
        props = d["__intercepted_properties__"]
        payload: list[str] = []
        subnodes: list[str] = []
        for n, t in props.items():
            ( subnodes if is_fsentity_type(t) else payload ).append(n)

        def __initialize__(self):
            self.__interceptor__ = FSEntityInterceptor(self)

        @property
        def __node__(self) -> Node:
            return self.__interceptor__.__node__

        @__node__.setter
        def __node__(self, val: Node):
            self.__interceptor__.__node__ = val

        @property
        def __on_node_loaded__(self) -> Node:
            return self.__interceptor__.__on_node_loaded__

        @__on_node_loaded__.setter
        def __on_node_loaded__(self, val: Node):
            self.__interceptor__.__on_node_loaded__ = val

        def __payload__(self) -> dict[str, Any]:
            return {
                k: to_json_model(getattr(self.__data__, k))
                for k in payload
            }

        def __subnodes__(self) -> list[str]:
            return subnodes or None # if subnodes is empty, return None; looks weird, but it disables any FS actions instead of pointless loading or update to an empty list

        return {
            "__payload_properties__": payload,
            "__subnode_properties__": subnodes,
            "__initialize__": __initialize__,
            "__node__": __node__,
            "__on_node_loaded__": __on_node_loaded__,
            "__payload__": __payload__,
            "__subnodes__": __subnodes__
        }

    return make_intercepted(t, customize)
    # return make_intercepted(t, customize, bases=(_BaseFSEntity, ))

class DBMAccessPoint(ABC):
    def __init__(self):
        self.id_to_instance = None
        self.

    @abstractmethod
    def root_node(self, t: type or GenericAlias, or_make: Callable[[], Node or "t"]) -> Node: ...

    def root(self, t: type or GenericAlias):
        assert is_fsentity_type(_simple(t))

    @abstractmethod
    def __enter__(self) -> "DBMAccessPoint": ...


    def resolve(self, *path: tuple(str or Path)) -> Path:
        return Path(abspath(Path(self.data, *path)))

    @abstractmethod
    def __exit__(self, exc_type: type[BaseException] | None, exc_val: BaseException | None, exc_tb: TracebackType | None) -> None: ...

class Session(Protocol):
    def root(self, t: type or GenericAlias) -> "session-managed instance of t": ...

class SessionManager(Protocol):
    def __enter__(self) -> Session: ...

    def __exit__(self, exc_type: type[BaseException] | None, exc_val: BaseException | None, exc_tb: TracebackType | None) -> None: ...

class FsDBM:
    _RW_SESSION_PER_WORKSPACE = {}

    def __init__(self, workspace: str or Path):
        self.workspace = _makedir(workspace)
        self.data = _makedir(workspace, "data")
        self.trash = _makedir(workspace, "trash")

    def rw_session(fsdbm_self) -> SessionManager:
        refs: dict[int, FSEntity] = {}
        tracking_order: list[int] = []
        def track(obj):
            objid = id(obj)
            if not objid in refs:
                refs[objid] = obj
            if not objid in tracking_order:
                tracking_order.append(objid)
            obj.__on_node_loaded__ = track

        class RWSession(Session):
            def root(self, t: type or GenericAlias) -> "instance of t":
                ...

        class RWManager(SessionManager):

            def __enter__(mgr_self) -> RWSession:
                assert str(fsdbm_self.workspace) not in FsDBM._RW_SESSION_PER_WORKSPACE # there can be only 1 session per workspace
                out = RWSession()
                FsDBM._RW_SESSION_PER_WORKSPACE[str(fsdbm_self.workspace)] = out
                return out

            def __exit__(mgr_self, exc_type: type[BaseException] | None, exc_val: BaseException | None, exc_tb: TracebackType | None) -> None: ...
                for tracked in tracking_order:
                    o = refs[tracked]
                    nt = node_type(type(o))
                    if nt != NodeType.ENTITY:
                        subentities = list(o.values() if nt == NodeType.MAPPING else o)
                        for sub in subentities:
                            if sub.__node__ is None:
                                new_id = str(uuid.uuid4())
                                subnode = Node(os.path.join(o.path, new_id), fsdbm_self, node_type(type(sub)))
                                o.__on_node_loaded__(sub)
                                sub.__node__ = subnode
                    payload = o.__payload__
                    if payload:
                        if nt == NodeType.ENTITY:
                            # update in-fs object with fields from memory
                            vals = tracked.__node__.json_payload or dict()
                            for k, v in payload.items():
                                vals[k] = v
                            tracked.__node__.json_payload = vals
                        else:
                            # only non-container entitites have payload ; todo dedicated
                            assert False
                    subnodes = o.__subnodes__
                    if subnodes:
                        if nt == NodeType.ENTITY: # in case of entity
                            # update in-fs object with fields from memory
                            vals = tracked.__node__.tree_payload or dict()
                            for k, v in subnodes.items():
                                vals[k] = v
                            tracked.__node__.tree_payload = vals
                        else: # in case of container
                            # overwrite in-fs object
                            tracked.__node__.tree_payload = subnodes

                del FsDBM._RW_SESSION_PER_WORKSPACE[str(fsdbm_self.workspace)]