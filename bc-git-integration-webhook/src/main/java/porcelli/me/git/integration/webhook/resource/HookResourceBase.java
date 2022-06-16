package porcelli.me.git.integration.webhook.resource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import porcelli.me.git.integration.common.properties.GitRemoteProperties;
import porcelli.me.git.integration.webhook.BCIntegration;
import porcelli.me.git.integration.common.json.MappingModule;
import porcelli.me.git.integration.webhook.GitHubIntegrationImpl;
import porcelli.me.git.integration.webhook.GitLabIntegrationImpl;

public abstract class HookResourceBase {

    protected final ObjectMapper objectMapper;
    protected final BCIntegration bcIntegration;

    public HookResourceBase() {
        final GitRemoteProperties properties = new GitRemoteProperties();
        final MappingModule module = new MappingModule();
        objectMapper = new ObjectMapper();

        if(properties.getGitProvider().name().equals("GIT_HUB")){
            bcIntegration = new GitHubIntegrationImpl(properties);
            objectMapper.registerModule(module);
        } else if(properties.getGitProvider().name().equals("GIT_LAB")){
            bcIntegration = new GitLabIntegrationImpl(properties);
            objectMapper.registerModule(new JavaTimeModule());
        } else {
            bcIntegration = new GitHubIntegrationImpl(properties);
            objectMapper.registerModule(module);
        }
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }
}
