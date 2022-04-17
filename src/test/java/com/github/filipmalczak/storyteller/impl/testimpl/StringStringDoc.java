package com.github.filipmalczak.storyteller.impl.testimpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StringStringDoc {
    @Id
    String id;
    String txt;
}
