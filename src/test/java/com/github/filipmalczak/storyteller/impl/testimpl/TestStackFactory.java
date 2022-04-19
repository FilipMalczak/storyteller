package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.StackedExecutorFactory;
import com.github.filipmalczak.storyteller.impl.stack.NitriteStackConfig;
import com.github.filipmalczak.storyteller.impl.stack.NitriteStackedExecutorFactory;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
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
    final static StandardIdGeneratorFactory<TrivialTaskType> GENERATOR_FACTORY = new StandardIdGeneratorFactory<>();

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
        return BASE_FACTORY.create(NitriteStackConfig.of(forTest(s), GENERATOR_FACTORY));
    }
}
