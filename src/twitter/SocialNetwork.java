package twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * SocialNetwork provides methods that operate on a social network.
 *
 * A social network is represented by a Map<String, Set<String>> where
 * map[A] is the set of people that person A follows on Twitter, and all
 * people are represented by their lower-case Twitter usernames.
 */
public class SocialNetwork {

    /**
     * Guess who might follow whom, from evidence found in tweets.
     *
     * @param tweets a list of tweets providing the evidence, not modified
     * @return a social network (as defined above) in which Ernie follows Bert
     *         if and only if there is evidence for it in the given list of
     *         tweets. Here, Ernie follows Bert if Ernie @-mentions Bert in a
     *         tweet. A user never follows themself. The graph is a
     *         lower-case-keyed map; the answer is not unique in general, as a
     *         more sophisticated inference could add further edges.
     */
    public static Map<String, Set<String>> guessFollowsGraph(List<Tweet> tweets) {
        Map<String, Set<String>> graph = new HashMap<>();
        for (Tweet tweet : tweets) {
            String author = tweet.getAuthor().toLowerCase(Locale.ROOT);
            Set<String> mentions =
                    Extract.getMentionedUsers(Collections.singletonList(tweet));
            mentions.remove(author); // nobody follows themself
            if (mentions.isEmpty()) {
                continue;
            }
            graph.computeIfAbsent(author, k -> new HashSet<>()).addAll(mentions);
        }
        return graph;
    }

    /**
     * Find the people in a social network who have the greatest influence,
     * in the sense that they have the most followers.
     *
     * @param followsGraph a social network (as defined above)
     * @return a list of all distinct Twitter usernames that have at least one
     *         follower in followsGraph, in descending order of follower count.
     *         Ties are broken alphabetically so the output is deterministic.
     */
    public static List<String> influencers(Map<String, Set<String>> followsGraph) {
        final Map<String, Integer> followers = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : followsGraph.entrySet()) {
            String follower = entry.getKey().toLowerCase(Locale.ROOT);
            for (String followed : entry.getValue()) {
                followed = followed.toLowerCase(Locale.ROOT);
                if (followed.equals(follower)) {
                    continue;
                }
                followers.merge(followed, 1, Integer::sum);
            }
        }
        List<String> result = new ArrayList<>(followers.keySet());
        result.sort((a, b) -> {
            int cmp = Integer.compare(followers.get(b), followers.get(a));
            return cmp != 0 ? cmp : a.compareTo(b);
        });
        return result;
    }
}
