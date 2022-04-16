package com.github.filipmalczak.storyteller.impl.stack.data.serialization;

import com.github.filipmalczak.storyteller.impl.stack.data.SessionManager;
import com.github.filipmalczak.storyteller.impl.stack.data.TaskManager;
import com.github.filipmalczak.storyteller.impl.stack.data.model.JournalEntryData;
import com.github.filipmalczak.storyteller.stack.Session;
import com.github.filipmalczak.storyteller.stack.task.Task;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.ExceptionCaught;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.ReferencesSubtask;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.filipmalczak.storyteller.impl.TimeUtils.toTimestamp;
import static com.github.filipmalczak.storyteller.stack.task.journal.EntryType.*;

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
                .getConstructor(Session.class, ZonedDateTime.class, String.class, String.class)
                .newInstance(
                    sessionManager.getById(data.getSessionId()),
                    data.getHappenedAt(),
                    (String) data.getAdditionalFields().get("message"),
                    (String) data.getAdditionalFields().get("stackTrace")
                );
        }
        if (referencesSubtask(data.getType())) {
            return (T) resultClass
                .getConstructor(Session.class, ZonedDateTime.class, Task.class)
                .newInstance(
                    sessionManager.getById(data.getSessionId()),
                    data.getHappenedAt(),
                    (Task) taskManager.getById(data.getAdditionalFields().get("referenced"))
                );
        }
        return (T) resultClass
            .getConstructor(Session.class, ZonedDateTime.class)
            .newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt()
            );
    }

    public <TaskId extends Comparable<TaskId>> JournalEntryData<TaskId> fromEntry(Task<TaskId, ?, ?> task, JournalEntry entry){
        return fromEntry(task.getId(), entry);
    }

    public <TaskId extends Comparable<TaskId>> JournalEntryData<TaskId> fromEntry(TaskId taskId, JournalEntry entry){
        Map<String, Object> additional = new HashMap<>();
        var type = toType(entry);
        if (describesException(type)){
            additional.put("message", ((ExceptionCaught) entry).getMessage());
            additional.put("stackTrace", ((ExceptionCaught) entry).getFullStackTrace());
        } else if (referencesSubtask(type)){
            additional.put("referenced", ((ReferencesSubtask) entry).getReferenced().getId());
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
