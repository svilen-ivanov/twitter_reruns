package com.buhtum.twr;

import com.google.common.collect.ImmutableList;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.ParseDateTime;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.IOException;

public class ArchiveReader {
    private final static Logger log = LoggerFactory.getLogger(ArchiveReader.class);
    private ImmutableList<Tweet> tweets;

    public ArchiveReader() {
    }

    public void load(String archive) {
        final ImmutableList.Builder<Tweet> builder = ImmutableList.builder();
        try (CsvBeanReader reader = new CsvBeanReader(new FileReader(archive), CsvPreference.STANDARD_PREFERENCE)) {
            reader.getHeader(false);

            // the header elements are used to map the values to the bean (names must match)
            final String[] header = new String[]{
                    "tweetId",
                    "inReplyToStatusId",
                    "inReplyToUserId",
                    "timestamp",
                    "source",
                    "text",
                    "retweetedStatusId",
                    "retweetedStatusUserId",
                    "retweetedStatusTimestamp",
                    "expandedUrls"};
            DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");

            final CellProcessor[] cellProcessors = new CellProcessor[]{
                    new ParseLong(),
                    new Optional(new ParseLong()),
                    new Optional(new ParseLong()),
                    new ParseDateTime(dateFormat),
                    new Optional(),
                    new Optional(),
                    new Optional(new ParseLong()),
                    new Optional(new ParseLong()),
                    new Optional(new ParseDateTime(dateFormat)),
                    new Optional()
            };
            Tweet tweet;
            while ((tweet = reader.read(Tweet.class, header, cellProcessors)) != null) {
                if (tweet.getInReplyToStatusId() == null && tweet.getInReplyToUserId() == null) {
                    builder.add(tweet);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tweets = builder.build();
    }

    public ImmutableList<Tweet> getTweets() {
        return tweets;
    }
}
