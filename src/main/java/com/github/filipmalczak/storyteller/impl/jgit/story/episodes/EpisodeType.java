package com.github.filipmalczak.storyteller.impl.jgit.story.episodes;

import com.github.filipmalczak.storyteller.impl.jgit.story.Thread;
import com.github.filipmalczak.storyteller.impl.jgit.story.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.Optional;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter
public enum EpisodeType {
     ARC(Arc.class, "arc"),
    SCENE(Scene.class, "scene"),
    STORY(Story.class, "story"),
    THREAD(Thread.class, "thread")
    ; //todo decision is missing

    Class<? extends Episode> backingClosure;
    String idPrefix; //must be already trimmed

    public static Optional<EpisodeType> findByBackend(@NonNull Class<?> backend){
        return Stream.of(EpisodeType.values()).filter(e -> e.backingClosure.equals(backend)).findAny();
    }

    public static Optional<EpisodeType> findByPrefix(@NonNull String prefix){
        return Stream.of(EpisodeType.values()).filter(e -> e.idPrefix.equals(prefix)).findAny();
    }
}
