package com.github.filipmalczak.storyteller.impl.visualize.html;

import com.github.filipmalczak.storyteller.api.visualize.html.Renderable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

import static com.github.filipmalczak.storyteller.impl.visualize.html.Html.*;

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
