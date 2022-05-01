package com.github.filipmalczak.storyteller.impl.visualize;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import com.github.filipmalczak.storyteller.api.visualize.HtmlReportGenerator;
import com.github.filipmalczak.storyteller.api.visualize.ReportOptions;
import com.github.filipmalczak.storyteller.api.visualize.StartingPoint;
import com.github.filipmalczak.storyteller.api.visualize.html.Renderable;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.visualize.html.Html.*;
import static com.github.filipmalczak.storyteller.impl.visualize.html.Icons.iconForTypeModifier;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
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

    public void generateReport(@NonNull File resultDirectory, @NonNull StartingPoint<Id> startingPoint, @NonNull ReportOptions<Id, Definition, Type> options) {
        new Generation(resultDirectory, options).generateReport(startingPoint);
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private class Generation {
        @NonNull File resultDirectory;
        @NonNull ReportOptions<Id, Definition, Type> options;

        SortedSet<Session> sessions = new TreeSet<>(comparing(Session::getStartedAt));

        public void generateReport(@NonNull StartingPoint<Id> startingPoint) {
            handleResultDir();
            var startId = startingPoint.get();
            var root = managers.getTaskManager().getById(startId);
            generateIndexFile(startId);
            handle(new ArrayList<>(), root);
        }

        private void handleResultDir() {
            if (resultDirectory.exists()) {
                require(resultDirectory.isDirectory(), "todo");
            } else {
                resultDirectory.mkdirs();
            }
            new File(resultDirectory, "tasks").mkdirs();
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

        private static String POPVER_LITERAL= "<script>$(function () {\n" +
            "  $('[data-toggle=\"popover\"]').popover()\n" +
            "})</script>";

        private void generateIndexFile(Id startId) {
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
                                    attr("src", "./tasks/" + options.getIdIdSerializer().toString(startId) + ".html")
                                )
                            )
                        )
                    )
            );
        }

        private void handle(List<Task<Id, Definition, Type>> ancestors, Task<Id, Definition, Type> task) {
            generateTaskReport(ancestors, task);
            var newAncestors = new ArrayList<>(ancestors);
            newAncestors.add(task);
            task
                .getJournalEntries()
                //if options.excludeDisowned we can simply browser task.subtasks
                .filter(e -> e instanceof SubtaskDefined)
                .map(e -> ((SubtaskDefined<Id>) e).getDefinedSubtaskId())
                .map(managers.getTaskManager()::getById)
                .forEach(t -> handle(newAncestors, t));
        }

        private void generateTaskReport(List<Task<Id, Definition, Type>> ancestors,
                                        Task<Id, Definition, Type> task) {
            render(
                new File(resultDirectory, "tasks/" + options.getIdIdSerializer().toString(task.getId()) + ".html"),
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
                                                    taskHeader(ancestors, task),
                                                    runDetails(task),
                                                    journal(task)
                                                ) :
                                                sequence(
                                                    taskHeader(ancestors, task),
                                                    subtasks(task),
                                                    disowned(task),
                                                    journal(task)
                                                )
                                        ),
                                        literal(BOOTSTRAP_BODY_LITERAL),
                                        literal(ICONS_LITERAL),
                                        literal(POPVER_LITERAL)
                                    )
                                )
                            )
                        )
                )

            );
        }

        private Renderable taskHeader(List<Task<Id, Definition, Type>> ancestors,
                                      Task<Id, Definition, Type> task) {
            return sequence(
                navigation(ancestors),
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

        private Renderable navigation(List<Task<Id, Definition, Type>> ancestors) {
            var lis = new ArrayList<Renderable>();
            for (var ancestor : ancestors)
                lis.add(
                    node("li", cssClass("breadcrumb-item"),
                        node("a",
                            attr("href", "./" + options.getIdIdSerializer().toString(ancestor.getId()) + ".html"),
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

        private Renderable runDetails(Task<Id, Definition, Type> task) {
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
                            node("li", literal("Succeeded on run #" + numberOfRuns(task))),
                            node("li", literal("Skipped " + numberOfSkips(task) + " times")),
                            node("li", literal("Took " + duration(task))) //todo this doesnt make sense when read out loud
                        ))
                    )
                )
            );
        }

        private static long numberOfRuns(Task task) {
            return task.getJournalEntries().filter(e -> e instanceof InstructionsRan).count();
        }

        private static long numberOfSkips(Task task) {
            return task.getJournalEntries().filter(e -> e instanceof InstructionsSkipped).count();
        }

        private static record DurationAndSessionId(String sessionId, Duration duration) {
        }

        private static Optional<DurationAndSessionId> duration(Task task) {
            Map<String, List<JournalEntry>> runsAndEnds = (Map<String, List<JournalEntry>>) task
                .getJournalEntries()
                .filter(e -> e instanceof InstructionsRan || e instanceof TaskEnded)
                .collect(groupingBy((JournalEntry e) -> e.getSession().getId()));
            var results = new ArrayList<DurationAndSessionId>();
            for (var sessionId : runsAndEnds.keySet()) {
                var events = runsAndEnds.get(sessionId);
                if (events.size() >= 2) {
                    var last = events.get(events.size() - 1);
                    var prev = events.get(events.size() - 2);
                    if (last instanceof TaskEnded && prev instanceof InstructionsRan) {
                        results.add(new DurationAndSessionId(sessionId, Duration.between(prev.getHappenedAt(), last.getHappenedAt())));
                    }
                }
            }
            require(results.size() < 2);
            return results.stream().findFirst();
        }

        private Renderable subtasks(Task<Id, Definition, Type> task) {
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
                        taskList(task.getSubtasks(), depth)
                    )
                )
            );
        }

        private Renderable taskList(Stream<Task<Id, Definition, Type>> toPut, int depth) {
            return node("ul",
                sequence(
                    toPut
                        .map(t -> {
                            var def = node("a",
                                attr("href", "./" + options.getIdIdSerializer().toString(t.getId()) + ".html"),
                                sequence(
                                    iconForTypeModifier(t.getType().getModifier()),
                                    options.getDefinitionRenderer().renderable(t.getDefinition())
                                )
                            );
                            if (depth == 0)
                                return def;
                            return sequence(
                                def,
                                taskList(t.getSubtasks(), depth - 1)
                            );
                        })
                        .map(i -> node("li", cssClass("subtask-item"), i))
                        .toList()
                )
            );
        }

        private Renderable disowned(Task<Id, Definition, Type> task) {
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
                        taskList(task.getDisownedSubtasks(), depth)
                    )
                )
            );
        }

        private int sessionNo(Session session){
            return sessions.headSet(session).size();
        }

        private Renderable session(Session s){
            int no = sessionNo(s);
            return node(
                "button",
                literal("#"+no),
                cssClass("btn"),
                attr("type", "button"),
                attr("data-container", "body"),
                attr("data-toggle", "popover"),
                attr("data-trigger", "focus"),
                attr("data-placement", "top"),
                attr("title", "Session #"+no),
                attr("data-content", "Started at "+s.getStartedAt()+" on host "+s.getHostname()+" ; ID: "+s.getId())
            );
        }

        //todo prettier dates format
        private Renderable journal(Task<Id, Definition, Type> task) {
            Map<Session, List<JournalEntry>> sessions = task.getJournalEntries().collect(groupingBy(JournalEntry::getSession));
            List<Session> orderedSessions = sessions.keySet().stream().sorted(comparing(Session::getStartedAt)).toList();
            this.sessions.addAll(orderedSessions);
            List<List<Renderable>> rows = new ArrayList<>();
            task.getJournalEntries().forEach(entry -> {

                int sessionIdx = orderedSessions.indexOf(entry.getSession());
                List<Renderable> row = new ArrayList<>();
                row.add(literal(entry.getHappenedAt().toString()));
                for (int i = 0; i < sessionIdx; ++i)
                    row.add(empty());
                String entryTxt = EntryType.toType(entry).toString();
                if (entry instanceof ReferencesSubtasks) {
                    entryTxt += " -> "+
                        ((ReferencesSubtasks<Id>) entry)
                            .getReferences()
                            .stream()
                            .map(managers.getTaskManager()::getById)
                            .map(Task::getDefinition)
                            .toList();
                } else if (entry instanceof ExceptionCaught)
                    entryTxt += " : "+((ExceptionCaught) entry).getClassName()+"("+((ExceptionCaught) entry).getMessage()+")";
                row.add(literal(entryTxt));
                for (int i = sessionIdx + 1; i < orderedSessions.size(); ++i)
                    row.add(empty());
                rows.add(row);
            });
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
                        node("table",
                            sequence(
                                node("tr",
                                    sequence(
                                        node("th", literal("@")),
                                        sequence(
                                            orderedSessions.stream().map(s ->
                                                node("th", session(s))
                                            ).toList()
                                        )
                                    )
                                ),
                                sequence(
                                    rows.stream().map(r ->
                                        node("tr",
                                            sequence(r.stream().map(cell -> node("td", cell)).toList())
                                        )
                                    ).toList()
                                )
                            )
                        )
                    )
                )
            );
        }
    }
}
