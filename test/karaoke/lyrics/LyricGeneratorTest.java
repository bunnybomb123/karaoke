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
    private void helperTest(List<Optional<Lyric>> expected, LyricGenerator lg) {
    	for (Optional<Lyric> l : expected) {
    		Optional<Lyric> actual = lg.next();
    		assertEquals(l, actual);
    	}
    }
    
    @Test
    public void testWaxiesDargle() {
    	LyricGenerator lg = new LyricGenerator("1");
    	lg.loadNewLine(Arrays.asList("I'll", " ","go"," ","down"," ","to"," ","Mon",
    			"-","to"," ", "to","-","w","-","n"," ","To"," ","see"," ","un"," ","cle",
    			" ","Mc","-","Ar","-","dle","*","A","-","-","nd"));
    	
    	String voice = "1";
    	String line = "I'll go down to Mon-to to-w-n To see un cle Mc-Ar-dle*A--nd";
    	List<Optional<Lyric>> expected = Arrays.asList(
			createOptionalLyric(line, 0, 4), // I'll
			createOptionalLyric(line, 5, 7), // go
			createOptionalLyric(line, 8, 12), // down
			createOptionalLyric(line, 13, 15), // to
			createOptionalLyric(line, 16, 19), // Mon
			createOptionalLyric(line, 20, 22), // to
			createOptionalLyric(line, 23, 25), // to
			createOptionalLyric(line, 26, 27), // w
			createOptionalLyric(line, 28, 29), // n
			createOptionalLyric(line, 30, 32), // To
			createOptionalLyric(line, 33, 36), // see
			createOptionalLyric(line, 37, 39), // un
			createOptionalLyric(line, 40, 43), // cle
			createOptionalLyric(line, 44, 46), // Mc
			createOptionalLyric(line, 47, 49), // Ar
			createOptionalLyric(line, 50, 53), // dle
			createOptionalLyric(line, 53, 54), // *
			createOptionalLyric(line, 54, 55), // A
			createOptionalLyric(line, 56, 57), // -
			createOptionalLyric(line, 57, 59) // nd
		);
    	helperTest(expected, lg);
    }
    
    private Optional<Lyric> createOptionalLyric(String line, int start, int end){
    	return Optional.of(new Lyric("1", line, start, end));
    }
    
    @Test
    public void testDoubleUnderscore() {
    	LyricGenerator lg = new LyricGenerator("1");
    	lg.loadNewLine(Arrays.asList("hi", "_","_"," ","go"));
    	
    	String line = "hi__ go";
    	List<Optional<Lyric>> expected = Arrays.asList(
			createOptionalLyric(line, 0, 4), // hi__
			Optional.empty(), Optional.empty(),
			createOptionalLyric(line, 5, 7) // go
		);
    	helperTest(expected, lg);
    }
    
    @Test
    public void testTilde() {
    	LyricGenerator lg = new LyricGenerator("1");
    	lg.loadNewLine(Arrays.asList("hi", "~"," ","go"));
    	
    	String line = "hi  go";
    	List<Optional<Lyric>> expected = Arrays.asList(
			createOptionalLyric(line, 0, 2), // hi 
			Optional.empty(),
			createOptionalLyric(line, 4, 6) // go
		);
    	helperTest(expected, lg);
    }
    
    @Test
    public void testBackslashHyphen() {
    	LyricGenerator lg = new LyricGenerator("1");
    	lg.loadNewLine(Arrays.asList("hi\\-"," ","go"," ", "\\-"," ","\\-"));
    	
    	String line = "hi- go - -";
    	List<Optional<Lyric>> expected = Arrays.asList(
			createOptionalLyric(line, 0, 3), // hi-
			createOptionalLyric(line, 4, 6), // go
			createOptionalLyric(line, 7, 8), // -
			createOptionalLyric(line, 9, 10) // -
		);
    	helperTest(expected, lg);
    }
}
