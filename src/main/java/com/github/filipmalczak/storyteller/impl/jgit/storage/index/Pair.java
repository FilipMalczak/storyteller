package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import lombok.*;

@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor
public class Pair<T, U> {
    @NonNull T first;
    @NonNull U second;
}
