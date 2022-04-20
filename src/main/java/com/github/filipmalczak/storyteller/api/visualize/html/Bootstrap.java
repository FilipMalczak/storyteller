package com.github.filipmalczak.storyteller.api.visualize.html;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Optional;

import static com.github.filipmalczak.storyteller.api.visualize.html.Html.*;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class Bootstrap {
    public static Renderable badge(String name, boolean pill, String flavour){
        var classes = new ArrayList<String>();
        classes.add("badge");
        if (pill)
            classes.add("badge-pill");
        if (flavour != null)
            classes.add("badge-"+flavour);
        return node("span", cssClass(classes), literal(name));
    }
}
