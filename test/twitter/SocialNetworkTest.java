package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.*;

import org.junit.Test;

/**
 * Testing strategy
 *
 * guessFollowsGraph(tweets):
 *   tweets.size():        0, 1, >1
 *   mentions per tweet:   0, 1, >1
 *   tweets per author:    1, >1 (same author, repeated mentions)
 *   username case:       lower-case, mixed/upper-case
 *   self-mention:         absent, present
 *   The postcondition is underdetermined (a smarter inference may add more
 *   edges), so tests only assert that the required edges are PRESENT
 *   (containsAll / contains); they never assert the exact graph, except for
 *   the empty/no-evidence cases where no edge can be justified.
 *
 * influencers(graph):
 *   graph.size():         0, 1, >1
 *   followers per user:   0, 1, >1
 *   ordering:             strictly decreasing, tied
 *   Ties may be ordered either way by the spec, so tie tests check only the
 *   position of the non-tied users and the membership of the tied group.
 */
public class SocialNetworkTest {

    private static final Instant d1 = Instant.parse("2026-10-23T10:00:00Z");

    private static Tweet tweet(long id, String author, String text) {
        return new Tweet(id, author, text, d1);
    }

    private static Set<String> set(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    // ---------------------------------------------------------------
    // guessFollowsGraph
    // ---------------------------------------------------------------

    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(new ArrayList<Tweet>());
        assertTrue("expected empty graph", graph.isEmpty());
    }

    @Test
    public void testGuessFollowsGraphNoMentions() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "alice", "just a normal tweet"),
                tweet(2, "bob", "another one, email me at bob@example.com"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        for (Set<String> follows : graph.values()) {
            assertTrue("no evidence, so nobody follows anybody", follows.isEmpty());
        }
    }

    @Test
    public void testGuessFollowsGraphSingleMention() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "ernie", "hi @bert"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue("ernie should be in graph", graph.containsKey("ernie"));
        assertTrue("ernie follows bert", graph.get("ernie").contains("bert"));
    }

    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "ernie", "hi @bert and @elmo and @grover"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue(graph.get("ernie").containsAll(set("bert", "elmo", "grover")));
    }

    @Test
    public void testGuessFollowsGraphMultipleTweetsSameUser() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "ernie", "hello @bert"),
                tweet(2, "ernie", "and also @elmo"),
                tweet(3, "ernie", "@bert again"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue("mentions from every tweet must be captured",
                graph.get("ernie").containsAll(set("bert", "elmo")));
    }

    @Test
    public void testGuessFollowsGraphCaseInsensitive() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "ERNIE", "hey @BeRt"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue(graph.containsKey("ernie"));
        assertTrue(graph.get("ernie").contains("bert"));
    }

    @Test
    public void testGuessFollowsGraphNoSelfFollow() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "ernie", "talking to myself @ernie"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        Set<String> follows = graph.get("ernie");
        assertTrue("nobody follows themself", follows == null || !follows.contains("ernie"));
    }

    @Test
    public void testGuessFollowsGraphDoesNotModifyInput() {
        List<Tweet> tweets = new ArrayList<>(Arrays.asList(tweet(1, "ernie", "hi @bert")));
        SocialNetwork.guessFollowsGraph(tweets);
        assertEquals(1, tweets.size());
        assertEquals("hi @bert", tweets.get(0).getText());
    }

    // ---------------------------------------------------------------
    // influencers
    // ---------------------------------------------------------------

    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> graph = new HashMap<>();
        assertTrue(SocialNetwork.influencers(graph).isEmpty());
    }

    @Test
    public void testInfluencersSingleUserWithoutFollowers() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", new HashSet<String>());
        assertTrue("nobody has followers", SocialNetwork.influencers(graph).isEmpty());
    }

    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", set("bob"));
        List<String> result = SocialNetwork.influencers(graph);
        assertEquals(Arrays.asList("bob"), result);
    }

    @Test
    public void testInfluencersMultipleInfluencers() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", set("carol", "bob"));
        graph.put("bob", set("carol"));
        graph.put("dave", set("carol", "bob", "erin"));
        // followers: carol=3, bob=2, erin=1
        List<String> result = SocialNetwork.influencers(graph);
        assertEquals(Arrays.asList("carol", "bob", "erin"), result);
    }

    @Test
    public void testInfluencersTiedInfluence() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("alice", set("bob", "carol", "dave"));
        graph.put("erin", set("bob", "carol"));
        graph.put("frank", set("dave", "bob"));
        // followers: bob=3, carol=2, dave=2 (tie between carol and dave)
        List<String> result = SocialNetwork.influencers(graph);
        assertEquals(3, result.size());
        assertEquals("bob", result.get(0));
        assertEquals(set("carol", "dave"), new HashSet<>(result.subList(1, 3)));
    }

    @Test
    public void testInfluencersEndToEndWithGuessFollowsGraph() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "alice", "@carol @bob"),
                tweet(2, "bob", "@carol"),
                tweet(3, "dave", "@Carol"));
        List<String> result =
                SocialNetwork.influencers(SocialNetwork.guessFollowsGraph(tweets));
        assertEquals("carol", result.get(0));
        assertEquals("bob", result.get(1));
    }
}
