package twitter;

import java.time.Instant;

/**
 * Immutable datatype representing a tweet.
 */
public class Tweet {

    private final long id;
    private final String author;
    private final String text;
    private final Instant timestamp;

    public Tweet(long id, String author, String text, Instant timestamp) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Tweet(" + id + ", @" + author + ", \"" + text + "\")";
    }
}
