package com.github.filipmalczak.storyteller.impl;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.filipmalczak.storyteller.impl.testimpl.DocWithDate;
import com.github.filipmalczak.storyteller.impl.testimpl.SimpleDoc;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Exporter;
import org.dizitart.no2.tool.Importer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.dizitart.no2.Constants.DOC_MODIFIED;
import static org.dizitart.no2.filters.Filters.gt;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Flogger
public class NitriteUsageTests {
    private static File testFile(String name) {
        return new File(new File("./test_data/nitrite-tests"), name);
    }

    @SneakyThrows
    private Nitrite open(String name){
        var dir = testFile(name).getAbsoluteFile();
        if (dir.exists())
            deleteDirectory(dir);
        dir.mkdirs();
        return Nitrite.builder()
            .compressed()
            .filePath(new File(dir, "db.no2"))
            .registerModule(new JavaTimeModule())
            .openOrCreate();
    }

    @Test
    void canHandleDatetime(){
        var no2 = open("insertAndRead");
        var repo = no2.getRepository(DocWithDate.class);
        var doc = new DocWithDate("abc", ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC.normalized()));
        repo.insert(doc);
    }

    @Test
    void insertAndRead(){
        var no2 = open("insertAndRead");
        var repo = no2.getRepository(SimpleDoc.class);
        var doc = new SimpleDoc("abc", 1, asList("def"), true);
        repo.insert(doc);
        var found = repo.find(eq("id", "abc")).toList();
        assertThat(found.size(), equalTo(1));
        assertThat(found.get(0), equalTo(doc));
    }

    @Test
    @SneakyThrows
    void dbMergePoc(){

        var initialDb = open("dbMergePoc_init");
        var initialRepo = initialDb.getRepository(SimpleDoc.class);
        initialRepo.insert(new SimpleDoc("a", 1, asList(), true)); //updated in db1
        initialRepo.insert(new SimpleDoc("b", 2, asList(), true)); //updated in db2
        initialRepo.insert(new SimpleDoc("c", 3, asList(), true)); //updated in result
        initialRepo.insert(new SimpleDoc("d", 4, asList(), true)); //updated in both
        initialRepo.insert(new SimpleDoc("e", 5, asList(), true)); //untouched
        //additionally, db1 adds x and db2 adds y
        var exporter = Exporter.of(initialDb);
        var exportedInit = testFile("dbMergePoc/init.json");
        exporter.exportTo(exportedInit);



        var db1 = open("dbMergePoc_branch1");
        var importer = Importer.of(db1);
        importer.importFrom(exportedInit);
        long ts1 = System.currentTimeMillis();
//        Thread.sleep(10);
        db1.getRepository(SimpleDoc.class).update(new SimpleDoc("a", 6, asList(), true));
        db1.getRepository(SimpleDoc.class).update(new SimpleDoc("d", 7, asList(), true));
        db1.getRepository(SimpleDoc.class).update(new SimpleDoc("x", 8, asList(), true), true);

        var db2 = open("dbMergePoc_branch2");
        importer = Importer.of(db2);
        importer.importFrom(exportedInit);
        long ts2 = System.currentTimeMillis();
//        Thread.sleep(10);
        db2.getRepository(SimpleDoc.class).update(new SimpleDoc("b", 9, asList(), true));
        db2.getRepository(SimpleDoc.class).update(new SimpleDoc("d", 10, asList(), true));
        db2.getRepository(SimpleDoc.class).update(new SimpleDoc("y", 11, asList(), true), true);

        var merged = open("dbMergePoc_merged");
        importer = Importer.of(merged);
        importer.importFrom(exportedInit);
        merged.getRepository(SimpleDoc.class).update(new SimpleDoc("c", 12, asList(), true));

        var collections = new HashSet<String>();
        //listRepositories returns names of backing collections - so, if key is used, it is already covered here
        collections.addAll(db1.listRepositories());
        collections.addAll(db2.listRepositories());
        //todo do the same for non-repo collections
        log.atInfo().log("Pivotal timestamp: %s", ts1);
        for (var collectionName: collections){
            var mergedCollection = merged.getCollection(collectionName);
            for (var db1Update: db1.getCollection(collectionName).find(gt(DOC_MODIFIED, ts1))){
                log.atInfo().log("Updating %s from db1", db1Update);
                mergedCollection.update(db1Update, true);
            }
            for (var db2Update: db2.getCollection(collectionName).find(gt(DOC_MODIFIED, ts2))){
                log.atInfo().log("Updating %s from db2", db2Update);
                mergedCollection.update(db2Update, true);
            }
        }
        Set<SimpleDoc> expected = new HashSet<>(asList(
            new SimpleDoc("a", 6, asList(), true),
            new SimpleDoc("b", 9, asList(), true),
            new SimpleDoc("c", 12, asList(), true),
            new SimpleDoc("d", 10, asList(), true),
            new SimpleDoc("e", 5, asList(), true),
            new SimpleDoc("x", 8, asList(), true),
            new SimpleDoc("y", 11, asList(), true)
        ));
        Set<SimpleDoc> found = new HashSet<>(merged.getRepository(SimpleDoc.class).find().toList());
        assertEquals(expected, found);
    }
}
