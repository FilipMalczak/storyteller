package com.github.filipmalczak.storyteller.api.tree;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

public interface Sessions {
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    class AlreadyStartedException extends RuntimeException {
        @NonNull Session current;
        public AlreadyStartedException(Session current) {
            super("There already is a started session: "+current);
            this.current = current;
        }
    }

    /**
     * Start a sesssion. If there already is a started session, throw. Use {@link Sessions#end()} to avoid that.
     * @return non-null newly started session
     * @throws AlreadyStartedException if a session has already been started
     */
    Session start();

    /**
     * Start a session if there is no started one yet; if there is, return the already started one.
     * @return non-null session
     */
    Session getCurrent();

    /**
     * Discards current session. Next call to {@link Sessions#start()} (or {@link Sessions#getCurrent()}) will open one.
     */
    void end();
}
