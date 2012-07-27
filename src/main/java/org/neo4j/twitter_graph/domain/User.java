package org.neo4j.twitter_graph.domain;

import org.springframework.data.neo4j.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mh
 * @since 24.07.12
 */
@NodeEntity
public class User {
    @GraphId Long id;

    @Indexed(unique=true) String user;

    public User() {
    }

    public User(String user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "@"+ user;
    }
}
