package karaoke.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.ABC;
import karaoke.sound.Instrument;
import karaoke.sound.Music;
import karaoke.sound.MusicLanguage;
import karaoke.sound.Note;
import karaoke.sound.Pitch;

/**
 * Test that ABCParser creates the correct ADT.
 * @category no_didit
 */
public class ABCParserTest {

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testBadAbcFile() throws FileNotFoundException {
        @SuppressWarnings("resource") 
        String abcFile = new Scanner(new File("sample-abc/bad.abc")).useDelimiter("\\Z").next();
        try {
            ABCParser.parse(abcFile);
            fail("should not get here");
        } catch (UnableToParseException e) {}
    }
    
    @Test
    public void testSample1() throws FileNotFoundException, UnableToParseException {
        String abcFile = new Scanner(new File("sample-abc/sample1.abc")).useDelimiter("\\Z").next();
        ABC actual = ABCParser.parse(abcFile);

        Music m1 = 
        Music m2 = new Note(1, new Pitch('C'), Instrument.PIANO, Optional.empty());
        Music music = MusicLanguage.concat(m1, m2);
        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "sample 1");
        fields.put('K', "C");
        ABC expected = new ABC(music, fields);
        assertEquals(expected, actual);
    }
}
