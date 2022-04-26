package com.github.filipmalczak.storyteller.impl.visualize.html;

import com.github.filipmalczak.storyteller.api.visualize.html.Renderable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class Html {
    @Value
    public static class Attribute {
        String name;
        String value;
    }

    //todo string overload to save one stack frame
    public static Attribute attr(String name, Object val){
        return new Attribute(name, val.toString());
    }

    public static Attribute cssClass(List<String> classes){
        return attr("class", classes.stream().collect(Collectors.joining(" ")));
    }

    public static Attribute cssClass(String... classes){
        return cssClass(asList(classes));
    }

    public static Renderable empty(){
        return literal("");
    }

    public static Renderable node(String tag, Attribute attribute, Renderable content){
        return node(tag, content, attribute);
    }

    public static Renderable node(String tag, Renderable content, Attribute... attributes){
        var opening = tag+" ";
        for (var a: attributes)
            opening += a.name+"=\""+a.value+"\"";
        var finalOpening = opening;
        return () ->  "<"+finalOpening+">" + content.renderHtml() + "</"+tag+">";
    }

    public static Renderable literal(String literal){
        return () -> literal;
    }

    public static Renderable sequence(Renderable... renderables){
        return sequence(asList(renderables));
    }
    public static Renderable sequence(List<Renderable> renderables){
        return () -> renderables.stream().map(Renderable::renderHtml).collect(Collectors.joining());
    }

    public interface NodeClosure {
        Renderable withContent(Renderable content);
    }

    public static NodeClosure node(String tag, Attribute... attributes){
        return c -> node(tag, c, attributes);
    }

    @SneakyThrows
    public static void render(File target, Renderable renderable){
        try (var writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(target)))){
            writer.print(renderable.renderHtml());
        }
    }
}
