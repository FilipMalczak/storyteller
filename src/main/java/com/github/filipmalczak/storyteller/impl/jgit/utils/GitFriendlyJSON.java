package com.github.filipmalczak.storyteller.impl.jgit.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GitFriendlyJSON {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> String serialize(T val){
        return gson.toJson(val);
    }

    public static <T> T deserialize(String json, Class<T> as){
        return gson.fromJson(json, as);
    }

//    private static final ObjectMapper mapper = JsonMapper.builder()
//        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
////        .configure(MapperFeature.USE_STATIC_TYPING, true)
//        .build();
//
//    @SneakyThrows
//    public static <T> String serialize(T value){
//        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
//////            .denyForExactBaseType(Collection.class)
//////            .denyForExactBaseType(Map.class)
////            .denyForExactBaseType(AbstractMap.class)
////            .denyForExactBaseType(AbstractList.class)
//////            .denyForExactBaseType(List.class)
//            .build();
//        return mapper
//            .activateDefaultTyping(
//                ptv
////                ,
////                ObjectMapper.DefaultTyping.NON_FINAL,
////                JsonTypeInfo.As.PROPERTY
//            )
//            .writerWithDefaultPrettyPrinter()
//            .writeValueAsString(value);
//    }
//
//    @SneakyThrows
//    public static <T> T deserialize(String txt, Class<T> as){
//        return mapper.reader().readValue(txt, as);
//    }
}
