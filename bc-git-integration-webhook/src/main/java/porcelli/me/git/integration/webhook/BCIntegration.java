package porcelli.me.git.integration.webhook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.jgit.api.Git;

public interface BCIntegration {
    public void onPush(final Object pushEvent) throws Exception;

    public void onPullRequest(final Object pullRequestEvent) throws Exception;

    public Git getGit(Object repository)  throws Exception
    ;

    default File tempDir(String reponame) throws IOException {
        return Files.createTempDirectory(new File(System.getProperty("java.io.tmpdir")).toPath(), "temp").resolve(reponame).toFile();
    }
}
