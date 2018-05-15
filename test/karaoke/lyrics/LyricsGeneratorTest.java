package karaoke.lyrics;

import java.util.Arrays;
import java.util.List;

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
    
    @Test
    public void testWaxiesDargle() {
    	List<String> lyricLine = Arrays.asList("I'll go down to Mon-to to-w-n To see "
    			+ "un-cle Mc-Ar-dle A-nd".split(" "));
    	LyricGenerator gnr = new LyricGenerator(lyricLine);
    	
    }
}
