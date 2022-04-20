package com.github.filipmalczak.storyteller.api.visualize.html;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;

import static com.github.filipmalczak.storyteller.api.visualize.html.Bootstrap.badge;
import static com.github.filipmalczak.storyteller.api.visualize.html.Html.*;
import static com.github.filipmalczak.storyteller.api.visualize.html.Html.empty;

public class Icons {
    public static Renderable iconForTypeModifier(TaskType.TaskTypeModifier modifier){
        return badge(
            (switch (modifier) {
                case ROOT -> node("ion-icon", attr("name", "play-circle-sharp"), empty());
                case NONE -> node("ion-icon", attr("name", "layers-sharp"), empty());
                case CHOICE -> node("ion-icon", attr("name", "git-branch-sharp"), empty());
                case LEAF -> node("ion-icon", attr("name", "leaf-sharp"), empty());
            }).renderHtml(),
            true,
            "primary"
        );
    }
}
