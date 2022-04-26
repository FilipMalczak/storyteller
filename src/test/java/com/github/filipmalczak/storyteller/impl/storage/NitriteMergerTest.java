package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.testimpl.StringStringDoc;
import lombok.SneakyThrows;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.tool.Exporter;
import org.dizitart.no2.tool.Importer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.function.Consumer;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NitriteMergerTest {
    Nitrite target;
    NitriteMerger merger;
    String storedState;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        target = Nitrite.builder().openOrCreate();
        var repo = target.getRepository(StringStringDoc.class);
        repo.insert(new StringStringDoc("a", "1"));
        repo.insert(new StringStringDoc("b", "2"));
        repo.insert(new StringStringDoc("c", "3"));

        var exporter = Exporter.of(target);
        try (var writer = new StringWriter()) {
            exporter.exportTo(writer);
            storedState = writer.toString();
        }

        merger = NitriteMerger.of(target);
    }

    Nitrite getStartingState(){
        var out = Nitrite.builder().openOrCreate();
        var importer = Importer.of(out);
        try (var reader = new StringReader(storedState)){
            importer.importFrom(reader);
        }
        return out;
    }

    static record Changeset(long timestamp, Nitrite db){}

    @SneakyThrows
    Changeset getModified(Consumer<ObjectRepository<StringStringDoc>> modification){
        var out = getStartingState();
        long ts = System.currentTimeMillis();
        Thread.sleep(10); // this is required, because sometimes operations may be VERY quick
        modification.accept(out.getRepository(StringStringDoc.class));
        return new Changeset(ts, out);
    }

    Changeset getModified(String id, String newVal){
        return getModified(r -> r.update(new StringStringDoc(id, newVal)));
    }

    Changeset getAdded(String id, String val){
        return getModified(r -> r.insert(new StringStringDoc(id, val)));
    }

    String get(String id){
        var found = target.getRepository(StringStringDoc.class).find(eq("id", id)).toList();
        assertTrue(found.size() < 2);
        return found.isEmpty() ? null : found.get(0).getTxt();
    }

    void apply(Changeset changeset){
        merger.applyChanges(changeset.timestamp, changeset.db);
    }

    //todo merger doesnt support deletion just yet;
    // if after upserting sizes of collections differ, we need to browse non-upserted target documents and see which are missing from source

    @Test
    void testScenario(){
        apply(getModified("a", "4"));

        assertEquals("4", get("a"));
        assertEquals("2", get("b"));
        assertEquals("3", get("c"));
        assertEquals(null, get("x"));

        apply(getModified("b", "5"));

        assertEquals("4", get("a"));
        assertEquals("5", get("b"));
        assertEquals("3", get("c"));
        assertEquals(null, get("x"));

        apply(getAdded("x", "6"));

        assertEquals("4", get("a"));
        assertEquals("5", get("b"));
        assertEquals("3", get("c"));
        assertEquals("6", get("x"));
    }

}