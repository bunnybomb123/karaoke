package karaoke.lyrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * LyricGenerator generates Lyrics from a provided lyrical line.
 */
public class LyricGenerator {
    
    private final List<String> lyricalElements;
    private final String line;
    private int index;
    private int beginIndex = 0;
    private int hold = 0;
    
    /* Abstraction function:
     *  AF(lyricalElements, line, index, beginIndex, hold) =
     *      a lyric generator that generates Lyrics from the lyrical line
     *          specified by lyricalElements and with formatted representation line,
     *      	where the syllable being sung by the next lyric (or the barline if at the 
     *      	end of a measure) is represented by lyricalElements.get(index) and found 
     *      	in the formatted lyrical line as line.substring(beginIndex, beginIndex + 
     *      	syllable.length()), unless hold > 0 in which case the previous syllable 
     *      	should be held for hold more notes
     * 
     * Rep invariant:
     *  fields are not null
     *  if index < lyricalElements.size():
     *      symbol = lyricalElements.get(index) is either a syllable or a barline, and
     *      syllable = format(symbol) is equal to line.substring(beginIndex, beginIndex + syllable.length())
     *  hold >= 0
     * 
     * Safety from rep exposure:
     *  defensive copying in constructor for lyricalElements
     *  no other fields are passed in or returned by any methods
     */
    
    /**
     * Creates a LyricGenerator for the lyrical line specified by lyricalElements.
     * @param lyricalElements lyrical line, represented by a list of lyrical elements
     *          according to Abc grammar
     */
    public LyricGenerator(List<String> lyricalElements) {
        this.lyricalElements = new ArrayList<>(lyricalElements);
        StringBuffer formattedLine = new StringBuffer();
        lyricalElements.stream().map(LyricGenerator::format).forEach(formattedLine::append);
        this.line = formattedLine.toString();
        this.index = 0;
        removeSuffix();
        checkRep();
    }
    
    /*
     * Check rep invariant.
     */
    private void checkRep() {
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
    }
    
    /**
     * @return new Lyric with next syllable being sung, or
     *          an instrumental Lyric if at the end of a measure, or
     *          no Lyric if previous Lyric is being held
     */
    public Optional<Lyric> next() {
        if (hold > 0) {
            hold--;
            checkRep();
            return Optional.empty();
        }
        
        if (index >= lyricalElements.size() || lyricalElements.get(index).equals("|"))
            return Optional.of(Lyric.INSTRUMENTAL);
        
        return nextSyllable();
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
    private Optional<Lyric> nextSyllable() {
        String syllable = format(lyricalElements.get(index++));
        String suffix = removeSuffix();
        int endIndex = beginIndex + syllable.length() + suffix.lastIndexOf('_') + 1;
        Lyric lyric = new Lyric(line, beginIndex, endIndex);
        beginIndex += syllable.length() + suffix.length();
        checkRep();
        return Optional.of(lyric);
    }
    
    /*
     * Returns the suffix starting at lyricalElements.get(index).
     * A suffix is a string of characters that could trail behind a syllable or 
     * barline before the next syllable or barline.
     * Updates index to point to next syllable or barline.
     */
    private String removeSuffix() {
        StringBuffer suffix = new StringBuffer();
        String prev = index > 0 ? lyricalElements.get(index - 1) : null;
        String symbol = lyricalElements.get(index);
        while (isSuffix(symbol, prev)) {
            suffix.append(symbol);
            if (symbol.equals("_"))
                hold++;
            prev = symbol;
            symbol = lyricalElements.get(++index);
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
