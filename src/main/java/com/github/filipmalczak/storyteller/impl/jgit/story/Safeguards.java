package com.github.filipmalczak.storyteller.impl.jgit.story;

public class Safeguards {
    public static class Failure extends RuntimeException {
        public Failure(String title) {
            super("Safeguard failure: "+title);
        }
    }

    public static void safeguard(boolean shouldBeTrue, String title){
        if (!shouldBeTrue)
            throw new Failure(title);
    }
}
