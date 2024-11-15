package auth.ms.jwt_server;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import auth.ms.jwt_server.services.external.TokenStoreService;
import auth.ms.jwt_server.utils.GenerateTokenUtils;
import auth.ms.response_utils.RefreshTokenCookie;
import auth.ms.response_utils.ResponseUtils;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;



@RequestScoped
@Path("/api/auth/verify")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class VerifyTokenResource {             

    @RestClient
    @Inject
    private final TokenStoreService tokenStoreService;

    @Inject 
    JWTParser parser;

    private final JsonWebToken jwt;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    public VerifyTokenResource(JsonWebToken jwt, @RestClient TokenStoreService tokenStoreService) {
        this.jwt = jwt;        
        this.tokenStoreService = tokenStoreService;
    }

    @GET
    public Response verifyToken(@Context SecurityIdentity ctx)  {          
        if (jwt == null) {
            return ResponseUtils.textResponse(Status.BAD_REQUEST, "invalid token");
        }

        if (!GenerateTokenUtils.SUBJECT_REFRESH.equals(jwt.getSubject())) {
            return ResponseUtils.textResponse(Status.BAD_REQUEST, "invalid subject");
        }

        try {
            parser.parse(jwt.getRawToken());            
        } catch (ParseException e) {
            return ResponseUtils.textResponse(Status.BAD_REQUEST, String.format("invalid token :%s", e.getMessage()));
        }
        
        long userId;
        try {
            userId = Long.parseLong(jwt.getName());
        } catch (NumberFormatException e) {
            return ResponseUtils.textResponse(Status.BAD_REQUEST,
                    "userId inside upn cannot be parsed");
        }
         
        //var tokenHash = RefreshTokenUtils.toSha256Hash(jwt.getRawToken());
        var groupsResponse = tokenStoreService.popGroups(userId, jwt.getRawToken());

        // if no groups were found (404), the JWT info was deleted inside the token store
        // (e.g. because it expired)
        // in this case, delete the cookie on the client side (max-age=0 acts as a delete-cookie),
        // as the refresh token cannot be used anymore, the client has to re-authenticate
        if (groupsResponse.getStatusInfo().getFamily() == Status.Family.CLIENT_ERROR) {
            var deleteCookie = new RefreshTokenCookie("", 0);
            return ResponseUtils.status(Status.BAD_REQUEST, deleteCookie);
        }
        if (groupsResponse.getStatusInfo().getFamily() == Status.Family.SERVER_ERROR) {
            return ResponseUtils.status(Status.INTERNAL_SERVER_ERROR);
        }   
        
        return ResponseUtils.status(Status.OK);
    }
}
