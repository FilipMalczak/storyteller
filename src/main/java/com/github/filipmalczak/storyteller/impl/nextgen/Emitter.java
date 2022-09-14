package com.github.filipmalczak.storyteller.impl.nextgen;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Central event entrypoint. Takes care of persistence and listeners.
 */
public interface Emitter<TaskId extends Comparable<TaskId>> {
    void emit(Consumer<EventTriggers<TaskId>> producer);

    interface EventTriggers<Id> {
        interface ForSession<Id> {
            void started();
            void ended();
            void killed();
            TaskEvents forTask(Id id);

            interface TaskEvents<Id> {
                void opened();
                void closed();
                void failed();

                JournalEntries<Id> journal();

                interface JournalEntries<Id> {
                    void defineSubtask(Id subtask);

                    void taskStarted();

                    void taskPerformed(boolean skip);

                    void bodyChanged(List<Id> conflicting, Id pivot);

                    void bodyExtended(List<Id> added);

                    void bodyNarrowed(List<Id> removed);

                    void taskAmended();

                    void subtasksDisowned(List<Id> disowned);

                    void exeptionCaught(Exception e);

                    void taskInterrupted();

                    void nodeInflated(Set<Id> disappeared);

                    void nodeDeflated(Set<Id> appeared);

                    void nodeRefiltered(Set<Id> appeared, Set<Id> disappeared);

                    void nodeAugmented();

                    void subtaskIncorporated(Id child);

                    void taskEnded();
                }
            }
        }

        ForSession<Id> forSession(String sessionId);
    }



}
