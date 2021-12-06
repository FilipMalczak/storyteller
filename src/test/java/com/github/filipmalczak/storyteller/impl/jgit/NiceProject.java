package com.github.filipmalczak.storyteller.impl.jgit;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

//meta-utilities
@FieldDefaults(makeFinal = true)
@Slf4j
public class NiceProject {
//    //file where to store pseudo-remote GIT repository
//    //you can configure it outside to push to something like GitLab
//    File data = safeCreate();
//    //Directory where you store current sessions of episodes of the story
//    File tmp = safeCreateTmp();
//
//    //the crap Im trying to create
//     Storyteller storyteller = new JGitStorytelerFactory()
//        .create(
//            JGitStorytellerConfig.builder()
//                .storyRoot(data)
//                .tempRoot(tmp)
//                .build()
//        );
//
//    //helpers
//
//    @SneakyThrows
//    private File safeCreate() {
//        var out = new File("./test-data/out/golden");
//        out.mkdirs();
//        return out;
//    }
//
//    @SneakyThrows
//    private File safeCreateTmp() {
//        return Files.createTempDirectory("storyteller").toFile();
//    }
//
//    //the actual usage
//
//    //e.g. a neural net or other ML shit
//    @Inject
//    ResearchSubject subject;
//
//    //FScore is a evaluation that incorporates all 4 false/true positives/negatives
//    @Inject
//    FScoreEvaluator evaluator;
//
//    @Inject
//    LearningMethod<Config> learning;
//
//    @Inject
//    Dataset learning;
//
//    @Inject
//    Dataset evaluating;
//
//    private static final int EPOCHS = 100;
//
//    @Test
//    public void tweakConfig(){
//        var config = new Config(0, 0, 0);
//        var bestConfig = config.copy();
//        var bestEval = evaluator.eval(subject, evaluating);
//        storyteller.tell("tweak config", s -> {
//            for (int iteration=0; iteration<3; iteration++){
//                s.arc("iteration #"+iteration, a -> {
//
//                    for (int x=-10; x<10; x++)
//                        s.thread("tweak param X", t -> {
//                            var config = bestConfig.withX(x);
//                            t.scene("learn", storage -> {
//                                var id = "model/tweaking/"+config.filename();
//                                if (!storage.files().find(id).isPresent()) {
//                                    for (int i = 0; i < EPOCHS; i++)
//                                        subject.learn(shuffle(learning), config);
//                                    var modelFile = storage.files().create(id, "dat");
//                                    subject.saveModel(modelFile);
//                                }
//                            });
//                            t.scene("evaluate", storage -> {
//                                Evaluation evaluation = evaluator.eval(subject, evaluating);
//                                var props = storage.properties();
//                                var prop = props.create(
//                                    "model/"+config.filename()+"/f-score",
//                                    evaluation.value()
//                                );
//                                if (evaluation.isBetterThan(bestEval)){
//                                    bestConfig = config;
//                                    bestEval = evaluation;
//                                    props
//                                        .find(
//                                            "iteration"+iteration+"/x/best",
//                                            )
//                                        .map(p -> p.with(bestEval))
//                                        .stream().peek(p -> props.save(p))
//                                        .orElse(() -> props
//                                            .create(
//                                                "iteration"+iteration+"/x/best",
//                                                bestEval.value()
//                                            )
//                                        );
//                                }
//                            });
//                        });
//                }
//            }
//        });
//    }

}
