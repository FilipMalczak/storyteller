package com.github.filipmalczak.storyteller.impl.jgit.utils;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import lombok.NonNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RefNames {
    public static final String DEFINE = "define"; //todo change to define-in, add parent id to every usage
    public static final String START = "start";
    public static final String PROGRESS = "progress";
    public static final String RUN = "run";
    public static final String END = "end";
    public static final String INTEGRATE = "integrate-into";
    public static final String RECONCILE = "reconcile-index";


    public static String buildRefName(@NonNull EpisodeId id, @NonNull String domain, Object... subparts){
        return buildRefName(id.toString(), domain, subparts);
    }

    public static String buildRefName(@NonNull String id, @NonNull String domain, Object... subparts){
        return Stream.concat(
                Stream.of(id, domain),
                Stream.of(subparts).map(x -> x.toString())
            )
            .collect(Collectors.joining("/"));
    }
    //todo hide constructor, etc; ditto with other utils


}
