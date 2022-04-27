# Storyteller

Research scenarios given some love. Persist the progress of your story and don't lose your results.

## ToDo

 - [ ] add license **needed for release v0.0.1**
 - [ ] add some meaningful readme and make it public (long way until there) **needed for release v0.0.1**
 - [ ] add some initial docs
 - [x] rename StackedExecutor to PersistentTaskTree **needed for release v0.0.1**
 - [x] add better session management (extract PersistentRoot with start/end session; requires renaming to tree) **needed for release v0.0.1** 
   - [x] update tests to use it 
 - [ ] add journal listener (may be easier if sessions are well handled already)
   - [ ] listener itself **needed for release v0.0.1**
   - [ ] VCS integration (commit and push to GIT per entry? do we flush on journal event, or do we give some control over flushing?)
   - [ ] dynamic reporting
     - REST+websocket API to expose task summaries as JSONs and push events, JS-friendly page that renders them 
 - [ ] change choice to parallel task (will be way easier after renaming to tree, because methods will be easier to name) **needed for release v0.0.1**
   - [x] keep choice as a utility method?
   - [ ] THIS REQUIRES MUCH MORE TESTING
 - [ ] test journaling (will be easier once session management is done, as we won't have to spin up subprocess just to do amendments, etc) **most probably needed for release v0.0.1**
 - [ ] more extensive testing (see comments in existing tests) **needed for release v0.0.1**
 - [ ] give some love to storyteller (as points above mainly focus on task tree)
 - [ ] enhance storage
   - try to minimize number of dependencies; in perfect scenario, default impl uses just Nitrite and nothing else 
   - [ ] KV store? doable with NoSQL collection
   - [ ] SQL? 
   - [ ] deletion! **needed for release v0.0.1**
     - [ ] deleting files
       - [x] implementation
       - [ ] testing
     - [ ] when merging DBs (in parallel nodes) - deleting documents/objects
        - [ ] implementation
        - [ ] testing 
   - [ ] use case: something (experiment written in C/C++/D2) handles file on its own and we want to inject them into storage
     - maybe some symlink magic?
- [ ] enhance storyteller API; add overloads with single XContext param (x=SequentialNode/ParalleNode/Leaf) that group all the parameters of bodies
 - [ ] fluent storyteller
   - it should track node/leaf parameters and expose them with static methods
   - bodies should be parameterless, users should be able to avoid so many lambda params in favour of these static methods
   - may be easier once we introduce Contexts
   - [ ] first, thread-unsafe implementation
   - [ ] then, thread-safa one (ThreadLocal to the rescue!)
 - [ ] currently we assume that all failures are caused by exceptions, so the throw/catch/finally is sufficient for cleanup;
   if power went down during task execution, then we need to start cleaning up on task start
   - [ ] clean up data on task start
   - [ ] flush at appropriate moment, so we are sure that data isnt lost