package com.github.filipmalczak.storyteller.impl.tree.internal.data.serialization;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.ExceptionCaught;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.ReferencesSubtasks;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.SessionManager;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.TaskManager;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.JournalEntryData;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType.*;
import static com.github.filipmalczak.storyteller.impl.TimeUtils.toTimestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
public class JournalEntrySerializer {
    @NonNull SessionManager sessionManager;
    @NonNull TaskManager taskManager;

    @SneakyThrows
    public <T extends JournalEntry> T toEntry(JournalEntryData data){
        Class<T> resultClass = (Class<T>) data.getType().getEntryClass();
        if (describesException(data.getType())){
            return (T) resultClass
                .getConstructor(Session.class, ZonedDateTime.class, String.class, String.class, String.class)
                .newInstance(
                    sessionManager.getById(data.getSessionId()),
                    data.getHappenedAt(),
                    (String) data.getAdditionalFields().get("className"),
                    (String) data.getAdditionalFields().get("message"),
                    (String) data.getAdditionalFields().get("stackTrace")
                );
        }
        if (referencesSubtask(data.getType())) {
            return (T) resultClass
                .getConstructor(Session.class, ZonedDateTime.class, Comparable.class)
                .newInstance(
                    sessionManager.getById(data.getSessionId()),
                    data.getHappenedAt(),
                    ((List) data.getAdditionalFields().get("references")).get(0)
                );
        }
        if (referencesSubtasks(data.getType())) {
            return (T) resultClass
                .getConstructor(Session.class, ZonedDateTime.class, List.class)
                .newInstance(
                    sessionManager.getById(data.getSessionId()),
                    data.getHappenedAt(),
                    data.getAdditionalFields().get("references")
                );
        }
        return (T) resultClass
            .getConstructor(Session.class, ZonedDateTime.class)
            .newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt()
            );
    }

    public <Id extends Comparable<Id>> JournalEntryData<Id> fromEntry(Task<Id, ?, ?> task, JournalEntry entry){
        return fromEntry(task.getId(), entry);
    }

    public <Id extends Comparable<Id>> JournalEntryData<Id> fromEntry(Id taskId, JournalEntry entry){
        Map<String, Object> additional = new HashMap<>();
        var type = toType(entry);
        if (describesException(type)){
            additional.put("className", ((ExceptionCaught) entry).getClassName());
            additional.put("message", ((ExceptionCaught) entry).getMessage());
            additional.put("stackTrace", ((ExceptionCaught) entry).getFullStackTrace());
        } else if (referencesSubtasks(type)){
            additional.put("references", ((ReferencesSubtasks) entry).getReferences());
        }
        String id = taskId.toString()+"::"+toTimestamp(entry.getHappenedAt());
        return new JournalEntryData<>(
            UUID.randomUUID().toString(),
            taskId,
            type,
            entry.getSession().getId(),
            entry.getHappenedAt(),
            additional
        );
    }
}
