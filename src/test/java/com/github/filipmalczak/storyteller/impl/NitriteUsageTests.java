package com.github.filipmalczak.storyteller.impl;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class NitriteUsageTests {
    @SneakyThrows
    private Nitrite open(String name){
        var dir = new File(new File("./test_data/nitrite-tests"), name).getAbsoluteFile();
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
}
