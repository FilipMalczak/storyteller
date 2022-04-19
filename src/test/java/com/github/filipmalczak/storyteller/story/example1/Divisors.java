package com.github.filipmalczak.storyteller.story.example1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Divisors {
    @Id
    int x;
    int noOfDivisors;
}
