package karaoke.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lyric represents either a lyrical line (including the syllable being sung, if any) or the absence of a lyric during an instrumental.
 */
public class LyricGenerator {
    
    private final List<String> lyricalElements;
    private final String line;
    private int index = 0;
    private int beginIndex = 0;
    private int hold = 0;
    
    /* Abstraction function:
     *  AF(prefix, syllable, suffix, isInstrumental) =
     *      a lyric if isInstrumental is false,
     *          represented by the tuple (prefix, syllable, suffix)
     *          denoting the lyrical line prefix + syllable.get() + suffix where
     *          syllable is the syllable being sung, if any; or
     *      the absence of a lyric if isInstrumental is true.
     * 
     * Rep invariant:
     *  fields are not null
     * 
     * Safety from rep exposure:
     *  all fields are final and immutable
     *  
     * Thread safety argument:
     *  This object and its field are all immutable, and there is no 
     *  beneficent mutation
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
    }
    
    private void checkRep() {
        assert lyricalElements != null;
        assert line != null;
        String prev = index > 0 ? lyricalElements.get(index - 1) : null;
        String symbol = lyricalElements.get(index);
        assert !isSuffix(symbol, prev);
        String syllable = format(symbol);
        int endIndex = beginIndex + syllable.length();
        assert syllable.equals(line.substring(beginIndex, endIndex));
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
            return Optional.empty();
        }
        
        if (lyricalElements.get(index).equals("|"))
            return Optional.of(Lyric.INSTRUMENTAL);
        
        return nextSyllable();
    }
    
    /**
     * Loads the next measure of lyrics if currently at the end of a measure,
     * such that subsequent calls to next() will return new Lyrics
     * instead of instrumental Lyrics.
     */
    public void loadNextMeasure() {
        if (lyricalElements.get(index).equals("|")) {
            hold = 0;
            nextSyllable();
        }
    }
    
    private Optional<Lyric> nextSyllable() {
        String syllable = format(lyricalElements.get(index));
        String suffix = removeSuffix();
        int endIndex = beginIndex + syllable.length() + suffix.lastIndexOf('_') + 1;
        Lyric lyric = new Lyric(line, beginIndex, endIndex);
        beginIndex += syllable.length() + suffix.length();
        return Optional.of(lyric);
    }
    
    private String removeSuffix() {
        String suffix = "";
        String prev = lyricalElements.get(index);
        String symbol = lyricalElements.get(++index);
        while (isSuffix(symbol, prev)) {
            suffix += symbol;
            if (symbol.equals("_"))
                hold++;
            prev = symbol;
            symbol = lyricalElements.get(++index);
        }
        return suffix;
    }
    
    private static boolean isSuffix(String symbol, String prev) {
        return symbol.equals("-") && (prev == null ||
                                      !prev.trim().isEmpty() && !prev.equals("-"))
                || symbol.trim().isEmpty()
                || symbol.equals("_");
    }
    
    private static String format(String symbol) {
        return symbol.replace("~", " ").replace("\\-", "-");
    }

}
