package auth.ms.jwt_server.domain;

import java.util.Set;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class JwtResponse {

    public final long userId;
    public final String accessToken;
    public final long expiresAt;
    public final Set<String> groups;

    public JwtResponse(long userId, String accessToken, long expiresAt, Set<String> groups) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.groups = groups;
    }
}
