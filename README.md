# Storyteller

Research scenarios given some love. Persist the progress of your story and don't lose your results.

## ToDo

### v0.0.1

- [ ] add license
- [ ] add some meaningful readme and make it public (long way until there)
- [x] add better session management (extract PersistentRoot with start/end session; requires renaming to tree)
    - [x] update tests to use it
- [x] rename StackedExecutor to PersistentTaskTree
  - [ ] tests and some impl details still use executor as variable names
- [x] add journal listener (may be easier if sessions are well handled already)
- [ ] change choice to parallel task (will be way easier after renaming to tree, because methods will be easier to name)
    - [x] keep choice as a utility method?
    - [ ] THIS REQUIRES MUCH MORE TESTING
- [ ] test journaling (will be easier once session management is done, as we won't have to spin up subprocess just to do amendments, etc)
    - [x] actually implement SHRUNK entry
- [ ] more extensive testing (see comments in existing tests)
- [ ] deletion
    - [x] deleting files
        - [x] implementation
        - [x] testing
        - [ ] add file modification summary to the report
    - [x] when merging DBs (in parallel nodes) - deleting documents/objects
        - [x] implementation
        - [x] testing
- [ ] parallel node needs to track what has been incorporated; if that has changed between runs, we need to amend the node
  - give choice whether incorporation order matters
  - if it does, add some new journal entry ("reordered"?)

### v0.0.2

- [ ] currently we assume that all failures are caused by exceptions, so the throw/catch/finally is sufficient for cleanup;
  if power went down during task execution, then we need to start cleaning up on task start **needed for v0.0.2**
    - [ ] clean up data on task start
    - [ ] flush at appropriate moment, so we are sure that data isnt lost
- [ ] adopting orphans **needed for v0.0.2**
    - if we orphan a task by changing ir shrinking body of the parent, then in the next run we extend it with task with the same definition as orphan,
      we should just reuse the orphan to save some time
    - this will be tricky if the orphan was defined with a class that isnt present anymore (e.g. storyteller research
      was deleted and its key class too; when looking up orphans will fail on undeserializable type)

### backlog


 - [ ] add some initial docs
 - [ ] more work on listeners
   - [ ] do some testing (not that important, since the impl is trivial and manual tests confirm it works)
   - [ ] VCS integration (commit and push to GIT per entry? do we flush on journal event, or do we give some control over flushing?)
   - [ ] dynamic reporting
     - REST+websocket API to expose task summaries as JSONs and push events, JS-friendly page that renders them 
 - [ ] give some love to storyteller (as points above mainly focus on task tree) (overlaps with some ideas below)
 - [ ] enhance storage
   - try to minimize number of dependencies; in perfect scenario, default impl uses just Nitrite and nothing else 
   - [ ] KV store? doable with NoSQL collection
   - [ ] SQL? 
   - [ ] use case: something (experiment written in C/C++/D2) handles file on its own and we want to inject them into storage
     - maybe some symlink magic?
 - [ ] enhance storyteller API; add overloads with single XContext param (x=SequentialNode/ParalleNode/Leaf) that group all the parameters of bodies
 - [ ] fluent storyteller
   - it should track node/leaf parameters and expose them with static methods
   - bodies should be parameterless, users should be able to avoid so many lambda params in favour of these static methods
   - may be easier once we introduce Contexts
   - [ ] first, thread-unsafe implementation
   - [ ] then, thread-safe one (ThreadLocal to the rescue!)
 - [ ] TBD: log capturing? 
   - it would be nice to store logs of leaf execution for later analysis
   - but that would require forcing users to use slf4j or flogger or (...)
   - this would be alright for user-generated logs, but what about external libs, e.g. deeplearning4j or even subprocesses?
   - alternatively, we could capture stdout, stderr and log entries
 - [ ] alterative DB merger that exploits Nitrite listener to gather the changed IDs
   - a song of the future, need some bigger projects using storyteller first, to perform some valid benchmarking
 - [ ] fix the slf4j/flogger mess
 - [ ] parallel and sequential nodes share a lot of code -> extract AbstractNodeExecution?
 - [ ] on the same note, the order subpackage is weird; I can't say without reading the usage whether it is order of current tasks subtasks or of the parent