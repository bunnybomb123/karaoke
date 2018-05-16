package karaoke.songs;

import org.junit.Test;

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
    
    
}
