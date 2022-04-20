package com.github.filipmalczak.storyteller.impl.visualize;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.*;
import com.github.filipmalczak.storyteller.api.visualize.HtmlReportGenerator;
import com.github.filipmalczak.storyteller.api.visualize.ReportOptions;
import com.github.filipmalczak.storyteller.api.visualize.StartingPoint;
import com.github.filipmalczak.storyteller.api.visualize.html.Renderable;
import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.api.visualize.html.Html.*;
import static com.github.filipmalczak.storyteller.api.visualize.html.Icons.iconForTypeModifier;
import static org.valid4j.Assertive.require;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NitriteReportGenerator<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements HtmlReportGenerator<Id, Definition, Type> {
    NitriteManagers<Id, Definition, Type> managers;

    public NitriteReportGenerator(@NonNull Nitrite nitrite) {
        this(new NitriteManagers<>(nitrite));
    }

    public NitriteReportGenerator(@NonNull File index){
        this(Nitrite.builder().filePath(index).readOnly().openOrCreate());
    }

    @Override
    public void generateReport(File resultDirectory, StartingPoint<Id> startingPoint, ReportOptions<Id, Definition, Type> options) {
        handleResultDir(resultDirectory);
        var startId = startingPoint.get();
        var root = managers.getTaskManager().getById(startId);
        generateIndexFile(resultDirectory, startId, options);
        handle(new ArrayList<>(), root, resultDirectory, options);
    }

    private void handleResultDir(File dir){
        if (dir.exists()) {
            require(dir.isDirectory(), "todo");
        } else {
            dir.mkdirs();
        }
        new File(dir, "tasks").mkdirs();
    }

    private static String INDEX_STYLE_LITERAL = "<style type=\"text/css\">\n" +
        "            body, html\n" +
        "            {\n" +
        "                margin: 0; padding: 0; height: 100%; overflow: hidden;\n" +
        "            }\n" +
        "\n" +
        "            #content\n" +
        "            {\n" +
        "                position:absolute; left: 0; right: 0; bottom: 0; top: 0px;\n" +
        "            }\n" +
        "        </style>";

    private static String LIST_STYLE_LITERAL = "<style type=\"text/css\">li.subtask-item { list-style-type: none; }</style>";

    private static String BOOTSTRAP_HEAD_LITERAL = "<meta charset=\"utf-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
        "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css\" integrity=\"sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T\" crossorigin=\"anonymous\">";

    private static String BOOTSTRAP_BODY_LITERAL = "<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\" integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\" crossorigin=\"anonymous\"></script>\n" +
        "    <script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.14.7/dist/umd/popper.min.js\" integrity=\"sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1\" crossorigin=\"anonymous\"></script>\n" +
        "    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/js/bootstrap.min.js\" integrity=\"sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM\" crossorigin=\"anonymous\"></script>";

    private static String ICONS_LITERAL = "<script type=\"module\" src=\"https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js\"></script>\n" +
        "<script nomodule src=\"https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js\"></script>";

    private void generateIndexFile(File resultDirectory, Id startId, ReportOptions<Id, Definition, Type> options){
        render(
            new File(resultDirectory, "index.html"),
            node("html", attr("xmlns", "http://www.w3.org/1999/xhtml"))
                .withContent(
                    sequence(
                        node("head",
                            sequence(
                                node("title", literal("Report")),
                                literal(INDEX_STYLE_LITERAL)
                            )
                        ),
                        node("body",
                            node("iframe",
                                empty(),
                                attr("width", "100%"), attr("height", "100%"),
                                attr("frameborder", 0),
                                attr("src", "./tasks/"+options.getIdIdSerializer().toString(startId)+".html")
                            )
                        )
                    )
                )
        );
    }

    private void handle(List<Task<Id, Definition, Type>> ancestors,
                        Task<Id, Definition, Type> task,
                        File resultDirectory,
                        ReportOptions<Id, Definition, Type> options){
        generateTaskReport(ancestors, task, resultDirectory, options);
        var newAncestors = new ArrayList<>(ancestors);
        newAncestors.add(task);
        task
            .getJournalEntries()
            //if options.excludeDisowned we can simply browser task.subtasks
            .filter(e -> e instanceof SubtaskDefined)
            .map(e -> ((SubtaskDefined) e).getDefinedSubtask())
            .forEach(t -> handle(newAncestors, t, resultDirectory, options));
    }

    private void generateTaskReport(List<Task<Id, Definition, Type>> ancestors,
                                    Task<Id, Definition, Type> task,
                                    File resultDirectory,
                                    ReportOptions<Id, Definition, Type> options){
        render(
            new File(resultDirectory, "tasks/"+options.getIdIdSerializer().toString(task.getId())+".html"),
            sequence(
                literal("<!doctype html>"),
                node("html",
                    attr("xmlns", "http://www.w3.org/1999/xhtml"),
                    attr("lang", "en")
                )
                    .withContent(
                        sequence(
                            node("head",
                                sequence(
                                    literal(BOOTSTRAP_HEAD_LITERAL),
                                    literal(LIST_STYLE_LITERAL),
                                    node("title", literal("Task report"))
                                )
                            ),
                            node("body",
                                sequence(
                                    node("div", cssClass("container-fluid"),
                                        task.getType().isLeaf() ?
                                            sequence(
                                                taskHeader(ancestors, task, options),
                                                runDetails(ancestors, task, options),
                                                journal(ancestors, task, options)
                                            ) :
                                            sequence(
                                                taskHeader(ancestors, task, options),
                                                subtasks(ancestors, task, options),
                                                disowned(ancestors, task, options),
                                                journal(ancestors, task, options)
                                            )
                                    ),
                                    literal(BOOTSTRAP_BODY_LITERAL),
                                    literal(ICONS_LITERAL)
                                )
                            )
                        )
                    )
            )

        );
    }

    private Renderable taskHeader(List<Task<Id, Definition, Type>> ancestors,
                                  Task<Id, Definition, Type> task,
                                  ReportOptions<Id, Definition, Type> options){
        return sequence(
            navigation(ancestors, options),
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    node("h1",
                        sequence(
                            options.getTypeRenderer().renderable(task.getType()),
                            iconForTypeModifier(task.getType().getModifier()),
                            options.getDefinitionRenderer().renderable(task.getDefinition())
                        )
                    )
                )
            )
        );
    }

    private Renderable navigation(List<Task<Id, Definition, Type>> ancestors,
                                  ReportOptions<Id, Definition, Type> options){
        var lis = new ArrayList<Renderable>();
        for (var ancestor: ancestors)
            lis.add(
                node("li", cssClass("breadcrumb-item"),
                    node("a",
                        attr("href", "./"+options.getIdIdSerializer().toString(ancestor.getId())+".html"),
                        sequence(
                            iconForTypeModifier(ancestor.getType().getModifier()),
                            options.getDefinitionRenderer().renderable(ancestor.getDefinition())
                        )
                    )
                )
            );
        return node("nav", attr("aria-label", "breadcrumb"),
            node("ol", cssClass("breadcrumb"),
                sequence((lis))
            )
        );
    }

    private Renderable runDetails(List<Task<Id, Definition, Type>> ancestors,
                                Task<Id, Definition, Type> task,
                                ReportOptions<Id, Definition, Type> options){
        int depth = options.getSubtaskDepths().getOrDefault(task.getType(), options.getDefaultSubtaskDepth());
        return sequence(
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    node("h2",
                        literal("Leaf task details")
                    )
                )
            ),
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    node("ul", sequence(
                        node("li", literal("Succeeded on run #"+numberOfRuns(task))),
                        node("li", literal("Skipped "+numberOfSkips(task)+" times")),
                        node("li", literal("Took "+duration(task))) //todo this doesnt make sense when read out loud
                    ))
                )
            )
        );
    }

    private static long numberOfRuns(Task task){
        return task.getJournalEntries().filter(e -> e instanceof InstructionsRan).count();
    }

    private static long numberOfSkips(Task task){
        return task.getJournalEntries().filter(e -> e instanceof InstructionsSkipped).count();
    }

    private static record DurationAndSessionId(String sessionId, Duration duration) {}

    private static Optional<DurationAndSessionId> duration(Task task){
        Map<String, List<JournalEntry>> runsAndEnds = (Map<String, List<JournalEntry>>) task
            .getJournalEntries()
            .filter(e -> e instanceof InstructionsRan || e instanceof TaskEnded)
            .collect(Collectors.groupingBy((JournalEntry e) -> e.getSession().getId()));
        var results = new ArrayList<DurationAndSessionId>();
        for (var sessionId: runsAndEnds.keySet()) {
            var events = runsAndEnds.get(sessionId);
            if (events.size() >= 2) {
                var last = events.get(events.size()-1);
                var prev  = events.get(events.size()-2);
                if (last instanceof TaskEnded && prev instanceof InstructionsRan) {
                    results.add(new DurationAndSessionId(sessionId, Duration.between(prev.getHappenedAt(), last.getHappenedAt())));
                }
            }
        }
        require(results.size() < 2);
        return results.stream().findFirst();
    }

    private Renderable subtasks(List<Task<Id, Definition, Type>> ancestors,
                                  Task<Id, Definition, Type> task,
                                  ReportOptions<Id, Definition, Type> options){
        int depth = options.getSubtaskDepths().getOrDefault(task.getType(), options.getDefaultSubtaskDepth());
        return sequence(
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    node("h2",
                        literal("Subtasks")
                    )
                )
            ),
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    taskList(task.getSubtasks().stream(), depth, options)
                )
            )
        );
    }

    private Renderable taskList(Stream<Task<Id, Definition, Type>> toPut, int depth, ReportOptions<Id, Definition, Type> options){
        return node("ul",
            sequence(
                toPut
                    .map(t -> {
                        var def = node("a",
                            attr("href", "./"+options.getIdIdSerializer().toString(t.getId())+".html"),
                            sequence(
                                iconForTypeModifier(t.getType().getModifier()),
                                options.getDefinitionRenderer().renderable(t.getDefinition())
                            )
                        );
                        if (depth == 0)
                            return def;
                        return sequence(
                            def,
                            taskList(t.getSubtasks().stream(), depth-1, options)
                        );
                    })
                    .map(i -> node("li", cssClass("subtask-item"), i))
                    .toList()
            )
        );
    }

    private Renderable disowned(List<Task<Id, Definition, Type>> ancestors,
                                Task<Id, Definition, Type> task,
                                ReportOptions<Id, Definition, Type> options){
        int depth = options.getDisownedDepths().getOrDefault(task.getType(), options.getDefaultDisownedDepth());
        return sequence(
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    node("h2",
                        literal("Disowned subtasks")
                    )
                )
            ),
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    taskList(task.getDisownedSubtasks(), depth, options)
                )
            )
        );
    }

    private Renderable journal(List<Task<Id, Definition, Type>> ancestors,
                                Task<Id, Definition, Type> task,
                                ReportOptions<Id, Definition, Type> options){
        return sequence(
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    node("h2",
                        literal("Journal")
                    )
                )
            ),
            node("div", cssClass("row"),
                node("div", cssClass("col"),
                    literal("TODO")
                )
            )
        );
    }
}
