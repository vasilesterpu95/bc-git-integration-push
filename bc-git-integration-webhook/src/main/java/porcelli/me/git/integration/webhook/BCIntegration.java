package porcelli.me.git.integration.webhook;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public interface BCIntegration {
    void onPush(final Object pushEvent) throws Exception;

    void onPullRequest(final Object pullRequestEvent) throws Exception;

    Git getGit(Object repository)  throws Exception;

    default File tempDir(String repoName) throws IOException {
        return Files.createTempDirectory(new File(System.getProperty("java.io.tmpdir")).toPath(), "temp").resolve(repoName).toFile();
    }
}
