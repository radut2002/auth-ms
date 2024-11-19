package auth.ms.login_server.services.external;

import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import auth.ms.login_server.domain.Credentials;

@Path("/auth/credentials")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
@Singleton
@RegisterRestClient
public interface CredentialsStoreService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 1, delay = 3000)
    Response storeCredentials(Credentials credentials);

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 1, delay = 3000)
    Response getAuthData(@PathParam("username") String username);

    @DELETE
    @Path("/{id}")
    @Retry(maxRetries = 1, delay = 3000)
    Response removeCredentials(@PathParam("id") long id);

    @PUT
    @Path("/{id}/username")
    @Retry(maxRetries = 1, delay = 3000)
    Response updateUsername(@PathParam("id") long id, String newUsername);

    @PUT
    @Path("/{id}/secret")
    @Retry(maxRetries = 1, delay = 3000)
    Response updateSecret(@PathParam("id") long id, String newSecret);

    @PUT
    @Path("/{id}/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 1, delay = 3000)
    Response updateGroups(@PathParam("id") long id, Set<String> newGroups);
}
