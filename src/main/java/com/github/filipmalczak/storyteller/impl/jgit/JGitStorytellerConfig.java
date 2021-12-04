package com.github.filipmalczak.storyteller.impl.jgit;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.io.File;

@Value
@Builder
@Accessors(chain = true)
public class JGitStorytellerConfig {
    @Builder.Default
    File storyRoot = new File("./story/trace");
    @Builder.Default
    File tempRoot = new File("./story/tmp");
}
