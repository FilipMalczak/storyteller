package com.github.filipmalczak.storyteller.impl.story;

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
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.SequentialNodeBody;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
                exec.chooseBranchToProceed(new StorytellerDefinition(decision), EpisodeType.DECISION, decisionToChoiceBody(body));
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

    private SequentialNodeBody<String, StorytellerDefinition, EpisodeType, NoSql> arcToNodeBody(StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> body){
        return (exec, storage) -> body.action(
            makeArcClosure(exec),
            storage
        );
    }

    private SequentialNodeBody<String, StorytellerDefinition, EpisodeType, NoSql> threadToNodeBody(StructureBody<ThreadClosure<NoSql>, ReadStorage<NoSql>> body){
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

    private <Key, Score> ChoiceBody<String, StorytellerDefinition, EpisodeType, NoSql> decisionToChoiceBody(ActionBody<DecisionClosure<Key, Score, NoSql>> body){
        return (exec, storage, insight) -> {
            var closure = new DecisionClosureImpl<Key, Score, NoSql>();
            body.action(closure);
            var scores = new HashMap<String, Score>();
            return closure
                .domain.get()
                .map(k -> {
                    var keyArc = exec.execute(
                        new StorytellerDefinition("choice option", k),
                        EpisodeType.ARC,
                        (domainExec, domainStorage) -> closure.body.research(k, makeArcClosure(domainExec))
                    );
                    var keyStorageInsight = insight.into(keyArc);
                    var score = closure.evaluator.apply(keyStorageInsight);
                    scores.put(keyArc.getId(), score);
                    return keyArc;
                })
                .sorted(
                    (t1, t2) ->
                        closure.comparator
                            .compare(
                                scores.get(t1.getId()),
                                scores.get(t2.getId())
                            )
                )
                .findFirst()
                .get();
        };
    }

    @Override
    public void tell(String storyName, StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> arcClosure) {
        executor.execute(new StorytellerDefinition(storyName), EpisodeType.STORY, arcToNodeBody(arcClosure));
    }

    @Override
    public Sessions sessions() {
        return executor.getSessions();
    }
}
