package de.dopler.ms.login_server.services.external;

import de.dopler.ms.login_server.domain.User;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
@RegisterRestClient
public interface TokenService {

    @POST
    @Path("/generate")
    @Retry(maxRetries = 1, delay = 3000)
    Response forUser(User user);

    @POST
    @Path("/refresh")
    @Retry(maxRetries = 1, delay = 3000)
    Response fromRefreshToken(@CookieParam("r_token") String refreshToken);

    @DELETE
    @Path("/remove/{userId}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Retry(maxRetries = 1, delay = 3000)
    Response removeTokens(@PathParam("userId") long userId);
}
