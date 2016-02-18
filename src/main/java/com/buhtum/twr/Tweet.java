package com.buhtum.twr;

import org.joda.time.DateTime;

public class Tweet {
    private long tweetId;
    private Long inReplyToStatusId;
    private Long inReplyToUserId;
    private DateTime timestamp;
    private String source;
    private String text;
    private Long retweetedStatusId;
    private Long retweetedStatusUserId;
    private DateTime retweetedStatusTimestamp;
    private String expandedUrls;

    public Tweet() {
    }

    public long getTweetId() {
        return tweetId;
    }

    public void setTweetId(long tweetId) {
        this.tweetId = tweetId;
    }

    public Long getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(Long inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public Long getInReplyToUserId() {
        return inReplyToUserId;
    }

    public void setInReplyToUserId(Long inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getRetweetedStatusId() {
        return retweetedStatusId;
    }

    public void setRetweetedStatusId(Long retweetedStatusId) {
        this.retweetedStatusId = retweetedStatusId;
    }

    public Long getRetweetedStatusUserId() {
        return retweetedStatusUserId;
    }

    public void setRetweetedStatusUserId(Long retweetedStatusUserId) {
        this.retweetedStatusUserId = retweetedStatusUserId;
    }

    public DateTime getRetweetedStatusTimestamp() {
        return retweetedStatusTimestamp;
    }

    public void setRetweetedStatusTimestamp(DateTime retweetedStatusTimestamp) {
        this.retweetedStatusTimestamp = retweetedStatusTimestamp;
    }

    public String getExpandedUrls() {
        return expandedUrls;
    }

    public void setExpandedUrls(String expandedUrls) {
        this.expandedUrls = expandedUrls;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "tweetId='" + tweetId + '\'' +
                ", inReplyToStatusId='" + inReplyToStatusId + '\'' +
                ", inReplyToUserId='" + inReplyToUserId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", source='" + source + '\'' +
                ", text='" + text + '\'' +
                ", retweetedStatusId='" + retweetedStatusId + '\'' +
                ", retweetedStatusUserId='" + retweetedStatusUserId + '\'' +
                ", retweetedStatusTimestamp='" + retweetedStatusTimestamp + '\'' +
                ", expandedUrls='" + expandedUrls + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tweet tweet = (Tweet) o;

        return tweetId == tweet.tweetId;

    }

    @Override
    public int hashCode() {
        return (int) (tweetId ^ (tweetId >>> 32));
    }
}
