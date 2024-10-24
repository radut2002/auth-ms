package auth.ms.jwt_server.domain;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.util.Set;

public class User {

    public final long id;
    public final Set<String> groups;

    @JsonbCreator
    public User(@JsonbProperty("id") long id, @JsonbProperty("groups") Set<String> groups) {
        this.id = id;
        this.groups = groups;
    }
}
