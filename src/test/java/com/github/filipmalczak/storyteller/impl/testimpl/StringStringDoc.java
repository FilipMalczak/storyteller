package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Id;

import static com.github.filipmalczak.storyteller.impl.IterationUtils.toStream;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StringStringDoc {
    @Id
    String id;
    String txt;

    public static void put(ReadWriteStorage<Nitrite> storage, String k, String v){
        storage
            .documents()
            .getRepository(StringStringDoc.class)
            .update(new StringStringDoc(k, v), true);
    }

    public static String getOr(ReadStorage<Nitrite> storage, String k, String def){
        return toStream(
            storage
                .documents()
                .getRepository(StringStringDoc.class)
                .find(eq("id", k))
        )
            .findFirst()
            .map(StringStringDoc::getTxt)
            .orElse(def);
    }
}
