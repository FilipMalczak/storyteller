package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.DecisionClosure;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.Arc;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.Scene;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.Thread;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.LinkedList;
import java.util.List;

import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Commands.handleDefinition;
import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Commands.pop;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.RUN;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.safeguard;

@Slf4j
public class Closures {
    public static ArcClosure arcClosure(List<EpisodeDefinition> knownProgress, EpisodeId episodeId, Workspace workspace, DiskSpaceManager manager){
        return new ArcClosure() {
            @Override
            public void thread(String thread, ActionBody<ThreadClosure> body) {
                var expected = pop(knownProgress);
                var def = handleDefinition(
                    expected,
                    EpisodeSpec.builder()
                        .type(EpisodeType.THREAD)
                        .name(thread)
                        .build()
                );
                new Thread(
                    def.getEpisodeId(),
                    def.getEpisodeSpec().getName(),
                    episodeId,
                    body
                ).tell(workspace, manager);
            }

            @Override
            public void arc(String arc, ActionBody<ArcClosure> body) {
                var expected = pop(knownProgress);
                var def = handleDefinition(
                    expected,
                    EpisodeSpec.builder()
                        .type(EpisodeType.ARC)
                        .name(arc)
                        .build()
                );
                new Arc(
                    def.getEpisodeId(),
                    def.getEpisodeSpec().getName(),
                    episodeId,
                    body
                ).tell(workspace, manager);
            }

            @Override
            public <K> void decision(String decision, ActionBody<DecisionClosure<K>> body) {
                throw new RuntimeException(); //todo implement me
            }
        };
    }

    public static ThreadClosure threadClosure(List<EpisodeDefinition> knownProgress, List<RevCommit> sceneLikeCommits, EpisodeId episodeId, Workspace workspace, DiskSpaceManager manager){
        //DO NOT replace with lambda, foe readability!
        return new ThreadClosure() {
            @Override
            public void scene(String name, ActionBody<Storage> body) {
                var firstDefinitionMaybe = pop(knownProgress);
                var firstCommitMaybe = pop(sceneLikeCommits);
                if (firstDefinitionMaybe.isPresent()){
                    log.info("Skipping scene "+name+" because its proof has been found in commit "+firstCommitMaybe.get().toObjectId());
                } else {
                    if (firstCommitMaybe.isPresent()){
                        var expected = buildRefName(firstDefinitionMaybe.get().getEpisodeId(), RUN);
                        safeguard(
                            expected.equals(firstCommitMaybe.get().getFullMessage()),
                            "commit proving run of "+episodeId+" must have "+expected+" as ID"
                        );
                        //todo its metadata is matching
                    } else {
                        var scene = new Scene(EpisodeId.randomId(EpisodeType.SCENE), name, episodeId, body);
                        scene.tell(workspace, manager);
                    }
                }

            }
        };
    }
}
