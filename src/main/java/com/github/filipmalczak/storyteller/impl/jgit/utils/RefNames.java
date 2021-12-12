package com.github.filipmalczak.storyteller.impl.jgit.utils;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RefNames {
    public static final String DEFINE = "define-in";
    public static final String START = "start";
    public static final String PROGRESS = "progress";
    public static final String RUN = "run";
    public static final String END = "end";
    public static final String INTEGRATE = "integrate-into";

    public static String buildRefName(EpisodeId id, String domain, Object... subparts){
        return buildRefName(id.toString(), domain, subparts);
    }

    public static String buildRefName(String id, String domain, Object... subparts){
        return Stream.concat(
                Stream.of(id, domain),
                Stream.of(subparts).map(x -> x.toString())
            )
            .collect(Collectors.joining("/"));
    }
    //todo hide constructor, etc; ditto with other utils


}
