package karaoke.lyrics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

/**
 * tests Lyrics 
 * @author ericaweng
 */
public class LyricGeneratorTest {
	
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
    
    /* helper to  */
    private void helperTest(List<Lyric> expected, LyricGenerator lg) {
    	for (Lyric l : expected) {
//    		System.out.println(l.getSyllable());
    		Optional<Lyric> actual = lg.next();
    		System.out.println("GET: "+actual.get());
    		if (actual.isPresent()){
    			System.out.println("EXPECTED: " + l + "\nACTUAL: "+actual.get());
    			assertEquals(l, actual.get());

    		}
    	}
    }
    
    @Test
    public void testWaxiesDargle() {
    	LyricGenerator lg = new LyricGenerator("1");
    	lg.loadNewLine(Arrays.asList("I'll", " ","go"," ","down"," ","to"," ","Mon",
    			"-","to"," ", "to","-","w","_","n"," ","To","\\-","see"," ","un","~","cle",
    			" ","Mc","-","Ar","-","dle","*","A","-","-","nd"));
    	
    	String voice = "1";
    	String line = "I'll go down to Mon-to to-w_n To-see un cle Mc-Ar-dle*A--nd";
    	List<Lyric> expected = Arrays.asList(
			new Lyric(voice, line, 0, 4), // I'll
			new Lyric(voice, line, 5, 7), // go
			new Lyric(voice, line, 8, 12), // down
			new Lyric(voice, line, 13, 15), // to
			new Lyric(voice, line, 16, 19), // Mon
			new Lyric(voice, line, 20, 22), // to
			new Lyric(voice, line, 23, 25), // to
			new Lyric(voice, line, 26, 28), // w_
			new Lyric(voice, line, 27, 28), // n
			new Lyric(voice, line, 30, 32), // To
			new Lyric(voice, line, 33, 36), // see
			new Lyric(voice, line, 37, 39), // un
			new Lyric(voice, line, 40, 43), // cle
			new Lyric(voice, line, 44, 46), // Mc
			new Lyric(voice, line, 47, 49), // Ar
			new Lyric(voice, line, 50, 53), // dle
			new Lyric(voice, line, 53, 54), // *
			new Lyric(voice, line, 54, 55), // A
			new Lyric(voice, line, 56, 57), // -
			new Lyric(voice, line, 57, 59) // nd
		);
    	helperTest(expected, lg);
    }
    
    @Test
    public void testHyphensDargle() {
    	
    }
}
