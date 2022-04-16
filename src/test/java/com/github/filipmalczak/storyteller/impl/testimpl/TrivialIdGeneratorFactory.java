package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.stack.task.IdGenerator;
import com.github.filipmalczak.storyteller.api.stack.task.IdGeneratorFactory;

import static java.lang.Math.min;

public class TrivialIdGeneratorFactory implements IdGeneratorFactory<String, String, TrivialTaskType> {
    @Override
    public IdGenerator<String, String, TrivialTaskType> over(String definition, TrivialTaskType type) {
        if (type.isRoot())
            return new IdGenerator<String, String, TrivialTaskType>() {
                @Override
                public String definition() {
                    return definition;
                }

                @Override
                public TrivialTaskType type() {
                    return type;
                }

                @Override
                public String generate() {
                    return definition;
                }

                @Override
                public boolean canReuse(String s) {
                    return s.equals(definition);
                }
            };
        return new IdGenerator<String, String, TrivialTaskType>() {
            @Override
            public String definition() {
                return definition;
            }

            @Override
            public TrivialTaskType type() {
                return type;
            }

            @Override
            public String generate() {
                return type.toString()+"_"+definition.substring(0, min(10, definition.length()))+"_"+System.currentTimeMillis();
            }

            @Override
            public boolean canReuse(String s) {
                String prefix = type.toString() + "_" + definition.substring(0, min(10, definition.length())) + "_";
                if (!s.startsWith(prefix))
                    return false;
                s = s.replace(prefix, "");
                try {
                    long l = Long.parseLong(s);
                } catch (NumberFormatException e){
                    return false;
                }
                return true;
            }
        };
    }
}
