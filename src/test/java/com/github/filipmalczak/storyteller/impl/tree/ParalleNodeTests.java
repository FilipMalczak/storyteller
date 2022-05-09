package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static com.github.filipmalczak.storyteller.utils.ExecutionTracker.unordered;
import static java.util.stream.Collectors.toSet;

public class ParalleNodeTests {
    private ExecutionTracker<Integer> tracker;
    private static final TestTreeFactory FACTORY = new TestTreeFactory("TaskSkippingTests");

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>(Integer.class);
    }

    //todo all the following tests (where possible), but starting from no merge

    @Test
    @DisplayName("r(=n1(>l1, l2), -n2(l3)) -> r(=n1(>l1, l2), -n2(l3))")
    void rerunSameParallel(){
        var exec = FACTORY.create("rerunSameParallel");
        tracker.setSessions(exec.getSessions());
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return subtasks.stream().filter(t -> t.getDefinition().equals("A")).collect(toSet());
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, unordered(3, 4), 5, 6, 7, 8, 9, 10);
        tracker.clear();
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return subtasks.stream().filter(t -> t.getDefinition().equals("A")).collect(toSet());
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, 5, 6, 7, 9, 10);
    }

    @Test
    @DisplayName("r(=n1(>l1, l2), -n2(l3)) -> r(=n1(>l1, >l2), -n2(l3))")
    void mergeMore(){
        var exec = FACTORY.create("mergeMore");
        tracker.setSessions(exec.getSessions());
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return subtasks.stream().filter(t -> t.getDefinition().equals("A")).collect(toSet());
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, unordered(3, 4), 5, 6, 7, 8, 9, 10);
        tracker.clear();
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return subtasks;
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("r(=n1(>l1, l2), -n2(l3)) -> r(=n1(l1, l2), -n2(l3))")
    void mergeOneThenMergeNone(){
        var exec = FACTORY.create("mergeOneThenMergeNone");
        tracker.setSessions(exec.getSessions());
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return subtasks.stream().filter(t -> t.getDefinition().equals("A")).collect(toSet());
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, unordered(3, 4), 5, 6, 7, 8, 9, 10);
        tracker.clear();
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return new HashSet<>();
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("r(=n1(>l1, >l2), -n2(l3)) -> r(=n1(>l1, l2), -n2(l3))")
    void mergeLess(){
        var exec = FACTORY.create("mergeLess");
        tracker.setSessions(exec.getSessions());
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return subtasks;
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, unordered(3, 4), 5, 6, 7, 8, 9, 10);
        tracker.clear();
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(5);
                    return subtasks.stream().filter(t -> t.getDefinition().equals("A")).collect(toSet());
                }
            );
            tracker.mark(6);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(7);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(8);
                });
                tracker.mark(9);
            });
            tracker.mark(10);
        });
        tracker.expect(1, 2, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("r(=n1(>l1, >l2, l3), -n2(l4)) -> r(=n1(>l1, l2, >l3), -n2(l4))")
    void mergeTwoThenReplaceOneMerged(){
        var exec = FACTORY.create("mergeTwoThenReplaceOneMerged");
        tracker.setSessions(exec.getSessions());
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                    nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(5);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(6);
                    return subtasks.stream().filter(t -> !t.getDefinition().equals("C")).collect(toSet());
                }
            );
            tracker.mark(7);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
            });
            tracker.mark(11);
        });
        tracker.expect(1, 2, unordered(3, 4, 5), 6, 7, 8, 9, 10, 11);
        tracker.clear();
        exec.execute("root", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("parallel node", TrivialTaskType.PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    tracker.mark(2);
                    nodeExec.execute("A", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(3);
                    });
                    nodeExec.execute("B", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(4);
                    });
                    nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                        tracker.mark(5);
                    });
                },
                (subtasks, insight) -> {
                    tracker.mark(6);
                    return subtasks.stream().filter(t -> !t.getDefinition().equals("B")).collect(toSet());
                }
            );
            tracker.mark(7);
            rootExec.execute("sequential node", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.execute("C", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
            });
            tracker.mark(11);
        });
        tracker.expect(1, 2, 6, 7, 8, 9, 10, 11);
    }
}
