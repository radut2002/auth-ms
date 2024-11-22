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

import auth.ms.jwt_server.domain.User;
import auth.ms.jwt_server.utils.GenerateTokenUtils;
import auth.ms.response_utils.ResponseUtils;
import io.quarkus.security.AuthenticationFailedException;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;

@RequestScoped
@Path("/auth/verify")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class VerifyTokenResource {             
    
    private final JWTParser parser;

    private final JsonWebToken jwt;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    public VerifyTokenResource(JsonWebToken jwt, JWTParser parser) {
        this.jwt = jwt; 
        this.parser= parser;       
    }

    @GET
    public Response verifyToken() {          
        if (jwt == null) {
            return ResponseUtils.textResponse(Status.BAD_REQUEST, "invalid token");
        }               

        JsonWebToken token;
        try {
            token = parser.parse(jwt.getRawToken());                        
        } catch (ParseException | AuthenticationFailedException e) {                      
            return ResponseUtils.textResponse(Status.EXPECTATION_FAILED, String.format("invalid token :%s", e.getCause()));
        }    
        
        if (!GenerateTokenUtils.SUBJECT_ACCESS.equals(token.getSubject())) {
            return ResponseUtils.textResponse(Status.BAD_REQUEST, "invalid subject");
        }
        
        long userId;
        try {
            userId = Long.parseLong(token.getName());
        } catch (NumberFormatException e) {
            return ResponseUtils.textResponse(Status.BAD_REQUEST,
                    "userId inside upn cannot be parsed");
        }                
        
        var user= new User(userId, token.getGroups());

        return ResponseUtils.textResponse(Status.OK, user.toString());
    }
}
