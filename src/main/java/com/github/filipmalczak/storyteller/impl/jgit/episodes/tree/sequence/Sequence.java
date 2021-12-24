package com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.sequence;

public sealed interface Sequence<T> permits NodeSequence, LeafSequence {
}
