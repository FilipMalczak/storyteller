package com.github.filipmalczak.storyteller.impl.stack.data.serialization;

import com.github.filipmalczak.storyteller.impl.stack.data.SessionManager;
import com.github.filipmalczak.storyteller.impl.stack.data.TaskManager;
import com.github.filipmalczak.storyteller.impl.stack.data.model.JournalEntryData;
import com.github.filipmalczak.storyteller.stack.Session;
import com.github.filipmalczak.storyteller.stack.task.Task;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.CatchException;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.DefineSubtask;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.IntegrateSubtask;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.JournalEntry;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.NitriteId;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

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
        if (data.getType() == CATCH){
            return (T) new CatchException(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt(),
                (String) data.getAdditionalFields().get("message"),
                (String) data.getAdditionalFields().get("stackTrace")
            );
        }
        if (data.getType() == DEFINE || data.getType() == INTEGRATE) {
            return (T) resultClass.getConstructor(Session.class, ZonedDateTime.class, Task.class).newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt(),
                (Task) taskManager.getById(data.getAdditionalFields().get("referenced"))
            );
        }
        return (T) resultClass.getConstructor(Session.class, ZonedDateTime.class).newInstance(
            sessionManager.getById(data.getSessionId()),
            data.getHappenedAt()
        );
    }



    public <TaskId> JournalEntryData<TaskId> fromEntry(Task<TaskId, ?, ?> task, JournalEntry entry){
        return fromEntry(task.getId(), entry);
    }

    public <TaskId> JournalEntryData<TaskId> fromEntry(TaskId taskId, JournalEntry entry){
        Map<String, Object> additional = new HashMap<>();
        if (entry instanceof CatchException){
            additional.put("message", ((CatchException) entry).getMessage());
            additional.put("stackTrace", ((CatchException) entry).getFullStackTrace());
        } else if (entry instanceof DefineSubtask){
            additional.put("referenced", ((DefineSubtask) entry).getDefined().getId());
        } else if (entry instanceof IntegrateSubtask){
            additional.put("referenced", ((IntegrateSubtask) entry).getIntegrated().getId());
        }
        String id = taskId.toString()+"::"+toTimestamp(entry.getHappenedAt());
        return new JournalEntryData<>(
            NitriteId.newId(),
            taskId,
            toType(entry),
            entry.getSession().getId(),
            entry.getHappenedAt(),
            additional
        );
    }
}
