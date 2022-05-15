package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.StorytellerFactory;
import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.tree.NitriteTaskTreeFactory;
import com.github.filipmalczak.storyteller.impl.tree.config.NitriteTreeConfig;
import org.dizitart.no2.Nitrite;

import java.nio.file.Path;

import static java.util.Arrays.asList;

//fixme placing it here will get tricky when we'll be splitting this into modules
public class NitriteStorytellerFactory implements StorytellerFactory<Nitrite, Path> {

    @Override
    public Storyteller<Nitrite> create(Path dataPath) {
        return new TreeStorytellerFactory()
            .create(
                new NitriteTaskTreeFactory<String, StorytellerDefinition, EpisodeType>()
                    .create(
                        NitriteTreeConfig.of(
                            new NitriteStorageConfig<>(dataPath, s -> s),
                            new StorytellerIdGeneratorFactory<EpisodeType>(),
                            node -> TaskSpec.of(new StorytellerDefinition("merge", asList(node.getDefinition(), node.getId())), EpisodeType.SCENE)
                        )
                    )
            );
    }
}
