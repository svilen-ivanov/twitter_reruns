package com.buhtum.twr;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple class that implements a few features that we can write unit tests for. It uses Google Guava for the sole
 * reason that we have an external compile-time dependency in this bootstrap project.
 * <p>
 * To make the example code simpler this class does not follow pure OO style. (If it did, we would create a Painting
 * object from painting elements and let our painter paint that painting object, for instance.)
 * <p>
 * "And from all of us here I'd like to wish you happy painting and God bless, my friend."
 */
public class Main {

    private final static Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        final Twitter twitter = getInstance();
        try {
            log.debug(String.valueOf(twitter.getScreenName()));
        } catch (TwitterException e) {
            throw new RuntimeException();
        }

        final ArchiveReader reader = new ArchiveReader();
        reader.load(System.getProperty("archive"));
        final DateTime now = DateTime.now();

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        for (final Tweet tweet : reader.getTweets()) {
            final DateTime newTime = tweet.getTimestamp().plusYears(5);
            if (newTime.isAfter(now)) {
                int delay = (Seconds.secondsBetween(DateTime.now(), newTime)).getSeconds();
                log.debug("Scheduled tweet from " + tweet.getTimestamp() + " for " + newTime + " (" + delay + " seconds): " + tweet.getText());
                scheduler.schedule(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (tweet.getInReplyToStatusId() == null && tweet.getInReplyToUserId() == null) {
                                        if (tweet.getRetweetedStatusId() != null) {
                                            log.debug("Retweeting: " + tweet.getText());
                                            twitter.retweetStatus(tweet.getRetweetedStatusId());
                                        } else {
                                            log.debug("Tweeting: " + tweet.getText());
                                            twitter.updateStatus(tweet.getText());
                                        }
                                    }
                                    try {
                                        TimeUnit.SECONDS.sleep(10);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if (tweet.getInReplyToUserId() != null) {
                                        tryFollowing(twitter, tweet.getInReplyToUserId());
                                    }
                                    try {
                                        TimeUnit.SECONDS.sleep(10);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if (tweet.getRetweetedStatusUserId() != null) {
                                        tryFollowing(twitter, tweet.getRetweetedStatusUserId());
                                    }

                                } catch (TwitterException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            private void tryFollowing(Twitter twitter, long userId) throws TwitterException {
                                final ResponseList<Friendship> friendships = twitter.lookupFriendships(userId);
                                for (Friendship friendship : friendships) {
                                    if (!friendship.isFollowing()) {
                                        log.debug("Following: " + friendship.getScreenName());
                                        twitter.createFriendship(friendship.getId(), true);
                                    } else {
                                        log.debug("Already following: " + friendship.getScreenName());
                                    }
                                }
                            }
                        },
                        delay,
                        TimeUnit.SECONDS);
            }
        }

        final Timer followFollowers = new Timer();
        final DateTime start = new DateTime().withTimeAtStartOfDay().plusHours(10);
        log.info("Started follow back users, starting: " + start);
        followFollowers.schedule(new TimerTask() {
            @Override
            public void run() {
                followFollowers();
            }
        }, start.toDate(), TimeUnit.DAYS.toMillis(1));
    }

    private static void followFollowers() {
        try {
            followFollowers(-1);
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
    }

    private static void followFollowers(long cursor) throws TwitterException {
        final Twitter twitter = getInstance();
        final IDs followersIDs = twitter.getFollowersIDs(cursor);
        for (Friendship friendship : twitter.lookupFriendships(followersIDs.getIDs())) {
            if (!friendship.isFollowing()) {
                log.debug("Following: " + friendship.getScreenName());
                twitter.createFriendship(friendship.getId(), true);
            } else {
                log.debug("Already following: " + friendship.getScreenName());
            }
        }
        if (followersIDs.hasNext()) followFollowers(followersIDs.getNextCursor());
    }


    public static Twitter getInstance() {

        try {
            final String configFile = System.getProperty("auth");
            Properties twitterConfig = loadConfig(configFile);

            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(
                    twitterConfig.getProperty("twitter4j.oauth.consumerKey"),
                    twitterConfig.getProperty("twitter4j.oauth.consumerSecret"));

            final String accessTokenStr = twitterConfig.getProperty("twitter4j.oauth.accessToken");
            final String accessTokenSecret = twitterConfig.getProperty("twitter4j.oauth.accessToken.secret");

            if (Strings.isNullOrEmpty(accessTokenStr) || Strings.isNullOrEmpty(accessTokenSecret)) {
                AccessToken accessToken;
                accessToken = createAccessToken(twitter);
                twitterConfig.setProperty("twitter4j.oauth.accessToken", String.valueOf(accessToken.getToken()));
                twitterConfig.setProperty("twitter4j.oauth.accessToken.secret", String.valueOf(accessToken.getTokenSecret()));
                saveConfig(twitterConfig, configFile);
            } else {
                twitter.setOAuthAccessToken(new AccessToken(accessTokenStr, accessTokenSecret));
            }


            return twitter;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static void saveConfig(Properties twitterConfig, String configFile) {
        try (final OutputStream outputStream = Files.newOutputStream(Paths.get(configFile), StandardOpenOption.CREATE)) {
            twitterConfig.store(outputStream, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties loadConfig(String configFile) throws IOException {
        Properties twitterConfig = new Properties();
        try (final InputStream inStream = Files.newInputStream(Paths.get(configFile), StandardOpenOption.READ)) {
            twitterConfig.load(inStream);
        }
        return twitterConfig;
    }

    private static AccessToken createAccessToken(Twitter twitter) {
        try {
            // get request token.
            // this will throw IllegalStateException if access token is already available
            RequestToken requestToken = twitter.getOAuthRequestToken();
            System.out.println("Got request token.");
            System.out.println("Request token: " + requestToken.getToken());
            System.out.println("Request token secret: " + requestToken.getTokenSecret());

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            AccessToken accessToken = null;
            while (null == accessToken) {
                System.out.println("Open the following URL and grant access to your account:");
                System.out.println(requestToken.getAuthorizationURL());
                System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
                String pin = br.readLine();
                try {
                    if (pin.length() > 0) {
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    } else {
                        accessToken = twitter.getOAuthAccessToken(requestToken);
                    }
                } catch (TwitterException te) {
                    throw new RuntimeException(te);
                }
            }
            System.out.println("Got access token.");
            System.out.println("Access token: " + accessToken.getToken());
            System.out.println("Access token secret: " + accessToken.getTokenSecret());
            return accessToken;
        } catch (IllegalStateException ie) {
            // access token is already available, or consumer key/secret is not set.
            if (!twitter.getAuthorization().isEnabled()) {
                throw new RuntimeException("OAuth consumer key/secret is not set.");
            }
        } catch (TwitterException | IOException e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Cannot get access token");
    }
}
