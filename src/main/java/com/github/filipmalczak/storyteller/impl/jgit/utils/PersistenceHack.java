package com.github.filipmalczak.storyteller.impl.jgit.utils;

import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;

public class PersistenceHack {
    public static void pushItTheFuckOuttaHere(WorkingCopy copy){
        pushItTheFuckOuttaHere(copy.getRepository());
    }

    @SneakyThrows
    //fixme the name is intentionally vulgar - mature product shouldn't contain such and since this is a hack, it will force some sort of action later
    public static void pushItTheFuckOuttaHere(Git git){
        git.push().setPushAll().setPushTags().call();
    }
}
