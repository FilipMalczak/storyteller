package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.RUN;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;

public class Rgx {
    public static final String HEX = "[0-9a-fA-F]";
    public static final String UUID = "("+HEX+"{8}(-"+HEX+"{4}){3}-"+HEX+"{12})";
    public static final String RUN_MESSAGE = buildRefName(UUID, RUN);
}
