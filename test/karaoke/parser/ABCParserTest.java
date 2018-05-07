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
import karaoke.sound.Concat;
import karaoke.sound.Instrument;
import karaoke.sound.Music;
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
        @SuppressWarnings("resource") String abcFile = new Scanner(new File("sample-abc/sample1.abc")).useDelimiter("\\Z").next();
        ABC actual = ABCParser.parse(abcFile);

        Music m1 = new Note(2, new Pitch('C').transpose(-Pitch.OCTAVE), Instrument.PIANO, Optional.empty());
        Music m2 = new Note(2, new Pitch('C'), Instrument.PIANO, Optional.empty());
        Music m3 = new Note(1, new Pitch('C').transpose(Pitch.OCTAVE), Instrument.PIANO, Optional.empty());
        Music m4 = new Note(1, new Pitch('C').transpose(2*Pitch.OCTAVE), Instrument.PIANO, Optional.empty());
        Music music = new Concat(m1, new Concat(m2, new Concat(m3, m4)));
        
        final Map<String, Music> parts = new HashMap<>();
        parts.put("", music);
        
        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "sample 1");
        fields.put('K', "C");
        
        ABC expected = new ABC(parts, fields);
        assertEquals(expected, actual);
    }
}
