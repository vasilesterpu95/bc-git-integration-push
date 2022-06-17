package porcelli.me.git.integration.webhook;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.*;

import org.gitlab4j.api.webhook.EventRepository;
import org.gitlab4j.api.webhook.MergeRequestEvent;
import org.gitlab4j.api.webhook.PushEvent;
import org.json.JSONObject;
import porcelli.me.git.integration.common.integration.BCRemoteIntegration;
import porcelli.me.git.integration.common.integration.GitRemoteIntegration;
import porcelli.me.git.integration.common.model.PullRequestEvent;
import porcelli.me.git.integration.common.properties.GitRemoteProperties;

public class GitLabIntegrationImpl implements BCIntegration {
    private final Map<String, Repository> repositoryMap = new HashMap<>();

    private final GitRemoteProperties properties;
    private final GitRemoteIntegration integration;
    private final BCRemoteIntegration bcRemoteIntegration;
    private final TransportConfigCallback transportConfigCallback;

    public GitLabIntegrationImpl(GitRemoteProperties properties) {
        this.properties = properties;

        if (!properties.validate()) {
            throw new IllegalStateException("Invalid properties file.");
        }

        integration = properties.getGitProvider().getRemoteIntegration(properties);

        bcRemoteIntegration = new BCRemoteIntegration(properties);

        transportConfigCallback = transport -> {
            final SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host host, Session session) {
                    // additional configurations can be set here
                }
            });
        };
    }


    @Override

    public void onPush(final Object pushEvent) throws GitAPIException, URISyntaxException {
        // TODO: 16.06.2022 FIX THIS 
        JSONObject event = (JSONObject) pushEvent;
        if (!event.getString("ref").contains("master")) {
            return;
        }
        final Git git = getGit(event.getJSONObject("project"));

        try {
            final PullCommand pullCommandFromBC = git.pull().setRemote(BCRemoteIntegration.ORIGIN_NAME).setRebase(true);
            final PullCommand pullCommandFromService = git.pull().setRemote(integration.getOriginName()).setRebase(true);
            final PushCommand pushCommandToBC = git.push().setRemote(BCRemoteIntegration.ORIGIN_NAME).setForce(true);
            final PushCommand pushCommandToService = git.push().setRemote(integration.getOriginName()).setForce(true);

            if (properties.getUseSSH()) {
                pullCommandFromBC.setTransportConfigCallback(transportConfigCallback);
                pullCommandFromService.setTransportConfigCallback(transportConfigCallback);
                pushCommandToBC.setTransportConfigCallback(transportConfigCallback);
                pushCommandToService.setTransportConfigCallback(transportConfigCallback);
            } else {
                pullCommandFromBC.setCredentialsProvider(bcRemoteIntegration.getCredentialsProvider());
                pullCommandFromService.setCredentialsProvider(integration.getCredentialsProvider());
                pushCommandToBC.setCredentialsProvider(bcRemoteIntegration.getCredentialsProvider());
                pushCommandToService.setCredentialsProvider(integration.getCredentialsProvider());
            }

            pullCommandFromBC.call();
            pullCommandFromService.call();
            pushCommandToBC.call();
            pushCommandToService.call();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @Deprecated
    public void onPullRequest(final Object pullRequestEvent) throws GitAPIException, URISyntaxException {
        JSONObject event = (JSONObject) pullRequestEvent;
        JSONObject evtAttr = event.getJSONObject("object_attributes");
        if (!evtAttr.get("state").equals("merged")) {
            return;
        }
        final String branchName = evtAttr.getString("source_branch");
        final Git git = getGit(event.get("project"));
        git.branchDelete().setBranchNames("refs/heads/" + branchName).call();
        final RefSpec refSpec = new RefSpec().setSource(null).setDestination("refs/heads/" + branchName);
        git.push().setRefSpecs(refSpec).setRemote("origin").call();
    }

    @Override
    public Git getGit(Object repository)
            throws GitAPIException, URISyntaxException {
        JSONObject evtProject = (JSONObject) repository;
        final Git git;
        if (!repositoryMap.containsKey(evtProject.getString("description"))) {
            final String bcRepo = evtProject.getString("description");

            try {
                final CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(bcRepo)
                        .setDirectory(tempDir(evtProject.getString("name")));

                if (properties.getUseSSH()) {
                    cloneCommand.setTransportConfigCallback(transportConfigCallback);
                } else {
                    cloneCommand.setCredentialsProvider(bcRemoteIntegration.getCredentialsProvider());
                }

                git = cloneCommand.call();

                final RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName(integration.getOriginName());
                remoteAddCommand.setUri(new URIish(evtProject.getString("http_url")));
                remoteAddCommand.call();
                repositoryMap.put(evtProject.getString("description"), git.getRepository());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }


        } else {
            git = new Git(repositoryMap.get(evtProject.getString("description")));
        }
        return git;
    }
}
