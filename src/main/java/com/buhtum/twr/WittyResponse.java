package com.buhtum.twr;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;

import java.security.SecureRandom;
import java.util.List;

public class WittyResponse extends UserStreamAdapter {
    private final static Logger log = LoggerFactory.getLogger(WittyResponse.class);

    private final List<String> responses = ImmutableList.of(
            "аз съм глупава програма, не разбирам какво казваш, но ЕТО ГИФ --> %s",
            "аз отговарям само с гифове %s",
            "говориш си с радиоточката, чуек %s",
            "мръдни, мръдни ще се изцапаш %s"
    );
    private final Twitter twitter;
    private final String screenName;
    private final Giphy giphy;
    private final SecureRandom random = new SecureRandom();

    public WittyResponse(Twitter twitter, String myHandle) {
        this.twitter = twitter;
        this.screenName = myHandle;
        this.giphy = new Giphy(Giphy.DEMO_API_KEY);
    }

    @Override
    public void onStatus(Status status) {
        checkForMentions(status);
        checkForGifs(status);
    }

    private void checkForGifs(Status status) {
        for (URLEntity urlEntity : status.getURLEntities()) {
            final String text = urlEntity.getText();
            if (text == null) continue;
            if (text.contains(".gif")) {
                log.debug("Found gif mention: {} in status {}", urlEntity, status);
                break;
            }
        }
    }

    private void checkForMentions(Status status) {
        if (status.getRetweetedStatus() == null) {
            for (UserMentionEntity mention : status.getUserMentionEntities()) {
                if (mention.getScreenName().equalsIgnoreCase(screenName)) {
                    log.debug("Mentioned in: {}", status);

                    final String randomGif = this.giphy.findRandomGif();
                    if (randomGif != null) {
                        String response = responses.get(random.nextInt(responses.size()));
                        StatusUpdate reply = new StatusUpdate("@" + status.getUser().getScreenName() + " " + String.format(response, randomGif));
                        reply.setInReplyToStatusId(status.getId());
                        log.debug("Replying with: {}", reply);

                        try {
                            twitter.updateStatus(reply);
                        } catch (TwitterException e) {
                            log.error("Failed to reply to: " + status);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onException(Exception ex) {
        log.error("Oops", ex);
    }


}
