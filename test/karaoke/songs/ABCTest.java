package karaoke.songs;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
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
    
    /* helper */
    private void helper(Map<Character, Object> expected, ABC actual) {
    	assertEquals(expected.get('T'), actual.getTitle());
    	assertEquals(expected.get('Q'), actual.getTempo());
    	assertEquals(expected.get('K'), actual.getKeySignature());
    	assertEquals(expected.get('M'), actual.getMeter());
    	assertEquals(expected.get('V'), actual.getVoices());
    	assertEquals(expected.get('C'), actual.getComposer());
    	assertEquals(expected.get('X'), actual.getComposer());
    }
    
    // M, L, and Q are omitted
    @Test
    public void testABC() {
    	final Map<String, Music> parts = new HashMap<>();
    	Music music = new Rest(0);
        parts.put("", music);
        
        final Map<Character, Object> expected = new HashMap<>();
        expected.put('T', "hi");
        expected.put('K', Key.valueOf("C"));
        expected.put('X', 1);
        
//        HashMap<Character, Object> expected = new HashMap<>(fields);
        expected.put('M', new Meter(4, 4));
        expected.put('Q', 100);

        ABC actual = new ABC(parts, expected);
        helper(expected, actual);
    }
    
    
}
