package com.github.filipmalczak.storyteller.impl.storage.files.indexing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;

import java.time.ZonedDateTime;

//this entity is exposed only to internals of implementation, so we dont need a DTO
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class ModificationEvent<Id extends Comparable<Id>> {
    @org.dizitart.no2.objects.Id
    NitriteId id;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    @NonNull Id scope;
    @NonNull String path;
    @NonNull Modification type;
    @NonNull ZonedDateTime occuredAt;
}
