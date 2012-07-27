package org.neo4j.twitter_graph.services;

import org.neo4j.twitter_graph.domain.Follows;
import org.neo4j.twitter_graph.domain.Tweet;
import org.neo4j.twitter_graph.domain.Tag;
import org.neo4j.twitter_graph.domain.User;
import org.neo4j.twitter_graph.repositories.TagRepository;
import org.neo4j.twitter_graph.repositories.TweetRepository;
import org.neo4j.twitter_graph.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.*;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mh
 * @since 24.07.12
 */
@Service
public class TwitterService {
    private static final Pattern MENTION = Pattern.compile("@(\\p{Alnum}{3,})");
    private static final Pattern TAG = Pattern.compile("#(\\p{Alnum}{3,})");
    @Autowired
    UserRepository userRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    TweetRepository tweetRepository;

    @Transactional
    public List<Tweet> importTweets(String search) {
        return importTweets(search,null);
    }

    @Transactional
    public List<Tweet> importTweets(String search, Long lastTweetId) {
        System.out.println("Importing for " +search+ ", max tweet id: "+lastTweetId);

        final SearchOperations searchOperations = new TwitterTemplate().searchOperations();
        
        final SearchResults results = lastTweetId==null ? searchOperations.search(search,1,200) : searchOperations.search(search,1,200,lastTweetId,Long.MAX_VALUE);

        final List<Tweet> result = new ArrayList<Tweet>();
        for (org.springframework.social.twitter.api.Tweet tweet : results.getTweets()) {
            result.add(importTweet(tweet));
        }
        return result;
    }

    @Transactional
    protected Tweet importTweet(org.springframework.social.twitter.api.Tweet source) {
        final String userName = source.getFromUser();
        User user = userRepository.save(new User(userName));
        final String text = source.getText();
        final Tweet tweet = new Tweet(source.getId(), user, text);
        System.out.println("Imported " + tweet);
        addMentions(tweet, text);
        addTags(tweet, text);
        addOriginalTweet(tweet, source.getInReplyToStatusId());
        return tweetRepository.save(tweet);
    }

    @Transactional
    public void connectFollowers() {
        final FriendOperations friendOperations = new TwitterTemplate().friendOperations();
        Map<String,User> users=new HashMap<String, User>(); 
        for (User user : userRepository.findAll()) {
            users.put(user.getUser(),user);    
        }
        for (Map.Entry<String, User> entry : users.entrySet()) {
            addFriends(friendOperations, users, entry);
        }
    }

    private void addFriends(FriendOperations friendOperations, Map<String, User> users, Map.Entry<String, User> entry) throws InterruptedException {
        try {
            final String name = entry.getKey();
            final User user = entry.getValue();
            for (TwitterProfile profile : friendOperations.getFriends(name)) {
                final User friend = users.get(profile.getScreenName());
                if (friend == null) continue;
                System.out.println(name + " FOLLOWS " + friend.getUser());
                userRepository.createRelationshipBetween(user, friend, Follows.class, "FOLLOWS");
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        }
    }

    private void addMentions(Tweet tweet, String text) {
        for (String mention : extractMentions(text)) {
            tweet.addMention(userRepository.save(new User(mention)));
        }
    }

    private void addTags(Tweet tweet, String text) {
        for (String tag : extractTokens(text, TAG)) {
            tweet.addTag(tagRepository.save(new Tag(tag)));
        }
    }

    private void addOriginalTweet(Tweet tweet, final Long replyId) {
        if (replyId == null) return;
        final Tweet source = tweetRepository.findByTweetId(replyId);
        if (source == null) return;
        tweet.setSource(source);
    }

    public Set<String> extractMentions(String text) {
        return extractTokens(text, MENTION);
    }

    public Set<String> extractTokens(String text, Pattern p) {
        final Matcher matcher = p.matcher(text);
        Set<String> result=new LinkedHashSet<String>();
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
}
