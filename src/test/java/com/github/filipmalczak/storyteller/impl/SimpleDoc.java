package com.github.filipmalczak.storyteller.impl;

import lombok.*;
import org.dizitart.no2.objects.Id;

import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleDoc {
    @Id
    String id;

    int value;
    List<String> texts;
    boolean bool;
}
