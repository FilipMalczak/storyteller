import multiprocessing
from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum, auto
from typing import Any, TypeVar


class TaskEvent(ABC):
    '''
    Base class for events
    '''

class TaskType(Enum):
    ROOT = auto()
    SEQUENCE = auto()
    PARALLEL = auto()
    LEAF = auto()

    @property
    def is_node(self) -> bool:
        return self != TaskType.LEAF

    @property
    def has_parent(self) -> bool:
        return self != TaskType.ROOT

# todo hide these
short = { TaskType.ROOT: "r", TaskType.SEQUENCE: "s", TaskType.PARALLEL: "p", TaskType.LEAF: "l" }
reverse_short = { v: k for k, v in short.items()}

class TreePath:
    TP = TypeVar('TreePath', bound='TreePath')

    def __init__(self, current_type: TaskType, history: [[TaskType]]):
        assert current_type
        self.current_type = current_type
        self.history = list(map(list, history))

    def _merge_history_and_current(self):
        new_history = list(self.history)
        if new_history:
            new_history[-1] = list(new_history[-1])
            new_history[-1].append(self.current_type)
        else:
            new_history.append([self.current_type])
        return new_history

    def subtask(self: TP, subtask_type: TaskType) -> TP:
        new_history = self._merge_history_and_current()
        new_history.append([])
        return TreePath(subtask_type, new_history)

    def next_sibling(self: TP, sibling_type: TaskType) -> TP:
        new_history = self._merge_history_and_current()
        return TreePath(sibling_type, new_history)

    def encode(self):
        full_story = self._merge_history_and_current()
        segments = []
        for scope in full_story:
            scope_path = []
            for subtype in scope:
                if scope_path and scope_path[-1][0] == subtype:
                    scope_path[-1][1] += 1
                else:
                    scope_path.append([subtype, 1])
            segments.append(
                ".".join(
                    map(
                        lambda x: short[x[0]]+"["+str(x[1])+"]",
                        scope_path
                    )
                )
            )
        return "-".join(segments)

    @classmethod
    def root(cls: type[TP]) -> TP:
        return TreePath(TaskType.ROOT, [])

    @classmethod
    def parse(cls: type[TP], encoded: str) -> TP:
        segments = encoded.split("-")
        full_story = []
        for s in segments:
            scope = []
            parts = s.split(".")
            for p in parts:
                shorted, bracket, count_and_bracket = p.partition("[")
                assert bracket == "["
                assert count_and_bracket[-1] == "]"
                cnt = int(count_and_bracket[:-1])
                t = reverse_short[shorted]
                scope += ([ t ])*cnt
            full_story.append(scope)
        current = full_story[-1][-1]
        full_story[-1] = list(full_story[-1][:-1])
        if len(full_story) == 1 and not full_story[0]:
            full_story = []
        return TreePath(current, full_story)

    def __eq__(self, o: object) -> bool:
        if o is self: return True
        if not isinstance(o, TreePath): return False
        return self.current_type == o.current_type and tuple(map(tuple, self.history)) == tuple(map(tuple, o.history))

    def __hash__(self) -> int:
        return 3*hash(self.current_type) + 7*hash(tuple(map(tuple, self.history)))

    def __str__(self) -> str:
        return "TreePath("+self.encode()+")"

    def __repr__(self) -> str:
        return "TreePath(current="+str(self.current_type)+", history="+str(self.history)+")"

def assert_match(tp: TreePath, encoded: str):
    try:
        assert TreePath.parse(encoded) == tp
        assert tp.encode() == encoded
    except:
        print("TP:")
        print(str(tp))
        print(repr(tp))
        print("encoded:", encoded)
        parsed = TreePath.parse(encoded)
        print("parsed:")
        print(str(parsed))
        print(repr(parsed))
        print("TP.encoded:", tp.encode())
        raise

assert_match(TreePath.root(), "r[1]")
assert_match(
    TreePath.root()
        .subtask(TaskType.SEQUENCE).next_sibling(TaskType.SEQUENCE).next_sibling(TaskType.PARALLEL)
        .subtask(TaskType.SEQUENCE)
        .subtask(TaskType.LEAF).next_sibling(TaskType.LEAF),
    "r[1]-s[2].p[1]-s[1]-l[2]"
)
assert_match(
    TreePath.root()
    .subtask(TaskType.SEQUENCE).next_sibling(TaskType.SEQUENCE).next_sibling(TaskType.PARALLEL)
    .subtask(TaskType.SEQUENCE).next_sibling(TaskType.SEQUENCE).next_sibling(TaskType.PARALLEL).next_sibling(TaskType.PARALLEL).next_sibling(TaskType.SEQUENCE)
    .subtask(TaskType.SEQUENCE).next_sibling(TaskType.SEQUENCE).next_sibling(TaskType.SEQUENCE).next_sibling(TaskType.PARALLEL)
    .subtask(TaskType.PARALLEL)
    .subtask(TaskType.SEQUENCE)
    .subtask(TaskType.SEQUENCE).next_sibling(TaskType.LEAF).next_sibling(TaskType.LEAF).next_sibling(TaskType.LEAF).next_sibling(TaskType.LEAF),
    "r[1]-s[2].p[1]-s[2].p[2].s[1]-s[3].p[1]-p[1]-s[1]-s[1].l[4]"
)

class TaskAPI(ABC):
    @property
    @abstractmethod
    def id(self) -> str:
        pass

    @property
    @abstractmethod
    def parent_id(self) -> str:
        pass

    @property
    @abstractmethod
    def name(self) -> str:
        pass

    @property
    @abstractmethod
    def params(self) -> dict:
        pass

    @property
    @abstractmethod
    def tree_path(self) -> TreePath:
        pass

    @property
    @abstractmethod
    def task_type(self) -> TaskType:
        pass

    @property
    @abstractmethod
    def is_executed(self) -> bool:
        '''
        Has this task run? For nodes - has it run in this session? For leaves - has it ever run succesfully?
        '''
        pass

    @property
    @abstractmethod
    def is_succesful(self) -> bool:
        pass

    @property
    @abstractmethod
    def is_joined(self) -> bool:
        pass

    @property
    @abstractmethod
    def is_merged(self) -> bool:
        pass

    @abstractmethod
    def join(self):
        '''Should be idempotent'''
        pass

    @abstractmethod
    def merge_into_parent(self):
        '''Should be idempotent'''
        pass

    @property
    @abstractmethod
    def result(self) -> Any:
        '''Should raise if not finished or not succesfull'''
        pass

    @property
    @abstractmethod
    def exception(self) -> Exception:
        '''Should raise if not finished; should return None if task succesful'''
        pass

    @property
    @abstractmethod
    def events(self) -> [ TaskEvent ]:
        pass

@dataclass
class ParallelismConfig:
    pool_size: int
    pool_factory = lambda size: multiprocessing.Pool(size)

    def make_pool(self):
        return self.pool_factory(self.pool_size)

class TreeBuilderAPI(ABC):
    #todo snapshot/version?
    @abstractmethod
    def root(self, name=None, params={}) -> TaskAPI:
        pass

    @abstractmethod
    def sequence(self, name=None, params={}) -> TaskAPI:
        pass

    @abstractmethod
    def parallel(self, name=None, params={}, config=None) -> TaskAPI:
        pass

    @abstractmethod
    def leaf(self, name=None, params={}) -> TaskAPI:
        pass

class TaskTreeAPI(ABC):
    @property
    @abstractmethod
    def builder(self) -> TreeBuilderAPI:
        pass

    

