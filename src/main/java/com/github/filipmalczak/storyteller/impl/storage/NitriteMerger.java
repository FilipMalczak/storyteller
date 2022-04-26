package com.github.filipmalczak.storyteller.impl.storage;


import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.valid4j.Assertive;

import java.util.HashSet;

import static org.dizitart.no2.Constants.DOC_MODIFIED;
import static org.dizitart.no2.filters.Filters.gt;
import static org.hamcrest.CoreMatchers.equalTo;

@Flogger
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NitriteMerger {
    @NonNull Nitrite target;

    public static NitriteMerger of(@NonNull Nitrite target){
        return new NitriteMerger(target);
    }

    /**
     * Pivot is excluded; if you want to merge changes that happened at that exact millisecond, subtract 1 from your desired pivot.
     */
    public void applyChanges(long pivotalTimestamp, @NonNull Nitrite source){
        Assertive.require(pivotalTimestamp > 0, "Pivotal timestamp cannot be 0 or negative");
        Assertive.require(pivotalTimestamp < System.currentTimeMillis(), "Pivotal timestamp must be in the past");
        var collectionNames = new HashSet<String>();
        collectionNames.addAll(source.listCollectionNames());
        //listRepositories returns names of backing collections - so, if key is used, it is already covered in there
        collectionNames.addAll(source.listRepositories());
        for (var collection: collectionNames){
            var targetCollection = target.getCollection(collection);
            var sourceCollection = source.getCollection(collection);
            var modifiedAfterPivot = sourceCollection.find(gt(DOC_MODIFIED, pivotalTimestamp));
            log.atFine().log("%s documents from collection %s were modified after timestamp %s", modifiedAfterPivot.size(), collection, pivotalTimestamp);
            for (var change: modifiedAfterPivot) {
                log.atFinest().log("Upserting %s", change);
                targetCollection.update(change, true);
            }
            log.atFine().log("Changes to collection %s succesfully introduced", collection);
        }
    }
}
