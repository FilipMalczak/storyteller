package com.github.filipmalczak.storyteller.impl.tree.internal.data.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntryData<Id extends Comparable<Id>> {
    @org.dizitart.no2.objects.Id
    String id;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    Id taskId;
    EntryType type;
    String sessionId;
    ZonedDateTime happenedAt;
    Map<String, Object> additionalFields;
}
