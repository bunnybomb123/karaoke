package karaoke.sound;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.ABC;

/**
 * Test that Music is played correctly.
 * @category no_didit
 */
public class MusicTest {

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // helper function for testing expectedness of abc files
    private void helperTest(ABC expected, ABC actual) {
        
    }
    
    @Test
    public void testSample1() {
//        Music m1 = note(2, );
//        Music m2 = note();
//        Music music = MusicLanguage.concat(m1, m2);
//        ABC expected = new ABC(music, "sample 1", "C");
//        assertEquals(expected, actual);
    }
}
