package org.neo4j.twitter_graph;

import org.neo4j.twitter_graph.domain.Tweet;
import org.neo4j.twitter_graph.services.TwitterService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author mh
 * @since 25.07.12
 */
public class TwitterGraph {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.out.println("Usage: TwitterGraph embedded|server #tag");
            return;
        }
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:TwitterGraph-"+args[0]+".xml");
        addShutdownHook(ctx);
        final TwitterService service = ctx.getBean(TwitterService.class);
        Long lastTweetId=args.length==3 ? Long.parseLong(args[2]) : null;
        while (true) {
            final String search = args[1];
            final List<Tweet> tweets = service.importTweets(search,lastTweetId);
            if (!tweets.isEmpty()) {
                lastTweetId = maxTweetId(tweets);
                service.connectFollowers();
            }
            Thread.sleep(TimeUnit.MINUTES.toMillis(5));
        } 
    }

    private static Long maxTweetId(List<Tweet> tweets) {
        final Tweet maxTweet = Collections.max(tweets, new TweetComparator());
        return maxTweet!=null ? maxTweet.getTweetId() : null;
    }

    private static void addShutdownHook(final ClassPathXmlApplicationContext ctx) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                ctx.close();
            }
        });
    }

    private static class TweetComparator implements Comparator<Tweet> {
        public int compare(Tweet o1, Tweet o2) {
            return o1.getTweetId().compareTo(o2.getTweetId());
        }
    }
}
