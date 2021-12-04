package com.github.filipmalczak.storyteller.impl.jgit;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

//todo delete me
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HashingService {
    ObjectMapper objectMapper;

    public HashingService() {
        this.objectMapper = JsonMapper.builder().build()
            .enable(
                MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
            )
            .enable(
                SerializationFeature.INDENT_OUTPUT
            );
        }

    public <T> String encode(String title, T data){
        return hash(canonical(title))+":"+hash(canonical(data));
    }

    public String encode(String title){
        return encode(title, null);
    }

    public <T> String encode(T data){
        return encode("", data);
    }

    @SneakyThrows
    public String hash(String txt){
        final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        final byte[] hashbytes = digest.digest(
            txt.getBytes(StandardCharsets.UTF_8)
        );
        return bytesToHex(hashbytes);
    }

    //https://www.baeldung.com/sha-256-hashing-java
    //mind that we use ...3-256
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String canonical(String txt){
        if (txt == null)
            txt = "";
        return txt.trim().toLowerCase(); //fixme locales, encodings and whatnot
    }

    @SneakyThrows
    public <T> String canonical(T data){
        String txt = (data == null) ? "{}" : objectMapper.writeValueAsString(data);
        return canonical(txt); //fixme locales, encodings and whatnot
    }


}
