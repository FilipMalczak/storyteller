package com.github.filipmalczak.storyteller.api.story;

public class ToBeContinuedException extends RuntimeException {
    public ToBeContinuedException() {
    }

    public ToBeContinuedException(String message) {
        super(message);
    }

    public static void toBeContinued(){
        throw new ToBeContinuedException("This part of the story hasn't been written yet");
    }

    public static void toBeContinued(String msg){
        throw new ToBeContinuedException(msg);
    }
}
