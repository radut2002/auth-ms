package auth.ms.jwt_server;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import auth.ms.jwt_server.services.external.TokenStoreService;

@RequestScoped
@Path("/auth/remove")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class RemoveTokensResource {

    private final TokenStoreService tokenStoreService;

    @Inject
    public RemoveTokensResource(@RestClient TokenStoreService tokenStoreService) {
        this.tokenStoreService = tokenStoreService;
    }

    @DELETE
    @Path("/{userId}")
    public Response removeTokens(@PathParam("userId") long userId) {
        return tokenStoreService.deleteForUser(userId);
    }
}
