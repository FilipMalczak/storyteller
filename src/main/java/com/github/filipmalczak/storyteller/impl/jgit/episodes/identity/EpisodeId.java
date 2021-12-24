package com.github.filipmalczak.storyteller.impl.jgit.episodes.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeId {
    @NonNull EpisodeType type;
    @NonNull String id; //todo nonempty

    public void setId(String id) {
        this.id = id.trim().replaceAll("\s+", "_");
    }

    @Override
    public String toString() {
        return type.getIdPrefix() +
            "." +
            id;
    }

    public static EpisodeId nonRandomId(EpisodeType type, String basis){
        return new EpisodeId(
            type,
            basis.trim().replaceAll(
                "([^\\w\\d-]+)",
                "_"
            )
        );
    }

    /**
     * The latter argument may be ignored or not; can be useful for debugging
     */
    public static EpisodeId randomId(EpisodeType type, String basis){
        return new EpisodeId(type, UUID.randomUUID().toString());
//        return nonRandomId(type, basis);
    }
}
