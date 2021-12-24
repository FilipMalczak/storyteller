package com.github.filipmalczak.storyteller.impl.jgit;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.ToBeContinuedException;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.TaleContext;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.Story;
import com.github.filipmalczak.storyteller.impl.jgit.storage.GitManager;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Flogger
public class JGitStoryteller implements Storyteller {
    //https://git-scm.com/docs/gitignore
    //last example, version 2.34.1
    private static String GITIGNORE_CONTENT = """
            /*
            !/documents
            !/properties
            !/files
            !/.episode-index
            !/.gitignore
            """;

    private static byte[] GITIGNORE_BYTES = GITIGNORE_CONTENT.getBytes(StandardCharsets.UTF_8);

    private static List<String> GITIGNORE_LINES = asList(GITIGNORE_CONTENT.split("\\n"));

    //given:
    @NonNull File root; //holds root of bare repo serving as persistence backend
    @NonNull File tmp; //dedicated tmp dir; holds working copies of root

    //initialized:
    @NonNull Git workingCopy; //duh

    public JGitStoryteller(@NonNull File root, @NonNull File tmp) {
        this.root = root;
        this.tmp = tmp;
        init();
    }

    private void init(){
        ensureRoot();
        ensureTmp();
        if (rootIsEmpty()){
            initializeBareRepostiory();
            initializeWorkingCopy();
            createInitialRefs();

        } else {
            //if root is not empty, then we need to assume that it is bare repo
            //we check that simply by cloning it; if it clones alright, its effectively a bare repo
            //if cloning fails, its not a proper bare repo
            initializeWorkingCopy();
            safeguardEmptyTagExists();
        }
    }

    @SneakyThrows
    private void initializeWorkingCopy() {
        String workingCopyId = UUID.randomUUID().toString();
        File workingCopyDir = new File(tmp, workingCopyId);
        workingCopyDir.mkdirs();
        workingCopy = Git.cloneRepository()
            .setDirectory(workingCopyDir)
            .setURI(root.toURI().toString())
            .setBare(false)
            .call();
    }

    private void ensureRoot(){
        if (root.exists() && !root.isDirectory())
            throw new RuntimeException(); //todo
        root.mkdirs();

    }

    @SneakyThrows
    private void createInitialRefs(){
        //todo why am I not using WorkspaceManager and friends?
        workingCopy.branchCreate().setName("master").setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);
//        workingCopy.checkout().setName("master").call();
        var gitignoreFile = new File(workingCopy.getRepository().getWorkTree(), ".gitignore");
        if (!gitignoreFile.exists()) {
            write(gitignoreFile.toPath(), GITIGNORE_BYTES);
            workingCopy.add().addFilepattern(".gitignore").call();
            workingCopy.commit().setMessage("Storyteller-compliant repository state").call();
            workingCopy.push().add("master").call();
        } else {
            var foundLines = asList(readString(gitignoreFile.toPath()).split("\\n"));
            for (var line: GITIGNORE_LINES)
                invariant(
                    foundLines.contains(line), //fixme not perfect, allows for commenting it out
                    "Line '"+line+"' must be found in .gitignore file"
                );
        }

        var tag = workingCopy.tagList().call().stream().map(Ref::getName).filter("empty"::equals).findAny();
        if (tag.isPresent()) {
            workingCopy.checkout().setCreateBranch(false).setName("empty");
            invariant(
                readString(gitignoreFile.toPath()).equals(GITIGNORE_CONTENT),
                ".gitignore file matches prepared patterns"
            );
            workingCopy.checkout().setName("master").call();
        } else {
            workingCopy.tag().setName("empty").call();
            workingCopy.push().setPushTags().call();
        }
    }

    @SneakyThrows
    private void safeguardEmptyTagExists(){
        invariant(
            workingCopy.tagList().call().stream().anyMatch(r -> "refs/tags/empty".equals(r.getName())),
            "existing repo must contain 'empty' tag" // tag which must only have .toryteller marker file" //todo
        );
    }

    private boolean rootIsEmpty(){
        return root.isDirectory() && root.listFiles(File::isFile).length <= 1; //& name == .storyteller and .storyteller is empty
    }

    @SneakyThrows
    private void initializeBareRepostiory(){
        Git.init().setDirectory(root).setBare(true).call();
    }

    private void ensureTmp(){
        if (tmp.exists()) {
            if (tmp.isDirectory())
                tmp.delete();
            else
                throw new RuntimeException(); //todo tmp points to file not dir
        }
        tmp.mkdirs();
    }

    @Override
    @SneakyThrows
    public void tell(String storyName, ActionBody<ArcClosure> arcClosure) {
        var manager = new GitManager(root, tmp);
        var story = new Story(EpisodeId.nonRandomId(EpisodeType.STORY, storyName), storyName, arcClosure);
        //todo empty?
        var workspace = manager.getWorkspace(story.getEpisodeId().toString(), true);
        manager.cloneInto(workspace);
        var git = manager.open(workspace).getRepository();
        git.pull().setTagOpt(TagOpt.FETCH_TAGS).call();
//        git.checkout().setName("empty").call(); //todo ?? good idea or not?
        var branchName = buildRefName(story.getEpisodeId(), PROGRESS);
        git.checkout()
            .setCreateBranch(true)
            .setName(branchName)
            .setStartPoint("empty")
            .call();
        git.push()
            .setRemote("origin")
            .setRefSpecs(asList(new RefSpec(branchName+":"+branchName)))
            .call();
        try {
            story.tell(TaleContext.of(workspace, manager));
        } catch (Exception e){
            if (e instanceof  ToBeContinuedException) {
                log.atInfo().log("Story "+storyName+" is yet to be continued");
            } else {
                log.atSevere().withCause(e).log("ERROR: %s", e);
                throw e;
            }
        } finally {
            git.push().setPushAll().setPushTags().call();
            log.atInfo().log("Current progress saved");
        }
    }
}
