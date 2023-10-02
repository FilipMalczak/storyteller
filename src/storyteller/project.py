from os.path import join
from shutil import rmtree

from src.storyteller.git_workspace import GitWorkspace
from src.storyteller.workspace_api import WorkspaceAPI


class Project:
    def __init__(self, name: str, root_dir: str, origin: str):
        self.name = name
        self.root_dir = root_dir
        self.origin = origin
        self.workspaces = {}
        self.zombie_workspaces = []

    def get_workspace(self, name: str, ref: str = None) -> WorkspaceAPI:
        out = GitWorkspace(name, self.origin, ref, self.root_dir)
        self.workspaces[name] = out
        return out

    @property
    def workspace_names(self) -> [str]:
        return list(self.workspaces.keys())

    def delete_zombies(self):
        for zombie in self.zombie_workspaces:
            rmtree(join(self.root_dir, zombie))
        self.zombie_workspaces = []

    def __getitem__(self, item):
        self.get_workspace(item)
