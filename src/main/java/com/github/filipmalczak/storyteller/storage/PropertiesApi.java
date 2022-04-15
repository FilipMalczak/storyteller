package com.github.filipmalczak.storyteller.storage;

import com.github.filipmalczak.storyteller.storage.envelope.PropertyEnvelope;

import java.util.Optional;

public interface PropertiesApi {
    PropertyEnvelope create(String id, String val);

    boolean exists(String id);

    void delete(String id);

    Optional<PropertyEnvelope> find(String id);

    void save(PropertyEnvelope envelope);
}