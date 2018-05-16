package karaoke.parser;

import static karaoke.music.Music.empty;
import static karaoke.music.Music.rest;
import static karaoke.music.Music.note;
import static karaoke.music.Music.concat;
import static karaoke.music.Music.together;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;
import edu.mit.eecs.parserlib.Visualizer;
import karaoke.lyrics.Lyric;
import karaoke.lyrics.LyricGenerator;
import karaoke.music.*;
import karaoke.music.Instrument;
import karaoke.music.Music;
import karaoke.music.Note;
import karaoke.music.Pitch;
import karaoke.music.Together;
import karaoke.songs.ABC;
import karaoke.songs.AccidentalMap;
import karaoke.songs.Key;

public class ABCParser {
    
    private static final char CURRENT_VOICE_CHAR = '.';
    private static final int DUPLET_NUM = 2;
    private static final int TRIPLET_NUM = 3;
    private static final int QUADRUPLET_NUM = 4;
    
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
        METER, METER_FRACTION, TEMPO, ABC_BODY, ABC_LINE, ELEMENT, MUSICAL_ELEMENT, 
        NOTE_ELEMENT, NOTE, PITCH, OCTAVE, NOTE_LENGTH, NOTE_LENGTH_STRICT, 
        NUMERATOR, DENOMINATOR, ACCIDENTAL, BASENOTE, REST_ELEMENT, TUPLET_ELEMENT,
        TUPLET_SPEC, CHORD, BARLINE, NTH_REPEAT, LYRIC, LYRICAL_ELEMENT, LYRIC_TEXT, 
        COMMENT, COMMENT_TEXT, END_OF_LINE, TEXT, NUMBER, DIGIT, NEWLINE, 
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
        
        Visualizer.showInBrowser(abcBodyTree);

        // display the parse tree in various ways, for debugging only
        // System.out.println("parse tree " + parseTree);
        // Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final Map<Character, Object> abcHeader = new HashMap<Character, Object>();
        getHeaderInfo(abcHeaderTree,abcHeader);
//        System.out.println("parse tree " + parseTree);
//         Visualizer.showInBrowser(parseTree);
        // Make a new dictionary from the header
        final AccidentalMap keySignature = ((Key)abcHeader.get('K')).getAccidentalMap();
        final Map<String, Music> abcBody = parseBody(abcBodyTree, keySignature);
        final ABC abc = new ABC(abcBody, abcHeader);
        // System.out.println("AST " + abc);
        
        return abc;
    }

    /**
     * Given a nonterminal abc_body and a key signature, return the complete music score.
     * @param abcBody nonterminal abc_body
     * @param accidentalMap key signature
     * @return complete music score as a map from voice part to Music
     */
    private static Map<String, Music> parseBody(final ParseTree<ABCGrammar> abcBody,
            final AccidentalMap keySignature) throws UnableToParseException {
        String voice = "";
        final Map<String, Music> savedParts = new HashMap<>();
        final Map<String, Music> newParts = new HashMap<>();
        // version of music score that is currently being modified for each voice part,
        // either savedParts or newParts
        Map<String, Map<String, Music>> partMap = new HashMap<>();
        final Map<String, LyricGenerator> lyricGenerators = new HashMap<>();
        
        // abc_body ::= abc_line+;
        for (final ParseTree<ABCGrammar> abcLine : abcBody.children()) {
            // abc_line ::= element+ end_of_line (lyric end_of_line)? | middle_of_body_field | comment;
            final List<ParseTree<ABCGrammar>> line = abcLine.children();
            final ParseTree<ABCGrammar> first = line.get(0);
            switch (first.name()) {
            case ELEMENT:
                if (!lyricGenerators.containsKey(voice))
                    lyricGenerators.put(voice, new LyricGenerator(voice));
                
                final LyricGenerator lyricGenerator = lyricGenerators.get(voice);
                
                final List<ParseTree<ABCGrammar>> elements;
                
                final ParseTree<ABCGrammar> last = line.get(line.size() - 2);
                switch (last.name()) {
                case LYRIC: // lyric ::= "w:" lyrical_element*;
                    elements = line.subList(0, line.size() - 3);
                    final List<String> lyricalElements = last.children()
                                                             .stream()
                                                             .map(ParseTree::text)
                                                             .collect(Collectors.toList());
                    lyricGenerator.loadLyrics(lyricalElements);
                    break;
                case ELEMENT:
                    elements = line.subList(0, line.size() - 1);
                    lyricGenerator.loadNoLyrics();
                    break;
                default:
                    throw new UnableToParseException("abc_line is malformed");
                }
                
                for (final ParseTree<ABCGrammar> genericElement : elements) {
                    // element ::= musical_element | barline | nth_repeat | space_or_tab;
                    final ParseTree<ABCGrammar> element = genericElement.children().get(0);
                    switch (element.name()) {
                    case MUSICAL_ELEMENT: // musical_element ::= note_element | rest_element | tuplet_element;
                        final Music music = makeMusic(element, keySignature, lyricGenerator);
                        final Map<String, Music> parts = partMap.getOrDefault(voice, newParts);
                        addMusic(parts, voice, music);
                        break;
                    case BARLINE: // barline ::= "|" | "||" | "[|" | "|]" | ":|" | "|:";
                        keySignature.refresh();
                        lyricGenerator.loadNextMeasure();
                        switch (element.text()) {
                        case "|":
                            break;
                        case "||":
                        case "[|":
                        case "|]":
                        case "|:":
                            partMap.put(voice, newParts);
                            break;
                        case ":|":
                            addMusic(savedParts, voice, newParts.getOrDefault(voice, empty()));
                            newParts.put(voice, empty());
                            break;
                        default:
                            throw new UnableToParseException("barline is malformed");
                        }
                        break;
                    case NTH_REPEAT: // nth_repeat ::= "[1" | "[2";
                        switch (element.text()) {
                        case "[1":
                            addMusic(savedParts, voice, newParts.getOrDefault(voice, empty()));
                            partMap.put(voice, savedParts);
                            break;
                        case "[2":
                            break;
                        default:
                            throw new UnableToParseException("nth_repeat is malformed");
                        }
                        break;
                    case SPACE_OR_TAB: // space_or_tab ::= " " | "\t";
                        break;
                    default:
                        throw new UnableToParseException("element is malformed");
                    }
                }
                break;
            case MIDDLE_OF_BODY_FIELD: // middle_of_body_field ::= field_voice;
                // field_voice ::= "V:" text end_of_line;
                voice = first.children().get(0).children().get(0).text().trim();
                break;
            case COMMENT: // comment ::= space_or_tab* "%" comment_text newline;
                break;
            default:
                throw new UnableToParseException("abc_line is malformed");
            }
        }
        
        for (String part : newParts.keySet())
            addMusic(savedParts, part, newParts.get(part));
        
        return savedParts;
    }
    
    /**
     * Add music to the specified voice part of a score.
     * @param parts music score
     * @param voice voice part
     * @param music music to add
     */
    private static void addMusic(Map<String, Music> parts, String voice, Music music) {
        if (music.duration() == 0)
            return;
        else if (!parts.containsKey(voice))
            parts.put(voice, music);
        else
            parts.put(voice, concat(parts.get(voice), music));
    }
    
    /**
     * Given a nonterminal musical_element, return the Music for this musical element.
     * @param element nonterminal musical_element or a descendant
     * @param accidentalMap key signature, potentially with additional accidentals
     * @param lyricGenerator lyric generator for each note
     * @return Music for this musical element
     */
    private static Music makeMusic(ParseTree<ABCGrammar> element, AccidentalMap accidentalMap, LyricGenerator lyricGenerator) throws UnableToParseException {
        // musical_element ::= note_element | rest_element | tuplet_element;

        switch (element.name()) {
        
        case MUSICAL_ELEMENT: { // musical_element ::= note_element | rest_element | tuplet_element;
            return makeMusic(element.children().get(0), accidentalMap, lyricGenerator);
        }
        
        case NOTE_ELEMENT: { // note_element ::= note | chord;
            return makeMusic(element.children().get(0), accidentalMap, lyricGenerator);
        }
        
        case REST_ELEMENT: { // rest_element ::= "z" note_length?;
            final List<ParseTree<ABCGrammar>> children = element.children();
            return children.isEmpty() ? rest(1) : rest(getDecimalValue(children.get(0)));
        }
        
        case TUPLET_ELEMENT: { // tuplet_element ::= tuplet_spec note_element+;
            final int duplet = 2;
            final int triplet = 3;
            final int quadruplet = 4;
            final double dupletFactor = 3./2;
            final double tripletFactor = 2./3;
            final double quadrupletFactor = 3./4;
            
            final List<ParseTree<ABCGrammar>> tuplet = element.children();
            // tuplet_spec ::= "(" number;
            final int tupletSpec = Integer.parseInt(tuplet.get(0).children().get(0).text());
            final double augmentationFactor;
            switch (tupletSpec) {
            case duplet:
                augmentationFactor = dupletFactor;
                break;
            case triplet:
                augmentationFactor = tripletFactor;
                break;
            case quadruplet:
                augmentationFactor = quadrupletFactor;
                break;
            default:
                throw new UnableToParseException("tuplet_element is malformed");
            }
            
            Music music = makeMusic(tuplet.get(1), accidentalMap, lyricGenerator).augment(augmentationFactor);
            for (final ParseTree<ABCGrammar> noteElement : tuplet.subList(2, tuplet.size()))
                music = concat(music, makeMusic(noteElement, accidentalMap, lyricGenerator).augment(augmentationFactor));
            return music;
        }
        
        case CHORD: { // chord ::= "[" note+ "]";
            final List<ParseTree<ABCGrammar>> chord = element.children();
            lyricGenerator.setChordSize(chord.size());
            Music music = makeMusic(chord.get(0), accidentalMap, lyricGenerator);
            // For each of the other children, create a together object with the notes in it
            for (final ParseTree<ABCGrammar> note : chord.subList(1, chord.size()))
                music = together(music, makeMusic(note, accidentalMap, lyricGenerator));
            return music;
        }
        
        case NOTE: { // note ::= pitch note_length?;
            final int sharp = 1;
            final int doubleSharp = 2;
            final int flat = -1;
            final int doubleFlat = -2;
            final int natural = 0;
            
            final List<ParseTree<ABCGrammar>> children = element.children();
            Optional<Integer> accidental = Optional.empty();
            String note = "";
            // pitch ::= accidental? basenote octave?;
            for (ParseTree<ABCGrammar> component : children.get(0).children()) {
                switch (component.name()) {
                case ACCIDENTAL: { // accidental ::= "^" | "^^" | "_" | "__" | "=";
                    switch (component.text()) {
                    case "^": {
                        accidental = Optional.of(sharp);
                        break;
                    }
                    case "^^": {
                        accidental = Optional.of(doubleSharp);
                        break;
                    }
                    case "_": {
                        accidental = Optional.of(flat);
                        break;
                    }
                    case "__": {
                        accidental = Optional.of(doubleFlat);
                        break;
                    }
                    case "=": {
                        accidental = Optional.of(natural);
                        break;
                    }
                    default: {
                        throw new UnableToParseException("accidental is malformed");
                    }
                    }
                }
                case BASENOTE: { // basenote ::= "C" | "D" | "E" | "F" | "G" | "A" | "B" | "c" | "d" | "e" | "f" | "g" | "a" | "b";
                    note = component.text();
                    break;
                }
                case OCTAVE: { // octave ::= "'"+ | ","+;
                    note += component.text();
                    break;
                }
                default: {
                    throw new UnableToParseException("pitch is malformed");
                }
                }
            }
            
            Pitch pitch = Pitch.parsePitch(note);
            if (accidental.isPresent()) {
                accidentalMap.put(pitch, accidental.get());
            }
            pitch = pitch.transpose(accidentalMap.get(pitch));
            
            final double duration = children.size() == 1 ? 1 : getDecimalValue(children.get(1));
            
            return note(duration, pitch, Instrument.PIANO, lyricGenerator.next());
        }
        
        default: {
            throw new UnableToParseException("musical_element is malformed");
        }
        }
    }
    
    private static double getDecimalValue(ParseTree<ABCGrammar> fraction) throws UnableToParseException {
        switch (fraction.name()) {
        case METER_FRACTION:
        case NOTE_LENGTH:
        case NOTE_LENGTH_STRICT:
            double result = 1;
            for (final ParseTree<ABCGrammar> component : fraction.children())
                result *= getDecimalValue(component);
            return result;
        case NUMERATOR:
            return Integer.parseInt(fraction.text());
        case DENOMINATOR:
            return 1.0 / Integer.parseInt(fraction.text());
        default:
            throw new UnableToParseException("note_length or note_length_strict is malformed");
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
            String fullKey = key.text();
            fullKey = fullKey.replace('#', '$');
            fullKey = fullKey.replaceAll("\\s+","");
            Key newKey = Key.valueOf(fullKey);
            currentHeaderInfo.put('K', newKey);
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