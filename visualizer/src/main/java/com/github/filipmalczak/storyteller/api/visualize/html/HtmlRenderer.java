package com.github.filipmalczak.storyteller.api.visualize.html;

public interface HtmlRenderer<T> {
    String renderHtml(T value);

    default Renderable renderable(T value){
        return () -> renderHtml(value);
    }

    public static <T extends Renderable> HtmlRenderer<T> nativeRenderer(Class<T> clazz){
        return t -> t.renderHtml();
    }

//    public static <T extends Renderable> HtmlRenderer<T> nativeOf(T renderable){
//        return renderable
//    }

    public static <T> HtmlRenderer<T> asString(){
        return t -> t.toString();
    }
}
