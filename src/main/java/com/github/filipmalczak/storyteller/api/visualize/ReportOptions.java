package com.github.filipmalczak.storyteller.api.visualize;

import com.fasterxml.jackson.databind.type.TypeModifier;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.visualize.html.HtmlRenderer;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdSerializer;
import com.github.filipmalczak.storyteller.api.visualize.html.Renderable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Map;
import java.util.Optional;

import static com.github.filipmalczak.storyteller.api.stack.task.TaskType.TaskTypeModifier.*;
import static com.github.filipmalczak.storyteller.api.visualize.html.Bootstrap.badge;
import static com.github.filipmalczak.storyteller.api.visualize.html.Html.*;
import static com.github.filipmalczak.storyteller.api.visualize.html.Icons.iconForTypeModifier;

/**
 * Depths are interpreted as:
 * 0 - only direct subtasks
 * 1 - direct subtasks and their direct subtasks
 * n - direct subtasks and n levels down
 * <0 - all the subtasks
 */
@Value
@Builder
public class ReportOptions<Id, Definition, Type extends TaskType> {

    @Builder.Default
    @NonNull HtmlRenderer<Id> idRenderer = HtmlRenderer.asString();
    @Builder.Default
    @NonNull HtmlRenderer<Definition> definitionRenderer = HtmlRenderer.asString();
    @Builder.Default
    @NonNull HtmlRenderer<Type> typeRenderer = t -> badge(
                t.toString(),
                true,
                "secondary"
            )
            .renderHtml();

    @Builder.Default
    @NonNull IdSerializer<Id> idIdSerializer = s -> s.toString();

    @Builder.Default
    int defaultSubtaskDepth = 1;
    @Singular
    Map<Type, Integer> subtaskDepths;

    @Builder.Default
    int defaultDisownedDepth = 0;
    @Singular
    Map<Type, Integer> disownedDepths;

    //todo include session hostname; include session started at
    //todo headers customizations
    //todo custom components (3 groups: top==above subtasks, middle==between subtasks and journal, bottom==below journal)
    //todo include disowned (true by default)
}
