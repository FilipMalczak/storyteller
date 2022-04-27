package com.github.filipmalczak.storyteller.impl.storage;


import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.valid4j.Assertive;

import java.util.HashSet;

import static org.dizitart.no2.Constants.*;
import static org.dizitart.no2.util.ObjectUtils.getIdField;
import static org.valid4j.Assertive.require;

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
        require(pivotalTimestamp > 0, "Pivotal timestamp cannot be 0 or negative");
        require(pivotalTimestamp < System.currentTimeMillis(), "Pivotal timestamp must be in the past");
        applyDocumentChanges(pivotalTimestamp, source);
        applyRepositoryChanges(pivotalTimestamp, source);
    }

    private void applyDocumentChanges(long pivotalTimestamp, @NonNull Nitrite source){
        var mergeTs = System.currentTimeMillis();
        for (var collection: source.listCollectionNames()){
            var targetCollection = target.getCollection(collection);
            var sourceCollection = source.getCollection(collection);
            var modifiedAfterPivot = sourceCollection.find(Filters.gt(DOC_MODIFIED, pivotalTimestamp));
            if (modifiedAfterPivot.hasMore()) {
                log.atFine().log(
                    "%s documents from collection %s were modified after timestamp %s",
                    modifiedAfterPivot.size(),
                    collection,
                    pivotalTimestamp
                );
            }
            for (var change: modifiedAfterPivot) {
                log.atFinest().log("Upserting document %s", change);
                targetCollection.update(change, true);
            }
            int deleted = 0;
            for (var untouched: targetCollection.find(Filters.lte(DOC_MODIFIED, mergeTs))) {
                if (sourceCollection.find(Filters.eq(DOC_ID, untouched.get(DOC_ID))).size() == 0){
                    log.atFinest().log("Deleting document %s", untouched);
                    targetCollection.remove(untouched);
                    deleted += 1;
                }
            }
            if (deleted > 0) {
                log.atFine().log("%s documents from collection %s were deleted", deleted, collection);
            }
            log.atFine().log("Changes to collection %s succesfully introduced", collection);
        }
    }

    @SneakyThrows
    private void applyRepositoryChanges(long pivotalTimestamp, @NonNull Nitrite source){
        var mergeTs = System.currentTimeMillis();

        for (var repo: source.listRepositories()){
            ObjectRepository targetRepo;
            ObjectRepository sourceRepo;
            Class clazz;
            if (repo.contains(KEY_OBJ_SEPARATOR)){
                String[] parts = repo.split("\\"+KEY_OBJ_SEPARATOR);
                // see org.dizitart.no2.util.ObjectUtils.isKeyedObjectStore
                require(parts.length == 2, "Repository name must be well-formed");
                String key = parts[1];
                clazz = Class.forName(parts[0]);
                targetRepo = target.getRepository(key, clazz);
                sourceRepo = source.getRepository(key, clazz);
            } else {
                clazz = Class.forName(repo);
                targetRepo = target.getRepository(clazz);
                sourceRepo = source.getRepository(clazz);
            }
            var modifiedAfterPivot = sourceRepo.find(ObjectFilters.gt(DOC_MODIFIED, pivotalTimestamp));
            if (modifiedAfterPivot.hasMore()) {
                log.atFine().log(
                    "%s objects from repository %s were modified after timestamp %s",
                    modifiedAfterPivot.size(),
                    repo,
                    pivotalTimestamp
                );
            }
            for (var change: modifiedAfterPivot) {
                log.atFinest().log("Upserting object %s", change);
                targetRepo.update(change, true);
            }
            int deleted = 0;
            var idField = getIdField(source.getContext().getNitriteMapper(), clazz);
            idField.setAccessible(true);
            for (var untouched: targetRepo.find(ObjectFilters.lte(DOC_MODIFIED, mergeTs))) {
                if (sourceRepo.find(ObjectFilters.eq(idField.getName(), idField.get(untouched))).size() == 0){
                    log.atFinest().log("Deleting object %s", untouched);
                    targetRepo.remove(untouched);
                    deleted += 1;
                }
            }
            if (deleted > 0) {
                log.atFine().log("%s documents from collection %s were deleted", deleted, repo);
            }
            log.atFine().log("Changes to repository %s succesfully introduced", repo);
        }
    }
}
