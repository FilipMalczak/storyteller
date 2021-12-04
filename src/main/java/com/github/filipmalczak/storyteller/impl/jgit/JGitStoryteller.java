package com.github.filipmalczak.storyteller.impl.jgit;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.ToBeContinuedException;
import com.github.filipmalczak.storyteller.impl.jgit.storage.GitManager;
import com.github.filipmalczak.storyteller.impl.jgit.story.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.story.Story;
import com.github.filipmalczak.storyteller.impl.jgit.story.Arc;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.github.filipmalczak.storyteller.impl.jgit.story.Safeguards.safeguard;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

//todo what pkg should this be in?
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class JGitStoryteller implements Storyteller {
    File root; //holds root of bare repo serving as persistence backend
    File tmp; //dedicated tmp dir; holds working copies of root
    Git rootGit;
    Git workingCopy;

    public JGitStoryteller(File root, File tmp) {
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

//            .setNoCheckout(true)
            .call();
    }

    private void ensureRoot(){
        if (root.exists() && !root.isDirectory())
            throw new RuntimeException(); //todo
        root.mkdirs();

    }

//    private static String gitignoreContent = "";

    //https://git-scm.com/docs/gitignore
    //last example, version 2.34.1
    private static String gitignoreContent = """
            /*
            !/documents
            !/properties
            !/files
            !/.episode-index
            !/.gitignore
            """;

    private static byte[] gitignoreBytes = gitignoreContent.getBytes(StandardCharsets.UTF_8);

    private static List<String> gitignoreLines = asList(gitignoreContent.split("\\n"));

    @SneakyThrows
    private void createInitialRefs(){
        //todo why am I not using WorkspaceManager and friends?
        workingCopy.branchCreate().setName("master").setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);
//        workingCopy.checkout().setName("master").call();
        var gitignoreFile = new File(workingCopy.getRepository().getWorkTree(), ".gitignore");
        if (!gitignoreFile.exists()) {
            write(gitignoreFile.toPath(), gitignoreBytes);
            workingCopy.add().addFilepattern(".gitignore").call();
            workingCopy.commit().setMessage("Storyteller-compliant repository state").call();
            workingCopy.push().add("master").call();
        } else {
            var foundLines = asList(readString(gitignoreFile.toPath()).split("\\n"));
            for (var line: gitignoreLines)
                safeguard(
                    foundLines.contains(line), //fixme not perfect, allows for commenting it out
                    "Line '"+line+"' must be found in .gitignore file"
                );
        }

        var tag = workingCopy.tagList().call().stream().map(Ref::getName).filter("empty"::equals).findAny();
        if (tag.isPresent()) {
            workingCopy.checkout().setCreateBranch(false).setName("empty");
            safeguard(
                readString(gitignoreFile.toPath()).equals(gitignoreContent),
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
        safeguard(
            workingCopy.tagList().call().stream().anyMatch(r -> "refs/tags/empty".equals(r.getName())),
            "existing repo must contain 'empty' tag" // tag which must only have .toryteller marker file" //todo
        );
    }

    @SneakyThrows
    private void ensureRootIsRepo(){
        rootGit = Git.open(root); //todo will throw if not repo
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
        git.checkout().setName("empty").call(); //?? good idea or not?
        try {
            story.tell(workspace, manager);
        } catch (ToBeContinuedException e){
            log.info("Story {} is yet to be continued");
        }
    }
}
