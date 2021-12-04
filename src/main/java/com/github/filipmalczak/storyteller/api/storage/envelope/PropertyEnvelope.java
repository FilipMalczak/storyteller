package com.github.filipmalczak.storyteller.api.storage.envelope;

import lombok.NonNull;
import lombok.Value;

@Value
public class PropertyEnvelope implements Envelope<String> {
    String id;
    String payload;

    @Override
    public Class<String> getPayloadType(){
        return String.class;
    }

    public PropertyEnvelope with(@NonNull String payload){
        return new PropertyEnvelope(id, payload);
    }
}
