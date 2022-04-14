package com.github.filipmalczak.storyteller.storage.envelope;

public sealed interface Envelope<Payload> permits DocumentEnvelope, FileEnvelope, PropertyEnvelope {
    String getId();
    Payload getPayload();
    Class<Payload> getPayloadType();

    default String  getParametersSummary(){
        return "id: "+getId()+", payload.type: "+getPayloadType().getCanonicalName();
    }

    static String toString(Envelope<?> e){
        return e.getClass().getSimpleName()+"("+e.getParametersSummary()+")";
    }
}
