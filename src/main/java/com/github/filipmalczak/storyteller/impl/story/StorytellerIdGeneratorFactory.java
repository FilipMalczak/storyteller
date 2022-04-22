package com.github.filipmalczak.storyteller.impl.story;

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
        @NonNull StorytellerDefinition definition;
        @NonNull Type type;

        @Getter(lazy = true) String prefix = makePrefix();

        protected String makePrefix(){
            return type.toString()+
                "_"+
                definition.getName()
                    .replaceAll("([^\\w\\d-]+)", "_")
                    .replaceAll("[_]+", "_")
                    .substring(0, min(32, definition.getName().length()))+
                "_"+
                definition.getName().hashCode();
        }

        @Override
        public StorytellerDefinition definition() {
            return this.definition;
        }

        @Override
        public Type type() {
            return this.type;
        }

        @Override
        public String generate() {
            if (type.isRoot())
                return getPrefix();
            return getPrefix()+"_"+System.currentTimeMillis();
        }

        @Override
        public boolean canReuse(String s) {
            var prefix = getPrefix();
            if (type().isRoot())
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
        public WithKeyGenerator(@NonNull StorytellerDefinition definition, @NonNull Type type) {
            super(definition, type);
        }

        @Override
        protected String makePrefix() {
            var keyString = definition().getKey().toString();
            return super.makePrefix()+"_"+keyString.substring(0, min(32, keyString.length()))+"_"+definition().getKey().hashCode();
        }
    }

    @Override
    public IdGenerator<String, StorytellerDefinition, Type> over(StorytellerDefinition definition, Type type) {
        if (definition.getKey() == null)
            return new JustNameGenerator<>(definition, type);
        return new WithKeyGenerator<>(definition, type);
    }
}
