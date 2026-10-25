package twitter;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Prints the most-followed users inferred from a small sample of tweets. */
public class Main {

    public static void main(String[] args) {
        Instant t = Instant.parse("2026-10-23T09:00:00Z");
        List<Tweet> tweets = Arrays.asList(
            new Tweet(1, "alice",   "hello @bob and @carol, great lab today", t),
            new Tweet(2, "bob",     "thanks @carol, see you @dave", t),
            new Tweet(3, "dave",    "@carol your JUnit tests are neat", t),
            new Tweet(4, "erin",    "@Carol @bob @alice meeting at 2pm", t),
            new Tweet(5, "frank",   "no mentions in this one", t),
            new Tweet(6, "alice",   "back again with @dave", t),
            new Tweet(7, "carol",   "mail me at carol@example.com", t));

        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        System.out.println("Follows graph: " + new java.util.TreeMap<>(graph));

        List<String> top = SocialNetwork.influencers(graph);
        System.out.println("Top influencers (most followed first):");
        for (int i = 0; i < Math.min(10, top.size()); i++) {
            System.out.println((i + 1) + ". @" + top.get(i));
        }
    }
}
