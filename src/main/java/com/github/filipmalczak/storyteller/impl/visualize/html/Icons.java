package com.github.filipmalczak.storyteller.impl.visualize.html;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.visualize.html.Renderable;

import static com.github.filipmalczak.storyteller.impl.visualize.html.Bootstrap.badge;
import static com.github.filipmalczak.storyteller.impl.visualize.html.Html.*;
import static java.lang.System.arraycopy;

public class Icons {
    final static String STYLE_SUFFIX = "-sharp"; //todo alternatives are "" and "-outline"

    public static Renderable icon(String name, Attribute... attributes){
        Attribute[] finalAttributes = new Attribute[attributes.length+1];
        finalAttributes[0] = attr("name", name+STYLE_SUFFIX);
        arraycopy(attributes, 0, finalAttributes, 1, attributes.length);
        return node("ion-icon", empty(), finalAttributes);
    }

    public static Renderable iconForTypeModifier(TaskType.TaskTypeModifier modifier){
        return badge(
            (switch (modifier) {
                case ROOT -> icon("play-circle");
                case NONE -> icon("layers");
                case PARALLEL -> icon("git-branch");
                case LEAF -> icon("leaf");
            }).renderHtml(),
            true,
            "primary"
        );
    }
}
