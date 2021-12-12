package com.github.filipmalczak.storyteller.api.story;

public class ToBeContinuedException extends RuntimeException {
    public ToBeContinuedException() {
    }

    public ToBeContinuedException(String message) {
        super(message);
    }

    public ToBeContinuedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final Runnable NO_OP_SALVEGER = () -> {};

    private static final String DEFAULT_MSG = "This part of the story hasn't been written yet";
    private static final Runnable DEFAULT_SALVAGER = NO_OP_SALVEGER;

    public static void toBeContinued(){
        toBeContinued(DEFAULT_MSG, DEFAULT_SALVAGER);
    }

    public static void toBeContinued(String msg){
        toBeContinued(msg, DEFAULT_SALVAGER);
    }

    public static void toBeContinued(Runnable sealTheSalvage){
        toBeContinued(DEFAULT_MSG, sealTheSalvage);
    }

    public static void toBeContinued(String msg, Runnable sealTheSalvage){
        ToBeContinuedException e  =  null;
        try {
            sealTheSalvage.run();
            e = new ToBeContinuedException(msg);
        } catch (Exception exception) {
            e = new ToBeContinuedException(msg, exception);
        }
        throw e;
    }
}
