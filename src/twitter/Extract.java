package twitter;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract consists of methods that extract information from a list of tweets.
 */
public class Extract {

    // '@' must not be preceded by a username character (so emails are ignored),
    // and a username is letters, digits, '_' or '-'.
    private static final Pattern MENTION =
            Pattern.compile("(?<![A-Za-z0-9_-])@([A-Za-z0-9_-]+)");

    /**
     * Get the set of usernames mentioned in a list of tweets.
     *
     * @param tweets list of tweets with distinct ids
     * @return the set of usernames mentioned in the text of the tweets
     *         (case-insensitive, returned in lower case)
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> mentioned = new HashSet<>();
        for (Tweet tweet : tweets) {
            Matcher m = MENTION.matcher(tweet.getText());
            while (m.find()) {
                mentioned.add(m.group(1).toLowerCase(Locale.ROOT));
            }
        }
        return mentioned;
    }
}
