package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.impl.stack.data.model.JournalEntryData;
import com.github.filipmalczak.storyteller.impl.stack.data.model.SessionData;
import com.github.filipmalczak.storyteller.impl.stack.data.model.TaskData;
import com.github.filipmalczak.storyteller.impl.stack.data.model.TaskHistoryData;
import com.github.filipmalczak.storyteller.impl.stack.data.serialization.JournalEntrySerializer;
import com.github.filipmalczak.storyteller.impl.stack.data.serialization.TaskSerializer;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
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
    @NonNull HistoryManager<Id> historyManager;

    public NitriteManagers(Nitrite nitrite) {
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

        NitriteHistoryManager<Id> history = new NitriteHistoryManager<>();
        history.setRepository(nitrite.getRepository(TaskHistoryData.class));
        history.setSessionManager(session);

        this.taskManager = task;
        this.sessionManager = session;
        this.journalEntryManager = journal;
        this.historyManager = history;
    }
}
