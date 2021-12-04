package com.github.filipmalczak.storyteller.api.storage.envelope;

import lombok.Value;

import java.io.File;

@Value
public class FileEnvelope implements Envelope<File> {
    String id;
    File payload;

    @Override
    public Class<File> getPayloadType() {
        return File.class;
    }

    public String getExtension(){
        if (payload == null)
            return null;
        if (payload.getName().contains("."))
            return payload.getName().substring(payload.getName().indexOf('.')+1);
        return "";
    }
}
