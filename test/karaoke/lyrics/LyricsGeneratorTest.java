package karaoke.lyrics;

import org.junit.Test;

/**
 * tests Lyrics 
 * @author ericaweng
 */
public class LyricsGeneratorTest {
	
    /* Testing strategy
     *  input:
     *      optional fields (meter, beatsPerMinute, defaultNote, 
     *          composer) are given, not given
     *      1 musical part, many musical parts
     *      music has lyrics, doesn't have lyrics
     *      lyrics contain all sorts of hyphens and breaks, don't
     *      notes must be transposed, not transposed
     * 
     *  output:
     *      resulting ABC object contains Note, Rest, Concat, Together
     *      subsets of those
     * 
     * Cover all parts
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
}
