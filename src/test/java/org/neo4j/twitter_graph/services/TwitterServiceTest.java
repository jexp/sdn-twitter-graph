package org.neo4j.twitter_graph.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.twitter_graph.domain.Tag;
import org.neo4j.twitter_graph.domain.Tweet;
import org.neo4j.twitter_graph.domain.User;
import org.neo4j.twitter_graph.repositories.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.IteratorUtil.first;

/**
 * @author mh
 * @since 24.07.12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TwitterServiceTest {
    @Autowired
    TwitterService twitterService;
    @Autowired
    TweetRepository tweetRepository;
    @Test
    @Transactional
    public void testImportSimpleTweet() throws Exception {
        final org.springframework.social.twitter.api.Tweet source = new org.springframework.social.twitter.api.Tweet(123L, "Text", null, "springsource", null, null, 234L, null, null);
        final Tweet tweet = twitterService.importTweet(source);
        assertNotNull(tweet.getId());
        assertEquals((Long)123L,tweet.getTweetId());
        final User sender = tweet.getSender();
        assertNotNull(sender.getId());
        assertEquals("springsource", sender.getUser());
    }
    @Test
    @Transactional
    public void testImportTweetWithMentions() throws Exception {
        final org.springframework.social.twitter.api.Tweet source = new org.springframework.social.twitter.api.Tweet(123L, "Text @mesirii", null, "springsource", null, null, 234L, null, null);
        final Tweet tweet = twitterService.importTweet(source);
        assertEquals("mesirii", first(tweet.getMentions()).getUser());
    }
    @Test
    @Transactional
    public void testImportTweetWithTags() throws Exception {
        final org.springframework.social.twitter.api.Tweet source = new org.springframework.social.twitter.api.Tweet(123L, "Text #neo4j", null, "springsource", null, null, 234L, null, null);
        final Tweet tweet = twitterService.importTweet(source);
        assertEquals("neo4j", first(tweet.getTags()).getTag());
    }
    @Test
    @Transactional
    public void testFindTweetsByTag() throws Exception {
        final org.springframework.social.twitter.api.Tweet source = new org.springframework.social.twitter.api.Tweet(123L, "Text #neo4j", null, "springsource", null, null, 234L, null, null);
        twitterService.importTweet(source);
        final Collection<Tweet> tweets = tweetRepository.findByTagsTag("neo4j");
        final Tweet tweet = first(tweets);
        assertEquals("neo4j", first(tweet.getTags()).getTag());
        assertEquals((Long)123L, tweet.getTweetId());
    }

    @Test
    @Transactional
    public void testImportTweets() throws Exception {
        Collection<Tweet> tweets =twitterService.importTweets("#neo4j");
        Tweet tweet = first( tweets );

        boolean found=false;
        for ( Tag tag : tweet.getTags() )
        {
            final String tagName = tag.getTag();
            if (tagName.equalsIgnoreCase("neo4j")) found=true;
        }
        assertEquals("found neo4j tag", true, found );
    }

    @Test
    public void testExtractMentions() throws Exception {
       assertThat(twitterService.extractMentions("test @mesir11 test"), hasItems("mesir11"));
       assertThat(twitterService.extractMentions("test @mesir11"), hasItems("mesir11"));
       assertThat(twitterService.extractMentions("@mesir11 test"), hasItems("mesir11"));
       assertThat(twitterService.extractMentions("@mesir11 test @springsource"), hasItems("mesir11","springsource"));
    }
}
