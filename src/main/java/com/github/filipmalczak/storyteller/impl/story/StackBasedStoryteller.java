package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.body.ActionBody;
import com.github.filipmalczak.storyteller.api.story.body.StructureBody;
import com.github.filipmalczak.storyteller.api.story.closure.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.closure.ThreadClosure;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class StackBasedStoryteller<NoSql> implements Storyteller<NoSql> {
    StackedExecutor<String, String, EpisodeType, NoSql> executor;

    private NodeBody<String, String, EpisodeType, NoSql> arcToNodeBody(StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> body){
        return (exec, storage) -> body.action(
            new ArcClosure<>() {
                @Override
                public void thread(String threadName, StructureBody<ThreadClosure<NoSql>, ReadStorage<NoSql>> body) {
                    exec.execute(threadName, EpisodeType.THREAD, threadToNodeBody(body));
                }

                @Override
                public void arc(String arcName, StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> body) {
                    exec.execute(arcName, EpisodeType.ARC, arcToNodeBody(body));
                }
            },
            storage
        );
    }

    private NodeBody<String, String, EpisodeType, NoSql> threadToNodeBody(StructureBody<ThreadClosure<NoSql>, ReadStorage<NoSql>> body){
        //stop IDE from turning to lambda for consistency
        //noinspection Convert2Lambda
        return (exec, storage) -> body.action(
            new ThreadClosure<>() {
                @Override
                public void scene(String name, ActionBody<ReadWriteStorage<NoSql>> body) {
                    exec.execute(name, EpisodeType.SCENE, sceneToLeafBody(body));
                }
            },
            storage
        );
    }

    private LeafBody sceneToLeafBody(ActionBody<ReadWriteStorage<NoSql>> body){
        //keep this method for consistency
        return storage -> body.action(storage);
    }

    @Override
    public void tell(String storyName, StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> arcClosure) {
        executor.execute(storyName, EpisodeType.STORY, arcToNodeBody(arcClosure));
    }
}
