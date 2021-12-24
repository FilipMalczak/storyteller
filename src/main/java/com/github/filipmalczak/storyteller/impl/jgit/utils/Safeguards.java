package com.github.filipmalczak.storyteller.impl.jgit.utils;

public class Safeguards {
    public static class Failure extends RuntimeException {
        public Failure(String title) {
            super("Safeguard failure: "+title);
        }
    }

    //fixme: Im reinventing the wheel http://www.valid4j.org/
    public static void invariant(boolean shouldBeTrue, String title){
        if (!shouldBeTrue)
            throw new Failure(title);
    }
}
