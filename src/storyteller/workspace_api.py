import os.path
from abc import ABC, abstractmethod


class WorkspaceAPI(ABC):
    @abstractmethod
    def __init__(self, name: str, origin: str, ref: str, parent_dir: str):
        pass

    @property
    @abstractmethod
    def name(self) -> str:
        pass

    @property
    @abstractmethod
    def root_dir(self) -> str:
        pass

    def path_to(self, path):
        return os.path.join(self.root_dir, path)

    @abstractmethod
    def clean(self):
        '''
        Delete everything in the root dir of the workspace
        :return:
        '''
        pass

    @property
    @abstractmethod
    def current_commit(self) -> str:
        '''
        Returned as commit hash
        '''
        pass

    @property
    @abstractmethod
    def current_commit_message(self) -> str:
        pass

    @property
    @abstractmethod
    def current_branch(self) -> str:
        pass

    @abstractmethod
    def checkout(self, tag_or_branch: str):
        pass

    @abstractmethod
    def branch_out(self, name: str):
        pass

    @abstractmethod
    def merge(self, another_branch: str):
        pass

    @property
    @abstractmethod
    def dirty(self) -> bool:
        pass

    @abstractmethod
    def commit(self, message):
        pass

    @abstractmethod
    def push(self):
        pass