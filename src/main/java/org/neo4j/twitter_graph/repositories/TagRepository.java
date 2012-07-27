package org.neo4j.twitter_graph.repositories;

import org.neo4j.twitter_graph.domain.Tag;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * @author mh
 * @since 24.07.12
 */
public interface TagRepository extends GraphRepository<Tag> {
}
