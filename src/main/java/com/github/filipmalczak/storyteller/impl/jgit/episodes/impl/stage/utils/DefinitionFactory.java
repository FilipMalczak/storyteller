package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import com.google.common.flogger.FluentLogger;

import java.util.function.Function;

import static java.util.Collections.emptyMap;

@FunctionalInterface
public interface DefinitionFactory extends Function<WorkingCopy, EpisodeDefinition> {
    FluentLogger log = FluentLogger.forEnclosingClass();

    DefinitionFactory RETRIEVE_FROM_PARENT_INDEX = DefinitionFactory::retrieveFromParentIndex;

    private static EpisodeDefinition retrieveFromParentIndex(WorkingCopy copy){
        //todo invariant: parent is define of sequence?
        var indexFile = copy
            .getIndexFile();
        var scopeMeta = indexFile
            .getMetadata();
        var scopeIdx = scopeMeta.getOrderedIndex();
        log.atFine().log("Index of scope is %s", scopeIdx);
        var sequenceDef = scopeIdx.get(scopeIdx.size()-1);
        log.atFine().log("Definition of sequence is %s", sequenceDef);
        return sequenceDef;
    }

     static DefinitionFactory constant(EpisodeId id, String name){
        return constant(new EpisodeDefinition(id, new EpisodeSpec(id.getType(), name, emptyMap())));
    }

    static DefinitionFactory constant(EpisodeDefinition c){
        return wc -> c;
    }
}
