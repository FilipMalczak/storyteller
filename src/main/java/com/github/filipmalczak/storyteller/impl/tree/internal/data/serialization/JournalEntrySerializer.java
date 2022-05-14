package com.github.filipmalczak.storyteller.impl.tree.internal.data.serialization;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdSerializer;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.ExceptionCaught;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesModifiedSubtasks;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesSingleTask;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesTasks;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.SessionManager;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.TaskManager;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.JournalEntryData;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType.*;
import static com.github.filipmalczak.storyteller.impl.TimeUtils.toTimestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Flogger
public class JournalEntrySerializer<Id extends Comparable<Id>> {
    @NonNull SessionManager sessionManager;
    @NonNull TaskManager<Id, ?, ?> taskManager;

    @SneakyThrows
    private ExceptionCaught deserializeExceptionCaught(JournalEntryData data){
        return ExceptionCaught.class
                .getConstructor(Session.class, ZonedDateTime.class, String.class, String.class, String.class)
                .newInstance(
                    sessionManager.getById(data.getSessionId()),
                    data.getHappenedAt(),
                    (String) data.getAdditionalFields().get("className"),
                    (String) data.getAdditionalFields().get("message"),
                    (String) data.getAdditionalFields().get("stackTrace")
                );
    }

    @SneakyThrows
    private <T extends JournalEntry> T deserializeRefAndList(Class<? extends JournalEntry> clazz, JournalEntryData<Id> data){

        return (T) clazz
            .getConstructor(Session.class, ZonedDateTime.class, Comparable.class, List.class)
            .newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt(),
                (Id) data.getAdditionalFields().get("reference"),
                (List<Id>) data.getAdditionalFields().get("references")
            );
    }

    @SneakyThrows
    private <T extends JournalEntry> T deserializeRef(Class<? extends JournalEntry> clazz, JournalEntryData<Id> data){
        log.atInfo().log("Im here");
        return (T) clazz
            .getConstructor(Session.class, ZonedDateTime.class, Comparable.class)
            .newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt(),
                (Id) data.getAdditionalFields().get("reference")
            );
    }

    @SneakyThrows
    private <T extends JournalEntry> T deserializeList(Class<? extends JournalEntry> clazz, JournalEntryData<Id> data){

        return (T) clazz
            .getConstructor(Session.class, ZonedDateTime.class, List.class)
            .newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt(),
                (List<Id>) data.getAdditionalFields().get("references")
            );
    }

    @SneakyThrows
    private <T extends JournalEntry> T deserializeTwoLists(Class<T> clazz, JournalEntryData<Id> data){

        return (T) clazz
            .getConstructor(Session.class, ZonedDateTime.class, List.class, List.class)
            .newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt(),
                (List<Id>) data.getAdditionalFields().get("increment"),
                (List<Id>) data.getAdditionalFields().get("decrement")
            );
    }

    @SneakyThrows
    private <T extends JournalEntry> T deserializeBase(Class<T> clazz, JournalEntryData<Id> data){

        return (T) clazz
            .getConstructor(Session.class, ZonedDateTime.class)
            .newInstance(
                sessionManager.getById(data.getSessionId()),
                data.getHappenedAt()
            );
    }

    @SneakyThrows
    public <T extends JournalEntry> T toEntry(JournalEntryData data){
        Class<T> resultClass = (Class<T>) data.getType().getEntryClass();
        if (ExceptionCaught.class.isAssignableFrom(resultClass)) {
            return (T) deserializeExceptionCaught(data);
        }
        if (ReferencesSingleTask.class.isAssignableFrom(resultClass)) {
            if (ReferencesTasks.class.isAssignableFrom(resultClass)) {
                return (T) deserializeRefAndList(data.getType().getEntryClass(), data);
            } else {
                return (T) deserializeRef(data.getType().getEntryClass(), data);
            }
        }
        if (ReferencesTasks.class.isAssignableFrom(resultClass)) {
            if (ReferencesModifiedSubtasks.AddedAndRemoved.class.isAssignableFrom(resultClass)) {
                return (T) deserializeTwoLists(resultClass, data);
            } else {
                return (T) deserializeList(resultClass, data);
            }
        }
        return (T) deserializeBase(resultClass, data);
    }

    public JournalEntryData<Id> fromEntry(Task<Id, ?, ?> task, JournalEntry entry){
        return fromEntry(task.getId(), entry);
    }

    public JournalEntryData<Id> fromEntry(Id taskId, JournalEntry entry){
        Map<String, Object> additional = new HashMap<>();
        var type = toType(entry);
        if (ExceptionCaught.class.isAssignableFrom(type.getEntryClass())){
            additional.put("className", ((ExceptionCaught) entry).getClassName());
            additional.put("message", ((ExceptionCaught) entry).getMessage());
            additional.put("stackTrace", ((ExceptionCaught) entry).getFullStackTrace());
        } else {
            if (ReferencesSingleTask.class.isAssignableFrom(type.getEntryClass())) {
                additional.put("reference", ((ReferencesSingleTask) entry).getReference());
            }
            if (ReferencesTasks.class.isAssignableFrom(type.getEntryClass())) {
                if (ReferencesModifiedSubtasks.AddedAndRemoved.class.isAssignableFrom(type.getEntryClass())) {
                    additional.put("increment", ((ReferencesModifiedSubtasks.AddedAndRemoved) entry).getIncrement().toList());
                    additional.put("decrement", ((ReferencesModifiedSubtasks.AddedAndRemoved) entry).getDecrement().toList());
                } else {
                    additional.put("references", ((ReferencesTasks<Id>) entry).getReferences().toList());
                }
            }
        }
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
