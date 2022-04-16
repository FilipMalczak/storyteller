package com.github.filipmalczak.storyteller.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocWithDate {
    String id;
    ZonedDateTime dateTime;
}
