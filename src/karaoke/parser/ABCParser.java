package karaoke.parser;

import static karaoke.music.Music.concat;
import static karaoke.music.Music.empty;
import static karaoke.music.Music.note;
import static karaoke.music.Music.rest;
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
import karaoke.lyrics.LyricGenerator;
import karaoke.music.Instrument;
import karaoke.music.Music;
import karaoke.music.Pitch;
import karaoke.songs.ABC;
import karaoke.songs.AccidentalMap;
import karaoke.songs.Key;
import karaoke.songs.Meter;
import karaoke.songs.Tempo;

/**
 * ABCParser is a static class used to parse ABC songs from .abc files.
 */
public class ABCParser {
    
    // the nonterminals of the grammar
    private static enum ABCGrammar {
        ABC, ABC_HEADER, FIELD_NUMBER, FIELD_TITLE, OTHER_FIELDS, 
        FIELD_COMPOSER, FIELD_DEFAULT_LENGTH, FIELD_METER, FIELD_TEMPO, 
        FIELD_VOICE, FIELD_KEY, KEY, KEYNOTE, KEY_ACCIDENTAL, MODE_MINOR, 
        METER, METER_FRACTION, TEMPO, ABC_BODY, ABC_LINE, ELEMENT, MIDDLE_OF_BODY_FIELD,
        MUSICAL_ELEMENT, NOTE_ELEMENT, NOTE, PITCH, OCTAVE, NOTE_LENGTH, NOTE_LENGTH_STRICT, 
        NUMERATOR, DENOMINATOR, ACCIDENTAL, BASENOTE, REST_ELEMENT, TUPLET_ELEMENT,
        TUPLET_SPEC, CHORD, BARLINE, NTH_REPEAT, LYRIC, LYRICAL_ELEMENT, LYRIC_TEXT, 
        COMMENT, COMMENT_TEXT, END_OF_LINE, TEXT, NUMBER, DIGIT, NEWLINE, SPACE_OR_TAB,
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
     * @return ABC the ABC song representing the parsed abc string
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
        final Map<Character, Object> abcHeader = new HashMap<Character, Object>();
        getHeaderInfo(abcHeaderTree,abcHeader);
        
        final AccidentalMap keySignature = ((Key) abcHeader.get('K')).getAccidentalMap();
        final Map<String, Music> abcBody = parseBody(abcBodyTree, keySignature);
        final ABC abc = new ABC(abcBody, abcHeader);
        
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
                
                final int lastIndex = line.size() - 1;
                final ParseTree<ABCGrammar> last = line.get(lastIndex - 1);
                switch (last.name()) {
                case LYRIC: // lyric ::= "w:" lyrical_element*;
                    elements = line.subList(0, lastIndex - 2);
                    final List<String> lyricalElements = last.children()
                                                             .stream()
                                                             .map(ParseTree::text)
                                                             .collect(Collectors.toList());
                    lyricGenerator.loadLyrics(lyricalElements);
                    break;
                case ELEMENT:
                    elements = line.subList(0, lastIndex);
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
    private static Music makeMusic(ParseTree<ABCGrammar> element, AccidentalMap accidentalMap,
            LyricGenerator lyricGenerator) throws UnableToParseException {
        
        switch (element.name()) {
        
        case MUSICAL_ELEMENT: { // musical_element ::= note_element | rest_element | tuplet_element;
            return makeMusic(element.children().get(0), accidentalMap, lyricGenerator);
        }
        
        case NOTE_ELEMENT: { // note_element ::= note | chord;
            return makeMusic(element.children().get(0), accidentalMap, lyricGenerator);
        }
        
        case REST_ELEMENT: { // rest_element ::= "z" note_length?;
            final List<ParseTree<ABCGrammar>> children = element.children();
            return children.isEmpty() ? rest(1) : rest(toDouble(children.get(0)));
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
            
            final double duration = children.size() == 1 ? 1 : toDouble(children.get(1));
            
            return note(duration, pitch, Instrument.PIANO, lyricGenerator.next());
        }
        
        default: {
            throw new UnableToParseException("musical_element is malformed");
        }
        }
    }
    
    private static int toInt(ParseTree<ABCGrammar> number) {
        return Integer.parseInt(number.text());
    }
    
    private static double toDouble(ParseTree<ABCGrammar> fraction) throws UnableToParseException {
        switch (fraction.name()) {
        case METER_FRACTION: // meter_fraction ::= numerator "/" denominator;
        case NOTE_LENGTH: // note_length ::= numerator ("/" denominator?)? | ("/" denominator?);
        case NOTE_LENGTH_STRICT: // note_length_strict ::= numerator "/" denominator;
            int numerator = 1;
            int denominator = fraction.text().contains("/") ? 2 : 1;
            for (final ParseTree<ABCGrammar> component : fraction.children())
                switch (component.name()) {
                case NUMERATOR: // numerator ::= number;
                    numerator = toInt(component);
                    break;
                case DENOMINATOR: // denominator ::= number;
                    denominator = toInt(component);
                    break;
                default:
                    throw new UnableToParseException("fraction is malformed");
                }
            return (double) numerator / denominator;
        default:
            throw new UnableToParseException("cannot parse to double");
        }
    }
    
    /**
     * Extracts the information stored in a ParseTree representation of the header of an abc file
     * 
     * @param parseTree constructed according to the grammar in Abc.g
     * @param currentHeaderInfo map containing information from the fields that have already been extracted from this abc file's header
     */
    private static void getHeaderInfo(final ParseTree<ABCGrammar> parseTree,
            Map<Character, Object> currentHeaderInfo) throws UnableToParseException {
        
        switch (parseTree.name()) {
        
        case ABC_HEADER: { // abc_header ::= field_number comment* field_title other_fields* field_key;
            for (ParseTree<ABCGrammar> t : parseTree.children())
                getHeaderInfo(t, currentHeaderInfo);
            break;
        }
        
        case FIELD_NUMBER: { // field_number ::= "X:" number end_of_line;
            currentHeaderInfo.put('X', toInt(parseTree.children().get(0)));
            break;
        }
        case FIELD_TITLE: { // field_title ::= "T:" text end_of_line;
            currentHeaderInfo.put('T', parseTree.children().get(0).text().trim());
            break;
        }
        case OTHER_FIELDS: { // other_fields ::= field_composer | field_default_length | field_meter | field_tempo | field_voice | comment;
            getHeaderInfo(parseTree.children().get(0),currentHeaderInfo);
            break;
        }
 
        case FIELD_COMPOSER: { // field_composer ::= "C:" text end_of_line;
            currentHeaderInfo.put('C', parseTree.children().get(0).text().trim());
            break;
        }
        case FIELD_DEFAULT_LENGTH: { // field_default_length ::= "L:" note_length_strict end_of_line;
            // note_length_strict ::= numerator "/" denominator;
            List<ParseTree<ABCGrammar>> fraction = parseTree.children().get(0).children();
            currentHeaderInfo.put('L', new Meter(toInt(fraction.get(0)), toInt(fraction.get(1))));
            break;
        }
        case FIELD_KEY: { // field_key ::= "K:" key end_of_line;
            ParseTree<ABCGrammar> key = parseTree.children().get(0);
            String fullKey = key.text();
            fullKey = fullKey.replace('#', '$');
            fullKey = fullKey.replaceAll("\\s+", "");
            Key newKey = Key.valueOf(fullKey);
            currentHeaderInfo.put('K', newKey);
            break;
        }
        case FIELD_METER: { // field_meter ::= "M:" meter end_of_line;
            // meter ::= "C" | "C|" | meter_fraction;
            ParseTree<ABCGrammar> meter = parseTree.children().get(0);
            if (meter.children().isEmpty())
                    currentHeaderInfo.put('M', new Meter(meter.text()));
            else {
                List<ParseTree<ABCGrammar>> meterFraction = meter.children().get(0).children();
                currentHeaderInfo.put('M', new Meter(toInt(meterFraction.get(0)), toInt(meterFraction.get(1))));
            }
            break;
        }
        case FIELD_TEMPO:  { // field_tempo ::= "Q:" tempo end_of_line;
            List<ParseTree<ABCGrammar>> tempo = parseTree.children().get(0).children();
            // tempo ::= meter_fraction "=" number;
            List<ParseTree<ABCGrammar>> meterFraction = tempo.get(0).children();
            int beatsPerMinute = toInt(tempo.get(1));
            int numerator = toInt(meterFraction.get(0));
            int denominator = toInt(meterFraction.get(1));
            
            currentHeaderInfo.put('Q', new Tempo(new Meter(numerator, denominator), beatsPerMinute));
            break;
        }
        case FIELD_VOICE: { // field_voice ::= "V:" text end_of_line;
            if (!currentHeaderInfo.containsKey('V')) {
                currentHeaderInfo.put('V', new HashSet<String>());
            }
            Set<String> currentVoices = ((Set<String>)currentHeaderInfo.get('V'));
            // Add the current voice's text into the set of voices.
            currentVoices.add(parseTree.children().get(0).text().trim());
            
//            ((HashMap<Character, Object>)currentHeaderInfo.get('V')).put('C', parseTree.children().get(0).text());
            break;
        }
        default:
            throw new UnableToParseException("header is malformed");
        }

    }

}