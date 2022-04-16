package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.stack.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;

public class StandardIdGeneratorFactory implements IdGeneratorFactory<String, String, EpisodeType> {
    @Override
    public IdGenerator<String, String, EpisodeType> over(String definition, EpisodeType episodeType) {
        String prefix = episodeType.toString()+
            "_"+
            definition
                .replaceAll("([^\\w\\d-]+)", "_")
                .replaceAll("[_]+", "_")
                .substring(0, 32)+
            "_"+
            definition.hashCode();
        return new IdGenerator<>() {
            @Override
            public String definition() {
                return definition;
            }

            @Override
            public EpisodeType type() {
                return episodeType;
            }
            @Override
            public String generate() {
                return prefix+
                    "_"+
                    System.currentTimeMillis();
            }

            @Override
            public boolean canReuse(String s) {
                if (!s.startsWith(prefix))
                    return false;
                try {
                    Long.parseLong(s.replace(prefix, ""));
                } catch (NumberFormatException e){
                    return false;
                }
                return true;
            }
        };
    }
}
