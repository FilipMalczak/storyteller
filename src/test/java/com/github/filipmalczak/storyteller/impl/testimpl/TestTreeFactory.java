package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.session.listener.LoggingJournalListener;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeFactory;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.ReferencesSubtask;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.tree.NitriteTaskTreeFactory;
import com.github.filipmalczak.storyteller.impl.tree.config.MergeSpec;
import com.github.filipmalczak.storyteller.impl.tree.config.NitriteTreeConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

import java.io.File;
import java.util.function.Predicate;

import static org.apache.commons.io.FileUtils.deleteDirectory;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestTreeFactory implements TaskTreeFactory<String, String, TrivialTaskType, Nitrite, String> {
    String dirName;
    final static NitriteTaskTreeFactory<String, String, TrivialTaskType> BASE_FACTORY = new NitriteTaskTreeFactory<>();
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
    public TaskTreeRoot<String, String, TrivialTaskType, Nitrite> create(String s) {
        var out = new NitriteTaskTreeFactory<String, String, TrivialTaskType>()
            .create(
                NitriteTreeConfig.<String, String, TrivialTaskType>of(
                    forTest(s),
                    GENERATOR_FACTORY,
                    node -> new MergeSpec<>("merge", TrivialTaskType.LEAF)
                )
            );
        out.getSessions().addListener(new LoggingJournalListener<>());
        return out;
    }

    public static <T extends ReferencesSubtask<String>> Predicate<T> entryMatchesSubtask(String def, TrivialTaskType type){
        return e -> GENERATOR_FACTORY.over(def, type).canReuse(e.getReference());
    }

    public static <T extends ReferencesSubtask<String>> Predicate<Pair<Task, T>> matchesSubtask(String def, TrivialTaskType type){
        var forEntry = entryMatchesSubtask(def, type);
        return p -> forEntry.test(p.get1());
    }
}
