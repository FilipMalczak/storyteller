package com.github.filipmalczak.storyteller.storage.envelope;

import lombok.NonNull;
import lombok.Value;

@Value
public class DocumentEnvelope<Type> implements Envelope<Type> {
    String id;;
    Type payload;
    Class<Type> payloadType;

    public DocumentEnvelope<Type> with(@NonNull Type payload){
        return new DocumentEnvelope<>(id, payload, payloadType);
    }
}
