package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;

import static java.lang.Math.min;

public class SimpleIdGeneratorFactory<Type extends Enum<Type> & TaskType> implements IdGeneratorFactory<String, String, Type> {
    @Override
    public IdGenerator<String, String, Type> over(TaskSpec<String, Type> taskSpec) {
        String prefix = taskSpec.getType().toString()+
            "_"+
            taskSpec.getDefinition()
                .replaceAll("([^\\w\\d-]+)", "_")
                .replaceAll("[_]+", "_")
                .substring(0, min(32, taskSpec.getDefinition().length()))+
            "_"+
            taskSpec.getDefinition().hashCode();
        return new IdGenerator<>() {
            @Override
            public TaskSpec<String, Type> getSpec() {
                return taskSpec;
            }

            @Override
            public String generate() {
                if (taskSpec.getType().isRoot())
                    return prefix;
                return prefix+
                    "_"+
                    System.currentTimeMillis();
            }

            @Override
            public boolean canReuse(String s) {
                if (taskSpec.getType().isRoot())
                    return prefix.equals(s);
                if (!s.startsWith(prefix))
                    return false;
                try {
                    Long.parseLong(s.replace(prefix+"_", ""));
                } catch (NumberFormatException e){
                    return false;
                }
                return true;
            }
        };
    }
}
