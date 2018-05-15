package karaoke.lyrics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    
    private void helperTest(List<Lyric> expected, LyricGenerator lg) {
    	for (Lyric l : expected) {
    		System.out.println(l.getSyllable());
    		assertEquals(l, lg.next().get());
    	}
    }
    
    private void helperLyric() {
    }
    
    @Test
    public void testWaxiesDargle() {
    	LyricGenerator lg = new LyricGenerator("1");
    	lg.loadNewLine(Arrays.asList("I'll go down to Mon-to to-w-n To see un-cle Mc-Ar-dle A-nd".split(" ")));
    	
    	String voice = "1";
    	String line = "I'll go down to Mon-to to-w-n To see un-cle Mc-Ar-dle A-nd".replaceAll(" ", "");
    	List<Lyric> expected = Arrays.asList(
			new Lyric(voice, line, 0, 4), // I'll
			new Lyric(voice, line, 4, 6), // go
			new Lyric(voice, line, 6, 10), // down
			new Lyric(voice, line, 10, 12), // to
			new Lyric(voice, line, 12, 15), // Mon
			new Lyric(voice, line, 16, 18), // to
			new Lyric(voice, line, 18, 20), // to
			new Lyric(voice, line, 21, 22), // w
			new Lyric(voice, line, 23, 24), // n
			new Lyric(voice, line, 24, 26), // To
			new Lyric(voice, line, 26, 29), // see
			new Lyric(voice, line, 29, 31), // un
			new Lyric(voice, line, 32, 35), // cle
			new Lyric(voice, line, 35, 37), // Mc
			new Lyric(voice, line, 38, 40), // Ar
			new Lyric(voice, line, 41, 44), // dle
			new Lyric(voice, line, 44, 45), // A
			new Lyric(voice, line, 46, 48) // nd
		);
    	helperTest(expected, lg);
    	
    	
    }
}
