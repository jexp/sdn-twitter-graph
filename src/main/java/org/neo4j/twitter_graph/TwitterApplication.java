
package org.neo4j.twitter_graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.twitter_graph.services.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableNeo4jRepositories(basePackages = "org.neo4j.twitter_graph.repositories")
@Import(RepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan(basePackages = "org.neo4j.twitter_graph.services")
public class TwitterApplication extends Neo4jConfiguration {

    public TwitterApplication() {
	     setBasePackage("org.neo4j.twitter_graph.domain");
    }
	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new GraphDatabaseFactory().newEmbeddedDatabase("twitter.db");
	}

    @Bean
    public TwitterTemplate twitterTemplate() {
        return new TwitterTemplate(System.getenv("TWITTER_BEARER"));
    }

	public static void main(String[] args) {
		SpringApplication.run(TwitterApplication.class, args);
	}
}
