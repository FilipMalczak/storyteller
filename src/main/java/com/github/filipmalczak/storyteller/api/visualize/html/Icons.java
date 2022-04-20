package com.github.filipmalczak.storyteller.api.visualize.html;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;

import static com.github.filipmalczak.storyteller.api.visualize.html.Html.*;
import static com.github.filipmalczak.storyteller.api.visualize.html.Html.empty;

public class Icons {
    public static Renderable iconForTypeModifier(TaskType.TaskTypeModifier modifier){
        return switch (modifier) {
            case ROOT -> node("ion-icon", attr("name", "play-circle-outline"), empty());
            case NONE -> node("ion-icon", attr("name", "layers-outline"), empty());
            case CHOICE -> node("ion-icon", attr("name", "git-branch-outline"), empty());
            case LEAF -> node("ion-icon", attr("name", "leaf-outline"), empty());
        };
    }
}
