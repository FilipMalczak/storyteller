package com.github.filipmalczak.storyteller.api.session.events;

//todo @MetaDimension
public sealed interface Joinpoint permits Joinpoint.Start, Joinpoint.End {
    non-sealed interface Start extends Joinpoint {}
    sealed interface End extends Joinpoint  {
        non-sealed interface Success extends End {}
        non-sealed interface Failure extends End {}
    }

}
