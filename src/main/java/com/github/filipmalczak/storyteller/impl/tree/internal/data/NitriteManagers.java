package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.JournalEntryData;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.SessionData;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.TaskData;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.serialization.JournalEntrySerializer;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.serialization.TaskSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NitriteManagers<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    @NonNull TaskManager<Id, Definition, Type> taskManager;
    @NonNull SessionManager sessionManager;
    @NonNull JournalEntryManager<Id> journalEntryManager;
    @NonNull Nitrite nitrite;

    public NitriteManagers(Nitrite nitrite) {
        this.nitrite = nitrite;

        JournalEntrySerializer journalEntrySerializer = new JournalEntrySerializer();

        NitritieJournalManager<Id> journal = new NitritieJournalManager<>();
        journal.setRepository(nitrite.getRepository(JournalEntryData.class));
        journal.setSerializer(journalEntrySerializer);

        TaskSerializer taskSerializer = new TaskSerializer();
        taskSerializer.setJournalEntryManager(journal);

        NitriteTaskManager<Id, Definition, Type> task = new NitriteTaskManager<>();
        task.setRepository(nitrite.getRepository(TaskData.class));
        task.setSerializer(taskSerializer);
        task.setJournalEntryManager(journal);

        taskSerializer.setTaskManager(task);

        NitriteSessionManager session = new NitriteSessionManager();
        session.setRepository(nitrite.getRepository(SessionData.class));

        journalEntrySerializer.setTaskManager(task);
        journalEntrySerializer.setSessionManager(session);

        journal.setSessionManager(session);

        this.taskManager = task;
        this.sessionManager = session;
        this.journalEntryManager = journal;
    }
}
