package karaoke.songs;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import karaoke.music.Music;
import karaoke.music.Rest;

public class ABCTest {

    /* Testing strategy
     *  input:
     *      optional fields (meter, beatsPerMinute, defaultNote, 
     *          composer) are given, not given
     *      1 musical part, many musical parts
     *      music has lyrics, doesn't have lyrics
     * 
     *  output:
     *      resulting ABC object contains Note, Rest, Concat, Together
     *      subsets of those
     *      resulting ABC contains notes of duration 0, (0, 1), 1, >1
     * 
     * Cover all parts
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    /* helper to check all ABC fields */
    private void helper(Map<Character, Object> expected, ABC actual) {
    	assertEquals(expected.get('T'), actual.getTitle());
    	assertEquals(expected.get('Q'), actual.getTempo());
    	assertEquals(expected.get('K'), actual.getKeySignature());
    	assertEquals(expected.get('M'), actual.getMeter());
    	assertEquals(expected.get('L'), actual.getDefaultNote());
    	assertEquals(expected.get('V'), actual.getVoices());
    	assertEquals(expected.get('C'), actual.getComposer());
    	assertEquals(expected.get('X'), actual.getComposer());
    }
    
    // M, L, and Q are omitted
    @Test
    public void testABC() {
    	final Map<String, Music> parts = new HashMap<>();
    	Music music = new Rest(0);
        parts.put("1", music);
        parts.put("2", music);

        final Map<Character, Object> expected = new HashMap<>();
        expected.put('T', "hi");
        expected.put('K', Key.valueOf("C"));
        expected.put('X', 1);
        ABC actual = new ABC(parts, expected);

        expected.put('M', new Meter(4, 4));
        expected.put('L', new Meter(1, 8));
        expected.put('C', "Unknown");
        expected.put('Q', new Tempo(new Meter(1,8), 100));
        expected.put('V', new HashSet<>(Arrays.asList("1", "2")));

        helper(expected, actual);
    }
    
}
