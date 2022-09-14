package com.github.filipmalczak.storyteller.impl.nextgen;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.filipmalczak.storyteller.api.session.EventType;
import lombok.*;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEventData {
    @Id
    NitriteId id;
    EventType type;
    @NonNull ZonedDateTime happenedAt;
    @NonNull String sessionId;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    Object taskId;
    @Singular("withAdditional")
    @NonNull Map<String, Object> additional;
}
