package com.github.filipmalczak.storyteller.impl.tree.internal.data.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntryData<TaskId extends Comparable<TaskId>> {
    @Id
    String id;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    TaskId taskId;
    EntryType type;
    String sessionId;
    ZonedDateTime happenedAt;
    Map<String, Object> additionalFields;
}
