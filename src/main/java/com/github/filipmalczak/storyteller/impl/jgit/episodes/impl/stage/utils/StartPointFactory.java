package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;

import java.util.function.Function;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;

@FunctionalInterface
public interface StartPointFactory extends Function<EpisodeId, String> {
    static StartPointFactory constant(String c){
        return id -> c;
    }

    static StartPointFactory buildRef(String suffix, String... suffixes){
        //noinspection ConfusingArgumentToVarargsMethod
        return id -> buildRefName(id, suffix, suffixes);
    }
}
