package auth.ms.jwt_server.services.external;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.core.ServerResponse;

import auth.ms.jwt_server.domain.TokenData;

@Path("/auth/tokens")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
@ApplicationScoped
@RegisterRestClient
public interface TokenStoreService {

    Logger LOG = Logger.getLogger("TokenStoreService");

    String STORE_FALLBACK_ERROR_MSG = "Fallback method for 'TokenStoreService#store' used because original method failed! Token is NOT being stored! Returning success though so the rest can continue.";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 1, delay = 3000)
    @Fallback(fallbackMethod = "storeFallback")
    Response store(TokenData tokenData);

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 1, delay = 3000)
    Response popGroups(@PathParam("userId") long userId,
            @QueryParam("token-hash") String tokenHash);

    @DELETE
    @Path("/{userId}")
    Response deleteForUser(@PathParam("userId") long userId);

    @SuppressWarnings("unused")
    private static Response storeFallback(TokenData tokenData) {
        LOG.error(STORE_FALLBACK_ERROR_MSG);
        return new ServerResponse();
    }
}
