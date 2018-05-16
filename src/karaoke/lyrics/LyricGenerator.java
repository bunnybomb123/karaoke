package karaoke.lyrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * LyricGenerator generates Lyrics from provided lyrical lines, one at a time.
 * LyricGenerator is guaranteed not to repeat any lyrics; any repeats are instead replaced by Optional.empty().
 */
public class LyricGenerator {
    
    private final String voice;
    private List<String> lyricalElements;
    private String line;
    private int index;
    private int beginIndex;
    private int hold;
    private int chordSize = 1;
    private Lyric previous = null;
    
    /* Abstraction function:
     *  AF(voice, lyricalElements, line, index, beginIndex, hold, previous) =
     *      a lyric generator that generates Lyrics for the given voice part from the lyrical line
     *          specified by lyricalElements and with formatted representation line,
     *          where the syllable being sung by the next lyric (or the barline if at the 
     *          end of a measure) is represented by lyricalElements.get(index) and found 
     *          in the formatted lyrical line as line.substring(beginIndex, beginIndex + 
     *          syllable.length()), unless hold > 0 in which case the previous syllable 
     *          should be held for hold more notes.
     *      chordSize is the size of the next chord in the music sequence, default 1.
     *      previous is the last lyric returned by this generator.
     * 
     * Rep invariant:
     *  fields are not null except previous
     *  if index < lyricalElements.size():
     *      symbol = lyricalElements.get(index) is either a syllable or a barline, and
     *      syllable = format(symbol) is equal to line.substring(beginIndex, beginIndex + syllable.length())
     *  hold >= 0
     *  chordSize >= 1
     * 
     * Safety from rep exposure:
     *  defensive copying in instantiation of lyricalElements
     *  no other fields are passed in or returned by any methods
     */
    
    /**
     * Creates an empty LyricGenerator, which can only generate instrumental lyrics until loadNewLine() is called.
     * @param voice voice part of lyrics passed into this LyricGenerator
     */
    public LyricGenerator(String voice) {
        this.voice = voice;
        loadNoLyrics();
        checkRep();
    }
    
    /*
     * Check rep invariant.
     */
    private void checkRep() {
        assert voice != null;
        assert lyricalElements != null;
        assert line != null;
        if (index < lyricalElements.size()) {
            String prev = index > 0 ? lyricalElements.get(index - 1) : null;
            String symbol = lyricalElements.get(index);
            assert !isSuffix(symbol, prev);
            String syllable = format(symbol);
            int endIndex = beginIndex + syllable.length();
            assert syllable.equals(line.substring(beginIndex, endIndex));
        }
        assert hold >= 0;
        assert chordSize >= 1;
    }
    
    /**
     * Loads a new empty lyrical line,
     * such that subsequent calls to next() will return instrumental Lyrics.
     */
    public void loadNoLyrics() {
        lyricalElements = new ArrayList<>();
        line = "";
        index = 0;
        beginIndex = 0;
        hold = 0;
        checkRep();
    }
    
    /**
     * Loads the lyrical line specified by lyricalLine,
     * such that subsequent calls to next() will return Lyrics from this lyrical line.
     * @param lyricalLine lyrical line, represented by a list of lyrical elements
     *          according to Abc grammar
     */
    public void loadLyrics(List<String> lyricalLine) {
        lyricalElements = new ArrayList<>(lyricalLine);
        
        StringBuffer formattedLine = new StringBuffer();
        lyricalLine.stream().map(LyricGenerator::format).forEach(formattedLine::append);
        line = formattedLine.toString();
        
        index = 0;
        removeSuffix();
        
        beginIndex = 0;
        hold = 0;
        previous = null;
        checkRep();
    }
    
    /**
     * @return new Lyric with next syllable being sung, or
     *          a Lyric with no syllable being sung if at the end of a measure, or
     *          an instrumental Lyric if current line contains no lyrics, or
     *          no Lyric if previous Lyric is being held
     */
    public Optional<Lyric> next() {
        if (hold > 0) {
            hold--;
            checkRep();
            return Optional.empty();
        }
        
        if (chordSize > 1) {
            hold += chordSize - 1;
            chordSize = 1;
        }
        
        final Lyric lyric;
        
        if (line.isEmpty())
            lyric = new Lyric(voice);
        else if (index >= lyricalElements.size() || lyricalElements.get(index).equals("|"))
            lyric = new Lyric(voice, line);
        else
            lyric = nextSyllable();
        
        if (lyric.equals(previous))
            return Optional.empty();
        else {
            previous = lyric;
            return Optional.of(lyric);
        }
    }
    
    /**
     * Notify the LyricGenerator that the next lyric should be generated
     * for a chord of size size.
     * @param size size of chord
     */
    public void setChordSize(int size) {
        chordSize = size;
    }
    
    /**
     * Loads the next measure of lyrics if currently at the end of a measure,
     * such that subsequent calls to next() will return new Lyrics
     * instead of instrumental Lyrics.
     */
    public void loadNextMeasure() {
        if (index < lyricalElements.size() && lyricalElements.get(index).equals("|")) {
            hold = 0;
            nextSyllable();
            checkRep();
        }
    }
    
    /*
     * Returns a new Lyric representing the extended syllable starting at lyricalElements.get(index) being sung.
     * An extended syllable is a syllable extended to include all "_" characters following it.
     * Updates index to point to next syllable or barline.
     */
    private Lyric nextSyllable() {
        String syllable = format(lyricalElements.get(index++));
        String suffix = removeSuffix();
        int endIndex = beginIndex + syllable.length() + suffix.lastIndexOf('_') + 1;
        Lyric lyric = new Lyric(voice, line, beginIndex, endIndex);
        beginIndex += syllable.length() + suffix.length();
        checkRep();
        return lyric;
    }
    
    /*
     * Returns the suffix starting at lyricalElements.get(index).
     * A suffix is a string of characters that could trail behind a syllable or 
     * barline before the next syllable or barline.
     * Updates index to point to next syllable or barline.
     */
    private String removeSuffix() {
        if (index >= lyricalElements.size())
            return "";
        
        StringBuffer suffix = new StringBuffer();
        String prev = index > 0 ? lyricalElements.get(index - 1) : null;
        String symbol = lyricalElements.get(index);
        while (isSuffix(symbol, prev)) {
            suffix.append(symbol);
            if (symbol.equals("_"))
                hold++;
            prev = symbol;
            index++;
            if (index >= lyricalElements.size())
                break;
            symbol = lyricalElements.get(index);
        }
        return suffix.toString();
    }
    
    /*
     * Given a symbol and the symbol preceding it, return whether the symbol is part of a suffix.
     */
    private static boolean isSuffix(String symbol, String prev) {
        return symbol.equals("-") && (prev == null ||
                                      !prev.trim().isEmpty() && !prev.equals("-"))
                || symbol.trim().isEmpty()
                || symbol.equals("_");
    }
    
    /**
     * @param symbol lyrical element to parse
     * @return formatted representation of symbol
     */
    private static String format(String symbol) {
        return symbol.replace("~", " ").replace("\\-", "-");
    }

}
