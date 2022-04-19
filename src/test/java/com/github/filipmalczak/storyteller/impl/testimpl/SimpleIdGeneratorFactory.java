package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;

import static java.lang.Math.min;

public class SimpleIdGeneratorFactory<Type extends Enum<Type> & TaskType> implements IdGeneratorFactory<String, String, Type> {
    @Override
    public IdGenerator<String, String, Type> over(String definition, Type episodeType) {
        String prefix = episodeType.toString()+
            "_"+
            definition
                .replaceAll("([^\\w\\d-]+)", "_")
                .replaceAll("[_]+", "_")
                .substring(0, min(32, definition.length()))+
            "_"+
            definition.hashCode();
        return new IdGenerator<>() {
            @Override
            public String definition() {
                return definition;
            }

            @Override
            public Type type() {
                return episodeType;
            }
            @Override
            public String generate() {
                if (type().isRoot())
                    return prefix;
                return prefix+
                    "_"+
                    System.currentTimeMillis();
            }

            @Override
            public boolean canReuse(String s) {
                if (type().isRoot())
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
