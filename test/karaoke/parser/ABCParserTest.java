package karaoke.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import karaoke.songs.ABC;
import karaoke.songs.Key;

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
     *      notes must be transposed (have ' and ,), notes don't have to be transposed
     *      with comments in file, without comments in file
     * 
     *  output:
     *      resulting ABC object contains Note, Rest, Concat, Together
     *      subsets of those
     *      resulting ABC contains notes of duration 0, (0, 1), 1, >1
     * 
     * Cover all parts
     */
    
    private String getContentsFromFile(String filename) throws FileNotFoundException {
        String abcFile = new Scanner(new File(filename)).useDelimiter("\\A").next();
        return abcFile;
    }
    
    
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
    
    // output: Note, Concat, transposed notes
    @Test
    public void testSample1() throws FileNotFoundException, UnableToParseException {
        /*
         * String abcFile = new Scanner(new File("sample-abc/sample1.abc")).useDelimiter("\\Z").next() + "\n";
         */
        String abcFile = getContentsFromFile("sample-abc/sample1.abc");
//        String abcFile = getContentsFromFile("sample-abc/sample1.abc");
        // System.out.println("parse tree " + parseTree);
        // Visualizer.showInBrowser(parseTree);
        System.out.println(abcFile);
//        System.out.println(fileTwo);
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
        fields.put('K', Key.C);
        fields.put('X', 1);
        
        ABC expected = new ABC(parts, fields);
        assertEquals(expected, actual);
    }
    
    // output: Note, Together
    @Test
    public void testSample2() throws FileNotFoundException, UnableToParseException {
        String abcFile = getContentsFromFile("sample-abc/sample2.abc");
        ABC actual = ABCParser.parse(abcFile);

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
        /*
         * String abcFile = new Scanner(new File("sample-abc/sample3.abc")).useDelimiter("\\Z").next();
         */
        String abcFile = getContentsFromFile("sample-abc/sample3.abc");
        ABC actual = ABCParser.parse(abcFile);

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
    
    // input: has lyrics, hyphens only
    @Test
    public void testLyricsHyphen() throws FileNotFoundException, UnableToParseException {
        @SuppressWarnings("resource") String abcFile = new Scanner(new File("sample-abc/lyricsHyphen.abc")).useDelimiter("\\Z").next();
        ABC actual = ABCParser.parse(abcFile);
        
        List<Integer> starts = Arrays.asList(0, 3, 7);
        List<Integer> ends = Arrays.asList(2, 6, 9);
        String line = "ly-ric-al";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        final Map<String, Music> parts = new HashMap<>();
        parts.put("", music);
        
        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "lyricsSimple");
        fields.put('K', Key.valueOf("Bm"));
        fields.put('X', 1);

        ABC expected = new ABC(parts, fields);
        assertEquals(expected, actual);
    }

    // input: has lyrics, with tildes
    @Test
    public void testLyricsTilde() throws FileNotFoundException, UnableToParseException {
        @SuppressWarnings("resource") String abcFile = new Scanner(new File("sample-abc/lyricsTilde.abc")).useDelimiter("\\Z").next();
        ABC actual = ABCParser.parse(abcFile);
        
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
        @SuppressWarnings("resource") String abcFile = new Scanner(new File("sample-abc/lyricsTilde.abc")).useDelimiter("\\Z").next();
        ABC actual = ABCParser.parse(abcFile);
        
        List<Integer> starts = Arrays.asList(0, 3, -1, -1, 9, -1);
        List<Integer> ends =   Arrays.asList(2, 8, -1, -1, 12, -1);
        String line = "ly-ric__ al_";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsUnderscore", music);
        assertEquals(expected, actual);
    }
    
    /* package helper method to get expected ABC file for lyrics testing */
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
    
    // input: has lyrics, with backslash hyphens.
    @Test
    public void testLyricsBackslashHyphen() throws FileNotFoundException, UnableToParseException {
        @SuppressWarnings("resource") String abcFile = new Scanner(new File("sample-abc/lyricsBackslashHyphen.abc")).useDelimiter("\\Z").next();
        ABC actual = ABCParser.parse(abcFile);
        
        List<Integer> starts = Arrays.asList(0, 7);
        List<Integer> ends =   Arrays.asList(6, 16);
        String line = "ly-ric ly-ri-cal";
        List<Music> musics = createNotesForLyricsTesting(line, starts, ends);
        Music music = concatChain(musics);
        		
        ABC expected = getExpectedLyricsTesting("lyricsBackslashHyphen", music);
        assertEquals(expected, actual);
    }
    
    
    // input: lyrics contain all sorts of hyphens and breaks
    // output: Together, Note
    @Test
    public void testLyricsParsing() throws FileNotFoundException, UnableToParseException {
        @SuppressWarnings("resource") String abcFile = new Scanner(new File("sample-abc/testLyrics.abc")).useDelimiter("\\Z").next();
        ABC actual = ABCParser.parse(abcFile);

        Music m1 = new Note(1, new Pitch('C'), Instrument.PIANO, Optional.empty());
        Music m2 = new Note(1, new Pitch('E'), Instrument.PIANO, Optional.empty());
        Music m3 = new Note(1, new Pitch('G'), Instrument.PIANO, Optional.empty());

        Music GnoLyric = new Note(1, new Pitch('G'), Instrument.PIANO, Optional.empty());

        Music G = new Note(1, new Pitch('G'), Instrument.PIANO, Optional.of(new Lyric("Gee")));
        Music E = new Note(1, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("Gee")));
        Music C = new Note(1, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("Cee")));
        
        Music rest = new Rest(1);
        
        Music Cchord = new Note(1, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("chord")));
        Music Echord = new Note(1, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("chord")));

        Music CE = new Together(Cchord, E);
        Music EC = new Together(Echord, C);

        // TODO: Changed this; please return back
        Music part1 = concatChain(Arrays.asList(C, CE, GnoLyric, G, rest ));
        Music part2 = concatChain(Arrays.asList(C, CE, GnoLyric, G, rest));

        final Map<String, Music> parts = new HashMap<>();
        parts.put("1", part1);
        parts.put("2", part2);

        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "voices");
        fields.put('K', "Cm");
        
        ABC expected = new ABC(parts, fields);
        
        assertEquals(new Together(new Together(m1, m2), m3), actual.getMusic());
        assertEquals(expected, actual);
    }
    
    private Music concatChain(List<Music> musics) {
        Music growing = new Rest(0); 
        for (Music music : musics)
            growing = new Concat(growing, music);
        return growing;
    }
}
