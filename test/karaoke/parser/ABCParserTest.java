package karaoke.parser;

import static karaoke.music.Music.concat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.lyrics.Lyric;
import karaoke.music.Concat;
import karaoke.music.Instrument;
import karaoke.music.Music;
import karaoke.music.Note;
import karaoke.music.Pitch;
import karaoke.music.Rest;
import karaoke.music.Together;
import karaoke.playback.SequencePlayer;
import karaoke.songs.ABC;
import karaoke.songs.Key;
import karaoke.songs.Meter;
import karaoke.songs.Tempo;

/**
 * Test that ABCParser creates the correct ADT.
 */
public class ABCParserTest {

    /* Testing strategy
     *  input:
     *      optional fields (meter, beatsPerMinute, defaultNote, 
     *          composer) are given, not given
     *      1 musical part, many musical parts
     *      music has lyrics, doesn't have lyrics
     *      lyrics contain all sorts of hyphens and breaks, don't
     *      notes must be transposed Octaves up or down (have ' and ,), don't have to
     *      with comments in file, without comments in file
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
    
    /* helper to get the actual ABC object that is parsed by parser */
    private ABC helperGetActual(String filename) throws UnableToParseException, FileNotFoundException {
    	@SuppressWarnings("resource") 
    	String abcFile = new Scanner(new File("sample-abc/"+filename+".abc")).useDelimiter("\\A").next();
        ABC actual = ABCParser.parse(abcFile);
        return actual;
    }
    
    @Test
    public void testFurElise() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("fur_elise");
//        SequencePlayer.load(actual, null).playUntilFinished();
    }
    
    // output: Note, Concat, transposed notes
    @Test
    public void testSample1() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("sample1");
        SequencePlayer.load(actual, null).playUntilFinished();
        Music m1 = new Note(2, new Pitch('C').transpose(-Pitch.OCTAVE), Instrument.PIANO, Optional.of(new Lyric("")));
        Music m2 = new Note(2, new Pitch('C'), Instrument.PIANO, Optional.empty());
        Music m3 = new Note(1, new Pitch('C').transpose(2*Pitch.OCTAVE), Instrument.PIANO, Optional.empty());
        Music m4 = new Note(1, new Pitch('C').transpose(3*Pitch.OCTAVE), Instrument.PIANO, Optional.empty());
        Music music = concat(concat(concat(m1, m2), m3), m4);
        
        final Map<String, Music> parts = new HashMap<>();
        parts.put("", music);
        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "sample 1");
        fields.put('K', Key.C);
        fields.put('X', 1);
        
        ABC expected = new ABC(parts, fields);
        assertEquals(expected, actual);
    }
    
    // output: Note, Together
    @Test
    public void testSample2() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("sample2");
//        SequencePlayer.load(actual, null).playUntilFinished();
        Music m1 = new Note(1, new Pitch('C'), Instrument.PIANO, Optional.empty());
        Music m2 = new Note(1, new Pitch('E'), Instrument.PIANO, Optional.empty());
        Music music = new Together(m1, m2);
        final Map<String, Music> parts = new HashMap<>();
        parts.put("", music);
        
        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "Chord");
        fields.put('K', Key.valueOf("C"));
        fields.put('X', 8);
        
        ABC expected = new ABC(parts, fields);
        assertEquals(expected, actual);
    }
    
    // input: many voices
    // output: Together, Note
    @Test
    public void testSample3() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("sample3");

        Music m1 = new Note(1, new Pitch('C'), Instrument.PIANO, Optional.empty());
        Music m2 = new Note(1, new Pitch('E'), Instrument.PIANO, Optional.empty());
        Music m3 = new Note(1, new Pitch('G'), Instrument.PIANO, Optional.empty());

        final Map<String, Music> parts = new HashMap<>();
        parts.put("1", m1);
        parts.put("2", m2);
        parts.put("3", m3);

        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "voices");
        fields.put('K', Key.valueOf("Cm"));
        fields.put('X', 0);
        
        ABC expected = new ABC(parts, fields);
        
        assertEquals(new Together(new Together(m1, m2), m3), actual.getMusic());
        assertEquals(expected, actual);
    }
    
    /* helper method to create a OptionalLyric object */
    private Optional<Lyric> createOptionalLyric(String line, int start, int end){
    	return Optional.of(new Lyric("", line, start, end));
    }
    
    /* helper method to create notes objects to test lyrics*/
    private List<Music> createNotesForLyricsTesting(String line, List<Integer> starts, List<Integer> ends) {
    	List<Music> listMusics = new ArrayList<>();
    	
    	Iterator<Integer> startsItr = starts.iterator();
    	Iterator<Integer> endsItr = ends.iterator();
    	while (startsItr.hasNext()) {
    		int start = startsItr.next();
    		int end = endsItr.next();
    		if (start == -1)
        		listMusics.add(new Note(1, new Pitch('C'), Instrument.PIANO, Optional.empty()));
    		else
    			listMusics.add(new Note(1, new Pitch('C'), Instrument.PIANO, createOptionalLyric(line, start, end)));
    	}
    	return listMusics;
    }
    
    /* helper method to get expected ABC file for lyrics testing */
    private ABC getExpectedLyricsTesting(String title, Music music) {
    	final Map<String, Music> parts = new HashMap<>();
        parts.put("", music);
        
        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', title);
        fields.put('K', Key.valueOf("C"));
        fields.put('X', 1);

        ABC expected = new ABC(parts, fields);
        return expected;
    }
    
    // input: has lyrics, hyphens only
    @Test
    public void testLyricsHyphen() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("lyricsHyphen");
        
        List<Integer> starts = Arrays.asList(0, 3, 7);
        List<Integer> ends = Arrays.asList(2, 6, 9);
        String line = "ly-ric-al";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsSimple", music);
        assertEquals(expected, actual);
    }

    // input: has lyrics, with tildes
    @Test
    public void testLyricsTilde() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("lyricsTilde");

        List<Integer> starts = Arrays.asList(0, 8);
        List<Integer> ends = Arrays.asList(7, 10);
        String line = "ly ric  al";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsTilde", music);
        assertEquals(expected, actual);
    }
    
    // input: has lyrics, with underscores, Key is minor, with accidental
    @Test
    public void testLyricsUnderscore() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("lyricsUnderscore");

        List<Integer> starts = Arrays.asList(0, 3, -1, -1, 9, -1);
        List<Integer> ends =   Arrays.asList(2, 8, -1, -1, 12, -1);
        String line = "ly-ric__ al_";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsUnderscore", music);
        assertEquals(expected, actual);
    }
        
    // input: has lyrics, with backslash hyphens.
    @Test
    public void testLyricsBackslashHyphen() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("lyricsBackslashHyphen");

        List<Integer> starts = Arrays.asList(0, 7);
        List<Integer> ends =   Arrays.asList(6, 16);
        String line = "ly-ric ly-ri-cal";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsBackslashHyphen", music);
        assertEquals(expected, actual);
    }
    
    // input: has lyrics, with asterisks.
    @Test
    public void testLyricsAsterisk() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("lyricsAsterisk");

        List<Integer> starts = Arrays.asList(0, 3, 5, 8, 11, 13, 15);
        List<Integer> ends =   Arrays.asList(2, 4, 7, 10, 12, 14, 17);
        String line = "ly * al ly * al";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsAsterisk", music);
        assertEquals(expected, actual);
    }
    
    // input: has lyrics, with a barline that is not ignored.
    @Test
    public void testLyricsBarline() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("lyricsBarline");

        List<Integer> starts = Arrays.asList(0, 3, 7, 10);
        List<Integer> ends =   Arrays.asList(2, 6, 9, 14);
        String line = "ly-ric-al bear";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsBarline", music);
        assertEquals(expected, actual);
    }
    
    // input: has lyrics, with a barline that is ignored.
    @Test
    public void testLyricsBarlineIgnored() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("lyricsBarlineIgnored");

        List<Integer> starts = Arrays.asList(0, 3, 7, 10, 13);
        List<Integer> ends =   Arrays.asList(2, 6, 9, 12, 17);
        String line = "ly-ric-al-ly bear";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsBarlineIgnored", music);
        assertEquals(expected, actual);
    }
    
    // Makes sure no errors are thrown when a relatively large file is parsed
    // Idea is that if no error is thrown here, we can handle most cases
    @Test
    public void testParsingBigFile() throws FileNotFoundException, UnableToParseException {
        ABC actual = helperGetActual("piece2");
        System.out.println(actual);
    }
   
    /* creates a note with empty lyric */
    private Music createNote(double d, Pitch pitch) {
    	return new Note(d, pitch, Instrument.PIANO, Optional.empty());
    }
    
    // input: lyrics contain all sorts of hyphens and breaks
    @Test
    public void testOctaveUp() throws FileNotFoundException, UnableToParseException {
    	final String title = "testOctaveUp";
    	Music n1 = createNote(1./4, new Pitch('C').transpose(Pitch.OCTAVE));
    	Music n2 = createNote(1, new Pitch('C').transpose(Pitch.OCTAVE).transpose(Pitch.OCTAVE));
    	Music n3 = createNote(1, new Pitch('C').transpose(Pitch.OCTAVE).transpose(Pitch.OCTAVE).transpose(Pitch.OCTAVE));
    	Music n4 = createNote(1./2, new Pitch('C').transpose(Pitch.OCTAVE).transpose(Pitch.OCTAVE));
    	Music n5 = createNote(1./4, new Pitch('C').transpose(Pitch.OCTAVE).transpose(Pitch.OCTAVE));
    	
    	List<Music> musics = Arrays.asList(n1, n2, n3, n4, new Together(n1, n5));
    	Music music = concatChain(musics);
        ABC actual = helperGetActual(title);
        ABC expected = getExpectedLyricsTesting(title, music);
        assertEquals(expected, actual);
    }
    
    // input: lyrics contain all sorts of hyphens and breaks
    @Test
    public void testOctaveDown() throws FileNotFoundException, UnableToParseException {
    	final String title = "testOctaveDown";
    	Music n1 = createNote(1, new Pitch('C'));
    	Music n2 = createNote(1, new Pitch('C').transpose(-Pitch.OCTAVE));
    	Music n3 = createNote(1./2, new Pitch('C').transpose(-Pitch.OCTAVE));
    	Music n4 = createNote(1, new Pitch('C').transpose(-Pitch.OCTAVE).transpose(-Pitch.OCTAVE));
    	Music n5 = createNote(1, new Pitch('C').transpose(-Pitch.OCTAVE).transpose(-Pitch.OCTAVE).transpose(-Pitch.OCTAVE));
    	
    	List<Music> musics = Arrays.asList(n1, n2, n3, n4, new Together(n1, n5));
    	Music music = concatChain(musics);
    	
        ABC actual = helperGetActual(title);
        ABC expected = getExpectedLyricsTesting(title, music);
        assertEquals(expected, actual);
    }
    
    // input: tests that all headers can be correctly parsed
    @Test
    public void testHeaders() throws FileNotFoundException, UnableToParseException {
    	final String title = "testHeaders";
    	Music n1 = createNote(1, new Pitch('C'));
    	Music n2 = createNote(1, new Pitch('E').transpose(-Pitch.OCTAVE));
    	
    	Map<Character, Object> fields = new HashMap<>();
    	Map<String, Music> parts = new HashMap<>();
    	parts.put("1", n1);
    	parts.put("2", n2);
    	
    	fields.put('T', "testHeaders");
        fields.put('K', Key.valueOf("Cbm"));
        fields.put('X', 1);
        fields.put('M', new Meter("C"));
        fields.put('L', new Meter(1, 2));
        fields.put('C', "Chris Chang");
        fields.put('Q', new Tempo(new Meter(1,2), 150));
        fields.put('V', new HashSet<>(Arrays.asList("1", "2")));
    	
        ABC actual = helperGetActual(title);
        ABC expected = new ABC(parts, fields);

        assertEquals(expected, actual);
    }    
    
    /* concats a bunch of musics into one music */
    private Music concatChain(List<Music> musics) {
        Iterator<Music> musicsItr = musics.iterator();
        Music growing = musicsItr.next();
        while (musicsItr.hasNext())
            growing = new Concat(growing, musicsItr.next());
        return growing;
    }
}
