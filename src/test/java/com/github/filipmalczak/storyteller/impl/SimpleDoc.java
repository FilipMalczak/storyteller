package com.github.filipmalczak.storyteller.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

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
