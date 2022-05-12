package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static java.lang.Math.min;

public class StorytellerIdGeneratorFactory<Type extends Enum<Type> & TaskType> implements IdGeneratorFactory<String, StorytellerDefinition, Type> {

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    private static class JustNameGenerator<Type extends Enum<Type> & TaskType> implements IdGenerator<String, StorytellerDefinition, Type> {
        @Getter @NonNull TaskSpec<StorytellerDefinition, Type> spec;

        @Getter(lazy = true) String prefix = makePrefix();

        protected String makePrefix(){
            return spec.getType().toString()+
                "_"+
                spec.getDefinition().getName()
                    .replaceAll("([^\\w\\d-]+)", "_")
                    .replaceAll("[_]+", "_")
                    .substring(0, min(32, spec.getDefinition().getName().length()))+
                "_"+
                spec.getDefinition().getName().hashCode();
        }

        @Override
        public String generate() {
            if (spec.getType().isRoot())
                return getPrefix();
            return getPrefix()+"_"+System.currentTimeMillis();
        }

        @Override
        public boolean canReuse(String s) {
            var prefix = getPrefix();
            if (spec.getType().isRoot())
                return prefix.equals(s);
            if (!s.startsWith(getPrefix()))
                return false;
            try {
                Long.parseLong(s.replace(prefix+"_", ""));
            } catch (NumberFormatException e){
                return false;
            }
            return true;
        }
    }

    private static class WithKeyGenerator<Type extends Enum<Type> & TaskType> extends JustNameGenerator<Type> {
        public WithKeyGenerator(@NonNull TaskSpec<StorytellerDefinition, Type> spec) {
            super(spec);
        }

        @Override
        protected String makePrefix() {
            var keyString = getSpec().getDefinition().getKey().toString();
            return super.makePrefix()+"_"+keyString.substring(0, min(32, keyString.length()))+"_"+getSpec().getDefinition().getKey().hashCode();
        }
    }

    @Override
    public IdGenerator<String, StorytellerDefinition, Type> over(TaskSpec<StorytellerDefinition, Type> taskSpec) {
        if (taskSpec.getDefinition().getKey() == null)
            return new JustNameGenerator<>(taskSpec);
        return new WithKeyGenerator<>(taskSpec);
    }
}
