package karaoke.songs;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import karaoke.lyrics.Lyric;
import karaoke.music.Concat;
import karaoke.music.Instrument;
import karaoke.music.Music;
import karaoke.music.Note;
import karaoke.music.Pitch;
import karaoke.music.Rest;
import karaoke.music.Together;

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
    private static void helper(Map<Character, Object> expected, ABC actual) {
        assertEquals(expected.get('T'), actual.getTitle());
        assertEquals(expected.get('Q'), actual.getTempo());
        assertEquals(expected.get('K'), actual.getKeySignature());
        assertEquals(expected.get('M'), actual.getMeter());
        assertEquals(expected.get('L'), actual.getDefaultNote());
        assertEquals(expected.get('V'), actual.getVoices());
        assertEquals(expected.get('C'), actual.getComposer());
        assertEquals(expected.get('X'), actual.getIndexNumber());
    }
    
    // M, L, and Q are omitted, V and C are given
    // Music object contains Rests, and Notes
    // Contains music objects of lengths 0, (0,1)
    @Test
    public void testABCMissingOptionalFields() {
        final Map<String, Music> parts = new HashMap<>();
        Music musicOne = new Rest(0);
        Music musicTwo = new Note(0.5, Pitch.MIDDLE_C,Instrument.PIANO,Optional.of(new Lyric("","")));
        parts.put("1", musicOne);
        parts.put("2", musicTwo);

        final Map<Character, Object> expected = new HashMap<>();
        expected.put('T', "hi");
        expected.put('K', Key.valueOf("C"));
        expected.put('X', 1);
        expected.put('C', "Unknown");
        expected.put('V', new HashSet<>(Arrays.asList("1", "2")));


        ABC actual = new ABC(parts, expected);

        expected.put('M', new Meter(4, 4));
        expected.put('L', new Meter(1, 8));
        expected.put('Q', new Tempo(new Meter(1,8), 100));

        helper(expected, actual);
    }
    
    // V and C are omitted, M, L, and Q are given
    // parts contains 1 element, music contains Rest, Note, Together, and Concat
    // Contains music objects of lengths 1, >1
    @Test
    public void testABCMissingMoreOptionalFields() {
        final Map<String, Music> parts = new HashMap<>();
        Music music = new Rest(1.0);
        music = new Together(new Rest(3.0), music);
        Music note = new Note(1.0, Pitch.MIDDLE_C,Instrument.PIANO,Optional.of(new Lyric("","")));
        music = new Concat(music, note);
        
        parts.put("", music);
  

        final Map<Character, Object> expected = new HashMap<>();
        expected.put('T', "hi");
        expected.put('K', Key.valueOf("C"));
        expected.put('X', 1);
        expected.put('M', new Meter(4, 4));
        expected.put('L', new Meter(1, 8));
        expected.put('Q', new Tempo(new Meter(1,8), 100));
        ABC actual = new ABC(parts, expected);


        expected.put('C', "Unknown");
        expected.put('V', new HashSet<>(Arrays.asList("")));

        helper(expected, actual);
    }
    
    @Test
    public void testABCFailsWithoutIndex() {
        final Map<String, Music> parts = new HashMap<>();
        Music music = new Rest(1.0);
        parts.put("", music);
  

        final Map<Character, Object> expected = new HashMap<>();
        expected.put('T', "hi");
        expected.put('K', Key.valueOf("C"));
//        expected.put('X', 1);
        try {
            // Should fail here
            new ABC(parts, expected);
            assert false;
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testABCFailsWithoutTitle() {
        final Map<String, Music> parts = new HashMap<>();
        Music music = new Rest(1.0);
        parts.put("", music);
  

        final Map<Character, Object> expected = new HashMap<>();
//        expected.put('T', "hi");
        expected.put('K', Key.valueOf("C"));
        expected.put('X', 1);
        try {
            // Should fail here
            new ABC(parts, expected);
            assert false;
        } catch (IllegalArgumentException e) {
            
        }
    }
    
    
    @Test
    public void testABCFailsWithoutKey() {
        final Map<String, Music> parts = new HashMap<>();
        Music music = new Rest(1.0);
        parts.put("", music);
  

        final Map<Character, Object> expected = new HashMap<>();
        expected.put('T', "hi");
//        expected.put('K', Key.valueOf("C"));
        expected.put('X', 1);
        try {
            // Should fail here
            new ABC(parts, expected);
            assert false;
        } catch (Exception e) {
            
        }
    }
}
