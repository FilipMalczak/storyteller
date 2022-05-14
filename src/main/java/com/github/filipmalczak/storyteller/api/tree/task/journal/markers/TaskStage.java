package com.github.filipmalczak.storyteller.api.tree.task.journal.markers;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.MetaDimension;

@MetaDimension
public sealed interface TaskStage permits TaskStage.Computations, TaskStage.Hierarchy, TaskStage.Planning {
    non-sealed interface Computations extends TaskStage {
    }

    non-sealed interface Hierarchy extends TaskStage {
    }

    non-sealed interface Planning extends TaskStage {
    }
}
