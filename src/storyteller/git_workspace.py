import os.path
from itertools import chain
from os import makedirs
from shutil import rmtree
from os.path import exists

import git
from git import GitCommandError, Head, Commit

from storyteller.workspace_api import WorkspaceAPI

def find_by_name(iter, name):
    out = list(filter(lambda x: x.name == name, iter))
    assert len(out) < 2
    if out:
        return out[0]

class GitWorkspace(WorkspaceAPI):
    def __init__(self, name: str, origin: str, ref: str, parent_dir: str):
        self._name = name
        self._root_dir = os.path.join(parent_dir, name)
        if not exists(self._root_dir):
            self.git_repo = git.Repo.clone_from(origin, self._root_dir)
        else:
            self.git_repo = git.Repo(self._root_dir)
            remote_origin = self.git_repo.remote()
            assert list(filter(lambda x: x == origin, remote_origin.urls))
        if ref:
            self.checkout(ref)

    @property
    def name(self) -> str:
        return self._name

    @property
    def root_dir(self) -> str:
        return self._root_dir

    def clean(self):
        rmtree(self._root_dir)
        makedirs(self._root_dir)

    @property
    def current_commit(self) -> str:
        return self.git_repo.active_branch.commit.hexsha

    @property
    def current_commit_message(self) -> str:
        return self.git_repo.active_branch.commit.message

    @property
    def current_branch(self) -> str:
        return self.git_repo.active_branch.name

    def checkout(self, tag_or_branch: str):
        try:
            self.git_repo.remote().pull(self.git_repo.remote().name+"/"+ref)
        except GitCommandError:
            # it means that there is no such ref on the remote
            pass
        ref_head = find_by_name(self.git_repo.heads, tag_or_branch)
        ref_tag = find_by_name(self.git_repo.tags, tag_or_branch)
        self.git_repo.head.ref = ref_head or ref_tag

    def branch_out(self, name: str):
        Head.create()
        self.git_repo.create_head(name)
        self.git_repo.create_tag(name+"/START")

    def commit(self, message):
        self.git_repo.git.add(update=True)
        self.git_repo.index.commit(message)

    @property
    def dirty(self) -> bool:
        return self.git_repo.is_dirty()

    def merge(self, another_branch: str):
        start_tag = self.git_repo.tag(self.current_branch+"-START")
        current_commit = self.git_repo.head.commit

        if start_tag.commit.hexsha == current_commit.hexsha: #todo maybe eq is enough?
            # is this the way to fast forward?
            self.git_repo.head.commit = find_by_name(self.git_repo.heads, another_branch).name
        else:
            self.git_repo.index.merge_tree(another_branch)
            self.git_repo.index.commit("Merge "+another_branch+" into "+self.current_branch)
        self.git_repo.create_tag(another_branch+"-END", another_branch)
        self.git_repo.create_tag(self.current_branch+"-MERGE-"+another_branch)

    def push(self):
        #does it push deletion? it may be important for renaming
        self.git_repo.remote().push()
#
# GitWorkspace("x", None, None, None)