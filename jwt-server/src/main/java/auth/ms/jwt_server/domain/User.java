package auth.ms.jwt_server.domain;

import java.util.Set;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class User {

    public final long id;
    public final Set<String> groups;

    @JsonbCreator
    public User(@JsonbProperty("id") long id, @JsonbProperty("groups") Set<String> groups) {
        this.id = id;
        this.groups = groups;
    }

    @Override
    public String toString() {
        Jsonb jsonb = JsonbBuilder.create();
        return jsonb.toJson(this);        
    }
}