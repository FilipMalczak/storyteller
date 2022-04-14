package com.github.filipmalczak.storyteller.impl.stack.data.model;

import com.github.filipmalczak.storyteller.stack.task.journal.EntryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntryData<TaskId> {
    @Id
    NitriteId id;
    TaskId taskId;
    EntryType type;
    String sessionId;
    ZonedDateTime happenedAt;
    Map<String, Object> additionalFields;
}
