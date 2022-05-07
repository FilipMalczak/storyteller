package com.github.filipmalczak.storyteller.impl.tree.internal.executor;

import com.github.filipmalczak.storyteller.api.session.Sessions;
import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;
import lombok.Value;
import org.dizitart.no2.Nitrite;

import java.util.Map;

@Value
public class RootAdapter<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskTreeRoot<Id, Definition, Type, Nitrite> {
    Sessions sessions;
    TaskExecutor<Id, Definition, Type, Nitrite> executor;

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        return executor.executeSequentialNode(definition, type, body, new TaskExecutor.Callback<Id, Definition, Type>() {
            @Override
            public void beforeRunning(Task<Id, Definition, Type> finished) {

            }

            @Override
            public void onFinished(Task<Id, Definition, Type> finished, Map<Id, HistoryDiff<Id>> increment) {

            }
        });
    }
}
