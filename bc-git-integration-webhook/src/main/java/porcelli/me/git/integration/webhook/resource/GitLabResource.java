package porcelli.me.git.integration.webhook.resource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.json.JSONObject;
import org.json.JSONTokener;
import porcelli.me.git.integration.common.model.Payload;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;


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
