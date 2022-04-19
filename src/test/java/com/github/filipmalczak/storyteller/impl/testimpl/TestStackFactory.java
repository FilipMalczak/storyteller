package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.StackedExecutorFactory;
import com.github.filipmalczak.storyteller.impl.stack.NitriteStackConfig;
import com.github.filipmalczak.storyteller.impl.stack.NitriteStackedExecutorFactory;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.story.EpisodeType;
import com.github.filipmalczak.storyteller.impl.story.SimpleDefinition;
import com.github.filipmalczak.storyteller.impl.story.StackBasedStorytellerFactory;
import com.github.filipmalczak.storyteller.impl.story.StandardIdGeneratorFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

import java.io.File;

import static org.apache.commons.io.FileUtils.deleteDirectory;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestStackFactory implements StackedExecutorFactory<String, String, TrivialTaskType, Nitrite, String> {
    String dirName;
    final static NitriteStackedExecutorFactory<String, String, TrivialTaskType> BASE_FACTORY = new NitriteStackedExecutorFactory<>();
    final static SimpleIdGeneratorFactory<TrivialTaskType> GENERATOR_FACTORY = new SimpleIdGeneratorFactory<>();

    @SneakyThrows
    private NitriteStorageConfig<String> forTest(String name) {
        var dir = new File(new File("./test_data/"+ dirName), name).getAbsoluteFile();
        if (dir.exists())
            deleteDirectory(dir);
        dir.mkdirs();
        return new NitriteStorageConfig<>(
            dir.toPath(),
            s -> s
        );
    }

    @Override
    public StackedExecutor<String, String, TrivialTaskType, Nitrite> create(String s) {
        return new NitriteStackedExecutorFactory<String, String, TrivialTaskType>()
                .create(
                    NitriteStackConfig.<String, String, TrivialTaskType>of(
                        forTest(s),
                        GENERATOR_FACTORY
                    )
                );
    }
}
