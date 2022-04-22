package com.github.filipmalczak.storyteller.impl.story;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Setter(value = AccessLevel.NONE)
public class StorytellerDefinition {
    @NonNull String name;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    Object key = null;

}
