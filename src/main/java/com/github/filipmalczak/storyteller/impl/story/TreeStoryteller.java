package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.session.Sessions;
import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.body.ActionBody;
import com.github.filipmalczak.storyteller.api.story.body.ResearchBody;
import com.github.filipmalczak.storyteller.api.story.body.StructureBody;
import com.github.filipmalczak.storyteller.api.story.closure.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.closure.DecisionClosure;
import com.github.filipmalczak.storyteller.api.story.closure.ThreadClosure;
import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class TreeStoryteller<NoSql> implements Storyteller<NoSql> {
    TaskTreeRoot<String, StorytellerDefinition, EpisodeType, NoSql> executor;

    private ArcClosure<NoSql> makeArcClosure(TaskTree<String, StorytellerDefinition, EpisodeType, NoSql> exec){
        return new ArcClosure<>() {
            @Override
            public void thread(String threadName, StructureBody<ThreadClosure<NoSql>, ReadStorage<NoSql>> body) {
                exec.execute(new StorytellerDefinition(threadName), EpisodeType.THREAD, threadToNodeBody(body));
            }

            @Override
            public void arc(String arcName, StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> body) {
                exec.execute(new StorytellerDefinition(arcName), EpisodeType.ARC, arcToNodeBody(body));
            }

            @Override
            public <Key, Score> void decision(String decision, ActionBody<DecisionClosure<Key, Score, NoSql>> body) {
                var args = decisionToChoiceBody(body);
                exec.execute(new StorytellerDefinition(decision), EpisodeType.DECISION, args.body(), args.filter());
            }
        };
    }

    private ThreadClosure<NoSql> makeThreadClosure(TaskTree<String, StorytellerDefinition, EpisodeType, NoSql> exec){
        //stop IDE from turning to lambda for consistency
        //noinspection Convert2Lambda
        return new ThreadClosure<>() {
            @Override
            public void scene(String name, ActionBody<ReadWriteStorage<NoSql>> body) {
                exec.execute(new StorytellerDefinition(name), EpisodeType.SCENE, sceneToLeafBody(body));
            }
        };
    }

    private NodeBody<String, StorytellerDefinition, EpisodeType, NoSql> arcToNodeBody(StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> body){
        return (exec, storage) -> body.action(
            makeArcClosure(exec),
            storage
        );
    }

    private NodeBody<String, StorytellerDefinition, EpisodeType, NoSql> threadToNodeBody(StructureBody<ThreadClosure<NoSql>, ReadStorage<NoSql>> body){
        return (exec, storage) -> body.action(
            makeThreadClosure(exec),
            storage
        );
    }



    private LeafBody sceneToLeafBody(ActionBody<ReadWriteStorage<NoSql>> body){
        //keep this method for consistency
        return storage -> body.action(storage);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class DecisionClosureImpl<Key, Score, NoSql> implements DecisionClosure<Key, Score, NoSql> {
        Supplier<Stream<Key>> domain;
        ResearchBody<Key, ArcClosure<NoSql>> body;
        Function<ReadStorage<NoSql>, Score> evaluator;
        Comparator<Score> comparator;

        @Override
        public DecisionClosure<Key, Score, NoSql> domain(Supplier<Stream<Key>> domain) {
            require(this.domain, nullValue());
            this.domain = domain;
            return this;
        }

        @Override
        public DecisionClosure<Key, Score, NoSql> research(ResearchBody<Key, ArcClosure<NoSql>> closure) {
            require(this.body, nullValue());
            this.body = closure;
            return this;
        }

        @Override
        public DecisionClosure<Key, Score, NoSql> resultEvaluator(Function<ReadStorage<NoSql>, Score> eval) {
            require(this.evaluator, nullValue());
            this.evaluator = eval;
            return this;
        }

        @Override
        public DecisionClosure<Key, Score, NoSql> scoreComparator(Comparator<Score> comparator) {
            require(this.comparator, nullValue());
            this.comparator = comparator;
            return this;
        }
    }

    private static record ParallelNodeArguments<NoSql>(
        @NonNull NodeBody<String, StorytellerDefinition, EpisodeType, NoSql> body,
        @NonNull TaskTree.IncorporationFilter<String, StorytellerDefinition,EpisodeType, NoSql> filter
    ) {}

    private <Key, Score> ParallelNodeArguments<NoSql> decisionToChoiceBody(ActionBody<DecisionClosure<Key, Score, NoSql>> body){
        var closure = new DecisionClosureImpl<Key, Score, NoSql>();
        body.action(closure);
        return new ParallelNodeArguments<>(
            (exec, storage) -> {

                closure
                    .domain.get()
                    .forEach(k ->
                        exec.execute(
                            new StorytellerDefinition("choice option", k),
                            EpisodeType.ARC,
                            (domainExec, domainStorage) -> closure.body.research(k, makeArcClosure(domainExec))
                        )
                    );
            },
            (subtasks, insight) ->
                subtasks
                    .stream()
                    .map(t -> new Pair<>(t, insight.into(t)))
                    .map(p -> p.map1(closure.evaluator::apply))
                    .sorted(
                        (p1, p2) ->
                            closure.comparator.compare(p1.get1(), p2.get1())
                    )
                    .map(Pair::get0)
                    .limit(1)
                    .collect(toSet())
        );
    }

    @Override
    public void tell(String storyName, StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> arcClosure) {
        executor.execute(TaskSpec.of(new StorytellerDefinition(storyName), EpisodeType.STORY), arcToNodeBody(arcClosure));
    }

    @Override
    public Sessions sessions() {
        return executor.getSessions();
    }
}
