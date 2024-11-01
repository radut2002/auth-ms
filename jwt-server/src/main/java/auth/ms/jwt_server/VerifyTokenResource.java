package auth.ms.jwt_server;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.jwt.JsonWebToken;

import auth.ms.response_utils.ResponseUtils;
import io.smallrye.jwt.auth.principal.JWTParser;


@RequestScoped
@Path("/auth/verify")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class VerifyTokenResource {
      
    private JsonWebToken jwt;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    public VerifyTokenResource(JsonWebToken jwt, JWTParser parser) {
        this.jwt = jwt;

    }

    @GET
    public Response verify()  {        
        return ResponseUtils.textResponse(Status.OK, jwt.toString());
    }
}
