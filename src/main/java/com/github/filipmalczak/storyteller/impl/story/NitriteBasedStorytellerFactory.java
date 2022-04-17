package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.StorytellerFactory;
import com.github.filipmalczak.storyteller.impl.stack.NitriteStackConfig;
import com.github.filipmalczak.storyteller.impl.stack.NitriteStackedExecutorFactory;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import org.dizitart.no2.Nitrite;

import java.nio.file.Path;

//fixme placing it here will get tricky when we'll be splitting this into modules
public class NitriteBasedStorytellerFactory implements StorytellerFactory<Nitrite, Path> {

    @Override
    public Storyteller<Nitrite> create(Path dataPath) {
        return new StackBasedStorytellerFactory()
            .create(
                new NitriteStackedExecutorFactory<String, String, EpisodeType>()
                    .create(
                        NitriteStackConfig.of(
                            new NitriteStorageConfig<>(dataPath, s -> s),
                            new StandardIdGeneratorFactory()
                        )
                    )
            );
    }
}
