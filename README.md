# Storyteller

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

[![](https://jitpack.io/v/FilipMalczak/storyteller.svg)](https://jitpack.io/#FilipMalczak/storyteller)

| `master` | `develop` |
| --- | --- |
| [![master](https://github.com/FilipMalczak/storyteller/actions/workflows/ci.yaml/badge.svg?branch=master)](https://github.com/FilipMalczak/storyteller/actions/workflows/ci.yaml) | [![develop](https://github.com/FilipMalczak/storyteller/actions/workflows/ci.yaml/badge.svg?branch=develop)](https://github.com/FilipMalczak/storyteller/actions/workflows/ci.yaml) |
| [![codecov](https://codecov.io/gh/FilipMalczak/storyteller/branch/master/graph/badge.svg?token=8JXL8TJ84K)](https://codecov.io/gh/FilipMalczak/storyteller) | [![codecov](https://codecov.io/gh/FilipMalczak/storyteller/branch/develop/graph/badge.svg?token=8JXL8TJ84K)](https://codecov.io/gh/FilipMalczak/storyteller) |

[![Nitrite](https://img.shields.io/badge/uses-Nitrite-blue.svg)](https://github.com/nitrite/nitrite-java)
[![valid4j](https://img.shields.io/badge/uses-valid4j-blue.svg)](https://github.com/valid4j/valid4j)
[![recordtuples](https://img.shields.io/badge/uses-recordtuples-blue.svg)](https://github.com/FilipMalczak/recordtuples)

Research scenarios given some love. Persist the progress of your story and don't lose your results.

## Story time, or motivation for this framework

### TL;DR

Storyteller is a framework that aims at providing a generic structure for running tasks that can be interrupted, started
again and proceed from the interruption point. It was created with scientific research in mind. 

### Intro

Imagine that you wanna conduct an experiment. Say, for example, you came up with some new twist when it comes to 
evolutionary algorithms or neural networks. You will probably want to compare its results with existing methods.
To do that, you will implement both your method and the contender, tweak parameters for both and compare results with
optimized params.

Now, tweaking parameters will require you to run the method for different parameters a lot of times to find the best ones
(or at least good ones). There are many approaches how to do that (greedy search, full search or maybe some other heuristic),
but that is not what we wanna solve here. The thing is, each run takes time.

### Rough numbers

Let's make this a bit more real. We wanna tweak parameters for evolutionary algorithm. In an oversimplified  case you
have 3 parameters: population size, crossover probability and mutation probability.

You wanna test 20 different population sizes (25, 50, 75..., 475, 500) and 10 values for each probability (0.05, 0.1, 0.15..., 0.5). 

For simplicity you do greedy search: you assume some initial probabilities, choose best population size for them, 
then choose the best crossover probability for the chosen size and assumed mutation probability, then do the same with 
mutation probability and best size and crossover probability from previous steps.

Evolutionary algorithms have random element, so you need to repeat the experiment couple time and take some kind of 
statistic, like average or median. Again, for simplicity, you run it 3 times for each parameter set.

In the end you get 3 runs*(20 sizes + 10 probabilities + 10 probabilities) = 120 runs. Let's say that single experiment
take 3 minutes, on average (of course the time will depend on parameters and your optimizations, but we're looking for a
ballpark, so this will do). You end up with 360 minutes. That is 6 hours.

### The problem

So, you implement the algorithm, you write the code (or maybe a script) that will automate the search for parameters, you
run it in the evening and you go to sleep, thinking that in the morning you'll get a pretty little CSV that summarizes these
120 runs.

You wake up, look at the screen... And after 2 hours you got a NullPointerException. You calm yourself down, you fix it,
run it again, and after 5 hours the electricity in your building goes down.

Finally, you succeed. It took you one wasted night and another 11 hours of computing.

When you start working on similar setup for the other method, you create an abstraction that can recover after failure 
and start computations from last succesful point. In the end, you finish the paper that compares both these evolutionary 
algorithm variants.

After some time you start another paper that compare 2 different approaches to neural networks. All the scripting, 
persistence, recoverability have to be reimplemented from scratch, because neural networks have very different parameters
and its models are stored in a very different way.

### Enter Storyteller

As was stated at the top of this, Storyteller aims to be a generic framework to replace that scripting, persistence, etc.

The entry point is a "story". Story consists of arcs, threads and decissons. 

Arcs are used to give your story structure. In fact, story is just a root arc.

Threads consist of scenes. Scenes are the actual units of computations.

Decisions are parts of story where you provide some set to be looked through (a domain), and a way to evaluate each 
element of that set.

Arcs, threads and decision run each time the code is executed, but scenes are skipped if they were succesfully executed 
in the past. Every task has a workspace (with managed access to raw files and a NoSQL database; in case of default 
implementation, it is [Nitrite](https://github.com/nitrite/nitrite-java)); it is empty when a story starts and every other task
starts from the state of workspace of previous finished task. Scenes have write access to it, while all the other 
tasks - just read access. Story, arc and thread workspace state always matches the state of last finished subtask.
Decisions are a bit more tricky - they branch out to a new workspace per a domain element, then, once all the branches finish,
chooses the best (by evaluating each) and proceeds with its state.

> Underlying implementation allows for some degree of parallelism and manual integration of subtasks; in fact, decisions
> are implemented over "parallel nodes" (while arcs and threads are "sequential nodes"). They will be exposed to Storyteller
> API in the future, when the feature matures enough. For now, mostly everything is sequential and decisions can "merge in"
> only one branch.

For example, your evolutionary algorithm research could look like:

    record Result(int size, float cp, float mp, int iteration, double result) {}
    
    storyteller.tell("Method1 param search", () -> {
        thread("Initialize parameters", () -> {
            scene("Store them", () -> {
                file("size").write(100);
                file("cp").write(0.4);
                file("mp").write(0.2);
            });
        });
        decision("Find best size", () -> {
            var cp = file("cp").readFloat();
            var mp = file("mp").readFloat();
            domain(25, 50, 75, ..., 475, 500); //or IntStream.range(1, 20).map(i -> i*25)
            research((size) -> {
                // each thread will get its own workspace and database that starts with the state of parent workspace and db
                thread("runs", () -> {
                    var algo = new Algorithm(size, cp, mp);
                    for (int i=0; i<3; ++i) {
                        scene("run #"+i, () -> {
                            var result = algo.run();
                            //Storyteller provides file storage, as well as NoSQL storage; default implementation uses Nitrite
                            noSql().insert(new Result(size, cp, mp, i, result));
                        });
                    }
                });
            });
            // each workspace will be evaluated in isolation and the one that scores best will be used to proceed
            evaluate((size) -> {
                  noSql()
                    .find(and(eq("size", size), eq("cp", cp), eq("mp", mp)))
                    .stream()
                    .mapToDouble(Result::result)
                    .average()  
            });
        });
        decision("Find best crossover probability", () -> {
            var cp = file("size").readInt(); //will read data from the best workspace chosen in previous decision
            var mp = file("mp").readFloat();
            (...)
        });
        decision("Find best mutation probability", () -> {
            //a lot of code can be reused; e.g. evaluation lambdas actually implement a type that can be defined once and reused in each decision
            (...)
        });
    });

> This is not real Storyteller API, just a simplified version for examples sake;
> at this point development is focused on more crucial issues, like persistence and recoverability, while fluent API
> and syntactic sugar is gonna come in the future.
>
> Rationale is simple - its better to have a bit messier code that will not lose your results, than pretty code that will
> lose them.
>
> See an actual, yet trivial, runnable experiment [here](src/test/java/com/github/filipmalczak/storyteller/story/example1/ExampleExperiment.java)
> and its short description [here](src/test/java/com/github/filipmalczak/storyteller/story/example1/README.md).
>
> Personally, I favour functional testing over unit testing every little detail. Have a look at
> [the test suite](src/test/java/com/github/filipmalczak/storyteller) and
> [how the Storyteller is implemented over the "task tree" structure](src/main/java/com/github/filipmalczak/storyteller/impl/story/TreeStoryteller.java);
> it should tell you more on how the API really looks like and how to use it.

## Get it


Hosting is handled via [jitpack](https://jitpack.io/#FilipMalczak/storyteller).

Current master version is [0.1.0](https://github.com/FilipMalczak/storyteller/releases/tag/0.1.0)

### Gradle

    allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
    }
    
    dependencies {
      implementation 'com.github.FilipMalczak:storyteller:0.1.0'
    }

### Maven

    <repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
    
    (...)
    
    <dependency>
	    <groupId>com.github.FilipMalczak</groupId>
	    <artifactId>storyteller</artifactId>
	    <version>0.1.0</version>
	</dependency>

### Others

Look it up on [jitpack](https://jitpack.io/#FilipMalczak/storyteller/0.1.0).

## ToDo

### v0.1.0

- [x] add license
- [x] add some meaningful readme 
- [x] make the repo public (long way until there)
- [x] add better session management (extract PersistentRoot with start/end session; requires renaming to tree)
    - [x] update tests to use it
- [x] rename StackedExecutor to PersistentTaskTree
  - [x] tests and some impl details still use executor as variable names (not done; in a way it makes sense, since tree has execute() methods)
- [x] add journal listener (may be easier if sessions are well handled already)
- [x] change choice to parallel task (will be way easier after renaming to tree, because methods will be easier to name)
    - [x] keep choice as a utility method? (nope, see below)
    - [x] THIS REQUIRES MUCH MORE TESTING (this didnt actually happen; choice isnt part of task tree API anymore)
    - [x] PARALLEL NODES IN GENERAL NEEDS TESTING
- [x] test journaling (will be easier once session management is done, as we won't have to spin up subprocess just to do amendments, etc)
    - [x] actually implement SHRUNK entry
    - [x] start, define, run, end
    - [x] start, define, run, catch, interrupt
    - [x] extend, amend - sequential
    - [x] narrow, amend - sequential
    - [x] change, amend - sequential
    - [x] extend, amend - parallel
    - [x] narrow, amend - parallel
    - [x] extend, narrow, amend - parallel
    - [x] inflate, augment
    - [x] deflate, augment
    - [x] refilter, augment
    - [x] extend, amend, inflate, augment
    - [x] extend, amend, deflate, augment
    - [x] extend, amend, refilter, augment
    - [x] narrow, amend, inflate, augment
    - [x] narrow, amend, deflate, augment
    - [x] narrow, amend, refilter, augment
    - [x] extend, narrow, amend, inflate, augment,
    - [x] extend, narrow, amend, deflate, augment
    - [x] extend, narrow, amend, refilter, augment
- [x] deletion
    - [x] deleting files
        - [x] implementation
        - [x] testing
    - [x] when merging DBs (in parallel nodes) - deleting documents/objects
        - [x] implementation
        - [x] testing
- [x] parallel node needs to track what has been incorporated; if that has changed between runs, we need to amend the node
  - give choice whether incorporation order matters
  - if it does, add some new journal entry ("reordered"?)
- [ ] add JitPack info to README

### mid-version, probably 0.1.1

- [ ] tweak journal entries hierarchy and API/impl dissonance
- [ ] enhance inflated/deflated/refiltered with details (the IDs that appeared or disappeared)
- [ ] parallel nodes testing, data-oriented
- [ ] more extensive testing (see comments in existing tests)
- [ ] add file modification summary to the report
  - maybe check if DB changed too?
- [ ] use pattern matching, its 2022, for gods sake (if (x instanceof A) { do((A) x); } -> if (x instanceOf A aX) { do(aX); })
- [ ] enhance APIs;
    - [ ] storyteller: add overloads with single XContext param (x=root/arc/thread/decision/scene) that group all the parameters of bodies
        - make them abstract, unrolled variants should be default methods
    - [ ] task tree: ditto, (x=SequentialNode/ParalleNode/Leaf)
    - [ ] task tree: add TaskSpec = definition+type, make it the default (probably remove distinct param approaches too) **candidate to be in 0.1.0**
- [ ] dynamic reporting (I really wanna do this, but it may need to be postponed, as 0.2.0 features may be more important)
  - REST+websocket API to expose task summaries as JSONs and push events, JS-friendly page that renders them

### v0.2.0

- [ ] currently we assume that all failures are caused by exceptions, so the throw/catch/finally is sufficient for cleanup;
  if power went down during task execution, then we need to start cleaning up on task start
    - [ ] clean up data on task start
    - [ ] flush at appropriate moment, so we are sure that data isnt lost
- [ ] adopting orphans
    - if we orphan a task by changing ir shrinking body of the parent, then in the next run we extend it with task with the same definition as orphan,
      we should just reuse the orphan to save some time
    - this will be tricky if the orphan was defined with a class that isnt present anymore (e.g. storyteller research
      was deleted and its key class too; when looking up orphans will fail on undeserializable type)
- [ ] "mimic an orphan"
  - if you change the structure (e.g. move a node to some parent node that is supposed to group the moved one together with a new one)
    you will lose a lot of data
  - if you look up the IDs of disowned task, you should be able to say (inside a post-change task) "this is a disowned task"
  - the disowned task stays disowned, but the storage state should be virtually copied (without actually copying anything)
    from the disowned one to the new one
  - this is first iteration of solving that problem; it is assumed to be quickly deprecated in favour of another approach,
    but we still need some experience first

### backlog

 - [ ] add some initial docs
 - [ ] more work on listeners
   - [ ] do some testing (not that important, since the impl is trivial and manual tests confirm it works)
   - [ ] VCS integration (commit and push to GIT per entry? do we flush on journal event, or do we give some control over flushing?)
 - [ ] give some love to storyteller (as points above mainly focus on task tree) (overlaps with some ideas below)
 - [ ] enhance storage
   - try to minimize number of dependencies; in perfect scenario, default impl uses just Nitrite and nothing else 
   - [ ] KV store? doable with NoSQL collection
   - [ ] SQL? 
   - [ ] use case: something (experiment written in C/C++/D2) handles file on its own and we want to inject them into storage
     - maybe some symlink magic?
 - [ ] fluent storyteller
   - it should track node/leaf parameters and expose them with static methods
   - bodies should be parameterless, users should be able to avoid so many lambda params in favour of these static methods
   - may be easier once we introduce Contexts
   - [ ] first, thread-unsafe implementation
   - [ ] then, thread-safe one (ThreadLocal to the rescue!)
 - [ ] parallel nodes in storyteller api (they are only available in underlying tree now)
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