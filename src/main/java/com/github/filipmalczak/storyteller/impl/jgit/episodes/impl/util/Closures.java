package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Closures {
//    public static ArcClosure arcClosure(List<EpisodeDefinition> knownProgress, EpisodeId episodeId, Workspace workspace, DiskSpaceManager manager, Consumer<EpisodeDefinition> persistDefinition){
//        return new ArcClosure() {
//            @Override
//            public void thread(String thread, ActionBody<ThreadClosure> body) {
//                log.info("Thread "+thread);
//                var expected = pop(knownProgress);
//                log.info("Expected "+expected);
//                var def = handleDefinition(
//                    expected,
//                    EpisodeSpec.builder()
//                        .type(EpisodeType.THREAD)
//                        .name(thread)
//                        .build(),
//                    persistDefinition
//                );
//                log.info("Def "+def);
//                new Thread(
//                    def.getEpisodeId(),
//                    def.getEpisodeSpec().getName(),
//                    episodeId,
//                    body
//                ).tell(workspace, manager);
//            }
//
//            @Override
//            public void arc(String arc, ActionBody<ArcClosure> body) {
//                var expected = pop(knownProgress);
//                var def = handleDefinition(
//                    expected,
//                    EpisodeSpec.builder()
//                        .type(EpisodeType.ARC)
//                        .name(arc)
//                        .build(),
//                    persistDefinition
//                );
//                new Arc(
//                    def.getEpisodeId(),
//                    def.getEpisodeSpec().getName(),
//                    episodeId,
//                    body
//                ).tell(TaleContext.of(workspace, manager));
//            }
//
//            @Override
//            public <K> void decision(String decision, ActionBody<DecisionClosure<K>> body) {
//                throw new RuntimeException(); //todo implement me
//            }
//        };
//    }
//
//    public static ThreadClosure threadClosure(List<EpisodeDefinition> knownProgress, List<RevCommit> sceneLikeCommits, EpisodeId episodeId, Workspace workspace, DiskSpaceManager manager, Consumer<EpisodeDefinition> persistDefinition){
//        //DO NOT replace with lambda, for readability! (following comment is for IntelliJ)
//        //noinspection Convert2Lambda
//        return new ThreadClosure() {
//            @Override
//            public void scene(String name, ActionBody<Storage> body) {
//                var spec = EpisodeSpec.builder().name(name).type(EpisodeType.SCENE).build();
//                var firstDefinitionMaybe = pop(knownProgress);
//                var firstCommitMaybe = pop(sceneLikeCommits);
//                EpisodeId id;
//                //if expected def:
//                if (firstDefinitionMaybe.isPresent()){
//                    var def = firstDefinitionMaybe.get();
//                    //  assert spec matches
//                    invariant(
//                        def.getEpisodeSpec().equals(spec),
//                        "already defined spec must match requested one"
//                    );
//                    //  id = from def
//                    id = def.getEpisodeId();
//                } else {
//                    //  id = new random
//                    id = EpisodeId.randomId(EpisodeType.SCENE);
//                    //  define(id)
//                    persistDefinition.accept(new EpisodeDefinition(id, spec));
//                }
//                //if expected commit
//                if (firstCommitMaybe.isPresent()) {
//                    //  assert name matches
//                    var expected = buildRefName(firstDefinitionMaybe.get().getEpisodeId(), RUN);
//                    invariant(
//                        expected.equals(firstCommitMaybe.get().getFullMessage()),
//                        "commit proving run of "+episodeId+" must have "+expected+" as ID"
//                    );
//                    //  skip
//                    log.info("Skipping scene "+name+" because its proof has been found in commit "+firstCommitMaybe.get().toObjectId());
//                } else {
//                    //scene.tell
//                    var scene = new Scene(id, name, episodeId, body);
//                    scene.tell(TaleContext.of(workspace, manager));
//                }
//            }
//        };
//    }
}
