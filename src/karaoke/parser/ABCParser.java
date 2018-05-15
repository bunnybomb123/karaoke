package karaoke.parser;

import karaoke.lyrics.Lyric;
import karaoke.music.*;
import karaoke.songs.ABC;
import karaoke.songs.Meter;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.awt.Label;
import java.beans.Expression;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import edu.mit.eecs.parserlib.Visualizer;
import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;
//import org.apache.commons.io.FileUtils;
public class ABCParser {
    
    private static final char CURRENT_VOICE_CHAR = '.';
    
    /*
     * Abstraction Function:
     * AF() = a function that maps the contents of an abc file into an ABC object. 
     * Rep Invariant:
     * true
     * Safety from Rep Exposure:
     * There are no fields, so nothing can be mutated.
     * Thread Safety: 
     * parse is a static method, which is threadsafe.
     */
    
    /**
     * Main method. Parses and then reprints an example expression.
     * 
     * @param args command line arguments, not used
     * @throws UnableToParseException if example expression can't be parsed
     */
    public static void main(final String[] args) throws UnableToParseException {
        final String input = "foo_bar.png|baz-qux.jpg";
        System.out.println(input);
        final ABC abc = ABCParser.parse(input);
        System.out.println(abc);
    }
    
    // the nonterminals of the grammar
    private static enum ABCGrammar {
        ABC, ABC_HEADER, FIELD_NUMBER, FIELD_TITLE, OTHER_FIELDS, 
        FIELD_COMPOSER, FIELD_DEFAULT_LENGTH, FIELD_METER, FIELD_TEMPO, 
        FIELD_VOICE, FIELD_KEY, KEY, KEYNOTE, KEY_ACCIDENTAL, MODE_MINOR, 
        METER, METER_FRACTION, TEMPO, ABC_BODY, ABC_LINE, ELEMENT, 
        NOTE_ELEMENT, NOTE, PITCH, OCTAVE, NOTE_LENGTH, NOTE_LENGTH_STRICT, 
        ACCIDENTAL, BASENOTE, REST_ELEMENT, TUPLET_ELEMENT, TUPLET_SPEC, 
        CHORD, BARLINE, NTH_REPEAT, LYRIC, LYRICAL_ELEMENT, LYRIC_TEXT, 
        COMMENT, COMMENT_TEXT, END_OF_LINE, TEXT, DIGIT, NEWLINE, 
        SPACE_OR_TAB, MIDDLE_OF_BODY_FIELD,
    }
    

    private static Parser<ABCGrammar> parser = makeParser();
    
    /**
     * Compile the grammar into a parser. 
     * 
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    private static Parser<ABCGrammar> makeParser() {
        try {
            // read the grammar as a file, relative to the project root.
            
            final File grammarFile = new File("src/karaoke/parser/Abc.g");
            return Parser.compile(grammarFile, ABCGrammar.ABC);

        // Parser.compile() throws two checked exceptions.
        // Translate these checked exceptions into unchecked RuntimeExceptions,
        // because these failures indicate internal bugs rather than client errors
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }

    /**
     * Parse the contents of an abc formatted string, and create an ABC object from these contents.
     * 
     * @param string string in abc format which will have its contents parsed
     * @return ABC the object representing the parsed abc string
     * @throws UnableToParseException exception raised if the parser can't parse the given string
     */
    public static ABC parse(final String string) throws UnableToParseException {
 
        // Create a parsetree from the string
        final ParseTree<ABCGrammar> parseTree = parser.parse(string);
        
        // Get the header and body of the parseTree
        ParseTree<ABCGrammar> abcHeaderTree = parseTree.children().get(0);
        ParseTree<ABCGrammar> abcBodyTree = parseTree.children().get(1);
        assert abcHeaderTree.name().equals(ABCGrammar.ABC_HEADER);
        assert abcBodyTree.name().equals(ABCGrammar.ABC_BODY);


        // display the parse tree in various ways, for debugging only
        // System.out.println("parse tree " + parseTree);
        // Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final Map<Character, Object> abcHeaderInfo = new HashMap<Character, Object>();
        getHeaderInfo(abcHeaderTree,abcHeaderInfo);
//        System.out.println("parse tree " + parseTree);
//         Visualizer.showInBrowser(parseTree);
        // Make a new dictionary from the header
        Map<Character, Object> headerCopy = new HashMap<>(abcHeaderInfo);
        headerCopy.put(CURRENT_VOICE_CHAR, "");
        final Map<String, Music> abcMusicParts = new HashMap<>();
        // populates abcMusicParts
        makeAbstractSyntaxTree(abcBodyTree, abcMusicParts, headerCopy, "");
        final ABC abc = new ABC(abcMusicParts, abcHeaderInfo);
        // System.out.println("AST " + abc);
        
        return abc;
    }
    
    private static Music makeMusic(ParseTree<ABCGrammar> parseTree, Map<Character, Object> header, Map<String,Integer> accidentalMap) {
        Visualizer.showInBrowser(parseTree);
        
        switch (parseTree.name()) {
        
        case ABC_LINE: {
            
            Music currentMusic = makeMusic(parseTree.children().get(0), header, accidentalMap);
            int lyricsPresent = 0;
            
            // Instrumental Lyric Generator
//            LyricGenerator lyricGenerator = new LyricGenerator();
            // If the last element is a lyric, then we don't need to go through the last child of the parseTree,
            // And we need to add all the elements of the parseTree to the 
            if (parseTree.children().get(parseTree.children().size()-2).name().equals(ABCGrammar.LYRIC)) {
                lyricsPresent = 1;
//                lyricGenerator = new LyricGenerator(parseTree.children().get(parseTree.children().size()-1));
            }
            // Go through each parseTree element, and for each, make a concat with 
            for (ParseTree<ABCGrammar> t : parseTree.children().subList(1, parseTree.children().size() - lyricsPresent)) {
                // Check to make sure it's not a non-musical element
                if (t.children().get(0).name().equals(ABCGrammar.NOTE_ELEMENT) | t.children().get(0).name().equals(ABCGrammar.REST_ELEMENT) | t.children().get(0).name().equals(ABCGrammar.TUPLET_ELEMENT) ) {
                    Music newMusic = makeMusic(t, header, accidentalMap);
                    currentMusic = new Concat(currentMusic, newMusic);
                }
                
            }
            return currentMusic;
        }
        
        case ELEMENT: {
            return makeMusic(parseTree.children().get(0), header, accidentalMap);
        }
        
        // In this case, we need to just 
        case NOTE_ELEMENT: { //note_element ::= note | chord;
            // 
            return makeMusic(parseTree.children().get(0), header, accidentalMap);
        }
        
        // In this case, we need to make a new note to return to the user
        case NOTE: { // note ::= pitch note_length?;
            // pitch ::= accidental? basenote octave?;
            Pitch newPitch = Pitch.MIDDLE_C;
            String note = "";
            ParseTree<ABCGrammar> pitch = parseTree.children().get(0);
            for (ParseTree<ABCGrammar> t : pitch.children()) {
                System.out.println(t.name());
//                String accidental = "";
                int accidentalNumber = 0;
                boolean accidentalChange = false;
                switch (t.name()) {
                
                case ACCIDENTAL: { // accidental ::= "^" | "^^" | "_" | "__" | "=";
                    String accidental = t.text();
                    accidentalChange = true;
                    switch (accidental) {
                    case "^": {
                        accidentalNumber = 1;
                        break;
                    }
                    case "^^": {
                        accidentalNumber = 2;
                        break;
                    }
                    case "_": {
                        accidentalNumber = -1;
                        break;
                    }
                    case "__": {
                        accidentalNumber = -2;
                        break;
                    }
                    case "=": {
                        accidentalNumber = 0;
                        break;
                    }
                    default:
                        break;
                    }
                    
                }
                case BASENOTE: { // basenote ::= "C" | "D" | "E" | "F" | "G" | "A" | "B" | "c" | "d" | "e" | "f" | "g" | "a" | "b";
                    note = t.text();
                    System.out.println("Hello there");
                    System.out.println(t.text());
                    break;
                }
                case OCTAVE: { // octave ::= "'"+ | ","+
                    note = note + t.text();
                    break;
                }
                default:
                    break;
                }
                if (accidentalChange) {
                    accidentalMap.put(note, accidentalNumber);
                }
//                Lyric nextLyric = LyricGenerator.next();
                System.out.println("Note Below: ");
                System.out.println(note);
                
                newPitch = Pitch.parsePitch(note).transpose(accidentalMap.getOrDefault(note, 0));
            }
            // If the duration of the note is being modified, then go here:
            if (parseTree.children().size() > 1) {
                ParseTree<ABCGrammar> noteLength = parseTree.children().get(1);
                
                
                if (noteLength.text().contains("/")) {
                    int index = noteLength.text().indexOf("/");
                    int numerator = Integer.parseInt(noteLength.text().substring(0, index));
                    
                }
            }
            
            
            
            
            
            Note newNote = new Note(1.0, newPitch, Instrument.ACCORDION, Optional.of(Lyric.INSTRUMENTAL));
            return newNote;
        }
        
        // In this case, we need to go through each note present, add create concats of each of these elements
        case CHORD: { // chord ::= "[" note+ "]";
            Music currentMusic = makeMusic(parseTree.children().get(0), header, accidentalMap);
            // For each of the other children, create a together object with the notes in it
            for (ParseTree<ABCGrammar> t : parseTree.children().subList(1, parseTree.children().size())) {
                Music newNote = makeMusic(t, header, accidentalMap);
                currentMusic = new Together(currentMusic, newNote);
            }
            return currentMusic;
        }
        
        default: {
            System.out.println("Thing that failed: ");
            System.out.println(parseTree.name());
            throw new AssertionError("Shouldn't get here.");
        }
            
        }
        
    }
    
    /**
     * For each voice part present in the abc file's ParseTree, creates an AST representation of the music associated with
     * that voice part. 
     * 
     * @param parseTree parsetree representation of an abc file
     * @param header copy of the header info, which also stores the currently used voice
     * @return map which maps voices, represented by strings, to their Music AST representations
     */
    private static Map<String, Music> makeAbstractSyntaxTree(final ParseTree<ABCGrammar> parseTree, Map<String, Music> currentMusic, Map<Character, Object> header, String voice) {
        
        String currentVoice = voice;
        
        switch (parseTree.name()) {
        
        case ABC_BODY: { // abc_body ::= abc_line+;
            // Contains a bunch of ABC_LINEs
            // What do we need?
            //      List of voices
            //      current voice (maybe need to make an empty voice as well, which is what the default will be 
//            Map<String, Music> returnMusic = makeAbstractSyntaxTree(parseTree.children().get(0), currentMusic,  header, currentVoice);
            for (ParseTree<ABCGrammar> t : parseTree.children()) {
                Map<String, Integer> accidentalMap = new HashMap<>();
                // if t == voice section, then update the current voice
                if(t.name().equals(ABCGrammar.MIDDLE_OF_BODY_FIELD)) {
                    currentVoice = t.children().get(0).text();
                } else if(t.name().equals(ABCGrammar.COMMENT)) {
                    // Do nothing here, it's just a comment
                }
                
                // If it's not an entry in the map, make the abstract syntax tree of the thing and put it in the map
                else if (!currentMusic.containsKey(currentVoice)) {
                    Music startMusic = makeMusic(t, header, accidentalMap);
                    currentMusic.put(currentVoice, startMusic);
                } else {
                    Music leftMusic = currentMusic.get(currentVoice);
                    Music rightMusic = makeMusic(t, header, accidentalMap);
                    Music combinedMusic = new Concat(leftMusic, rightMusic);
                    currentMusic.put(currentVoice, combinedMusic);
                }
                // If it is an entry already, then make a new concat with the right element being the new line's elements, and the 
                // left element being the old music object. Then store the concat in the map for that voice
                // If it's a new voice field, then set the new voice to be the given voice
                
            }
            return currentMusic;
        } 
        
        default: {
            throw new AssertionError("should never get here");
        }

        }
    }
    
    
    /**
     * Extracts the information stored in a ParseTree representation of the header of an abc file
     * 
     * @param parseTree constructed according to the grammar in Abc.g
     * @param currentHeaderInfo map containing information from the fields that have already been extracted from this abc file's header
     */
    private static void getHeaderInfo(final ParseTree<ABCGrammar> parseTree, Map<Character, Object> currentHeaderInfo) {
//        System.out.println(parseTree.children());
        switch (parseTree.name()) {
        
        case ABC_HEADER: //abc_header ::= field_number comment* field_title other_fields* field_key;
            { // Go through all of the children, and call the method on those
                ParseTree<ABCGrammar> fieldNumberParsed = parseTree.children().get(0);
                String currentStringDigits = "";
                for (int i = 0; i < fieldNumberParsed.children().size() - 1; i++) {
                    currentStringDigits = currentStringDigits +  fieldNumberParsed.children().get(i).text();
                }
//                System.out.println(currentStringDigits);
                int fieldNumber = Integer.parseInt(currentStringDigits);
                currentHeaderInfo.put('X', fieldNumber);
                for (ParseTree<ABCGrammar> t : parseTree.children().subList(1, parseTree.children().size())) {
//                    System.out.println("Name of child");
//                    System.out.println(t.name());
                    getHeaderInfo(t, currentHeaderInfo);
                }
                break;
            }    
        
        case FIELD_NUMBER: { // Get the digit, and assign it to the "X" field in the map
            currentHeaderInfo.put('X', Integer.parseInt(parseTree.children().get(0).text()));
            break;
        }
        case FIELD_TITLE: { // Get the title text, and assign it to the "T" field in the map
//            System.out.println("Made it here");
//            System.out.println(parseTree.children());
            currentHeaderInfo.put('T', parseTree.children().get(0).text());
            break;
        }
        case OTHER_FIELDS: { // Go through all the children, and populate the header info with the information obtained from each. 
//            System.out.println("Did it get here?");
//            System.out.println(parseTree.text());
            getHeaderInfo(parseTree.children().get(0),currentHeaderInfo);
            break;
        }
 
        case FIELD_COMPOSER: { // Go through all the children
            currentHeaderInfo.put('C', parseTree.children().get(0).text());
            break;
        }
        case FIELD_DEFAULT_LENGTH: {
            ParseTree<ABCGrammar> noteLengthStrict = parseTree.children().get(0);
            currentHeaderInfo.put('L', parseTree.children().get(0).text());
            break;
        }
        case FIELD_KEY: {
            ParseTree<ABCGrammar> key = parseTree.children().get(0);
            break;
        }
        case FIELD_METER: {
            ParseTree<ABCGrammar> meter = parseTree.children().get(0);
            // If the meter's text contains C|, then make the meter 2/2
            // If the meter's text contains C, then make the mater 4/4
            // Otherwise, it's a meter fraction node, in which case you should extract the numerator and denominator and make a meter from that 
            // currentHeaderInfo.put('M', parseTree.children().get(0).text());
            break;
        }
        case FIELD_TEMPO:  {
            ParseTree<ABCGrammar> meterFraction = parseTree.children().get(0);
            int digit = Integer.parseInt(parseTree.children().get(1).text());
            int numerator = Integer.parseInt(meterFraction.children().get(0).text());
            int denominator = Integer.parseInt(meterFraction.children().get(1).text());
            
            // Tempo will be stored as the length of time needed to complete an entire measure. 
            double measureLength = (double)denominator * (double) digit / (double)numerator;
            
            currentHeaderInfo.put('Q', measureLength);
            break;
        }
        case FIELD_VOICE: {
            // Voice will be stored as a Set of String objects, each representing a different voice
            // If there is no set of voices already in the currentHeading, set it to be a new dictionary.
            if (!currentHeaderInfo.containsKey('V')) {
                currentHeaderInfo.put('V', new HashSet<String>());
            }
            Set<String> currentVoices = ((Set<String>)currentHeaderInfo.get('V'));
            // Add the current voice's text into the set of voices.
            currentVoices.add(parseTree.children().get(0).text());
            
//            ((HashMap<Character, Object>)currentHeaderInfo.get('V')).put('C', parseTree.children().get(0).text());
            break;
        }
        default:
            throw new AssertionError("should never get here");
        }

    }

}