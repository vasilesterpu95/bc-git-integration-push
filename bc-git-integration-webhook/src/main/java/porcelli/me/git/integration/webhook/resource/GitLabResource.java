package porcelli.me.git.integration.webhook.resource;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.gitlab4j.api.systemhooks.AbstractSystemHookEvent;
import org.gitlab4j.api.systemhooks.SystemHookEvent;
import org.gitlab4j.api.webhook.AbstractEvent;
import org.gitlab4j.api.webhook.Event;
import org.gitlab4j.api.webhook.MergeRequestEvent;
import org.gitlab4j.api.webhook.PushEvent;
import org.json.JSONObject;
import org.json.JSONTokener;
import porcelli.me.git.integration.common.model.Payload;
import porcelli.me.git.integration.common.model.PullRequestEvent;


@Path("/gitlab")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GitLabResource extends HookResourceBase {

    @POST
    @Path("/")
    public Response post(@HeaderParam("X-Gitlab-Event") String eventType,
                         @Context HttpServletRequest request) {
        try (InputStream in = request.getInputStream()) {
            Payload.EventType type = Payload.EventType.valueOf(eventType.replace(" ", "_").toUpperCase());

//            MergeRequestEvent event = objectMapper.readValue(in, MergeRequestEvent.class);
            JSONObject event = new JSONObject(new JSONTokener(in));

            switch (type) {
                case MERGE_REQUEST_HOOK:
                    bcIntegration.onPullRequest(event);
                    break;
                case PUSH_HOOK:
                    bcIntegration.onPush(event);
                    break;
                default:
                    break;
            }

            return Response.ok().build();
        } catch (JsonParseException | JsonMappingException e) {
            e.printStackTrace();
            throw new InternalServerErrorException(e.getMessage(), Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage() + "\n").build(), e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e);
        } catch (Exception e) {
            throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                           .entity(e.getMessage()).build(), e);
        }
    }
}
