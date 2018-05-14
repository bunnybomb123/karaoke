package karaoke.lyrics;

import java.util.Optional;

/**
 * Lyric represents either a lyrical line (including the syllable being sung, if any) or the absence of a lyric during an instrumental.
 */
public class Lyric {

    /**
     * Lyric constant representing the absence of a lyric during an instrumental.
     */
    public static final Lyric INSTRUMENTAL = new Lyric();
    
    private final String prefix;
    private final Optional<String> syllable;
    private final String suffix;
    private final boolean isInstrumental;
    
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
     * Creates a Lyric representing a lyrical line with no syllable being sung.
     * @param line lyrical line
     */
    public Lyric(String line) {
        this.prefix = line;
        this.syllable = Optional.empty();
        this.suffix = "";
        this.isInstrumental = false;
        checkRep();
    }
    
    /**
     * Creates a Lyric representing a lyrical line including a syllable being sung at line.substring(beginIndex, endIndex).
     * @param line lyrical line
     * @param beginIndex start index of syllable being sung, inclusive
     * @param endIndex end index of syllable being sung, exclusive
     */
    public Lyric(String line, int beginIndex, int endIndex) {
        this.prefix = line.substring(0, beginIndex);
        this.syllable = Optional.of(line.substring(beginIndex, endIndex));
        this.suffix = line.substring(endIndex);
        this.isInstrumental = false;
        checkRep();
    }
    
    /*
     * Creates a Lyric representing the absence of a lyric during an instrumental, with text representation "***instrumental***"
     */
    private Lyric() {
        this.prefix = "***instrumental***";
        this.syllable = Optional.empty();
        this.suffix = "";
        this.isInstrumental = true;
        checkRep();
    }
    
    private void checkRep() {
        assert prefix != null;
        assert syllable != null;
        assert suffix != null;
    }
    
    /**
     * @return syllable being sung, if any
     */
    public Optional<String> getSyllable() {
        return syllable;
    }
    
    /**
     * @return unbolded lyrical line
     */
    public String getLine() {
        return syllable.isPresent() ?
                prefix + syllable.get() + suffix :
                prefix + suffix;
    }
    
    /**
     * @return lyrical line formatted for plain-text
     */
    public String toPlainText() {
        return syllable.isPresent() ?
                prefix + "*" + syllable.get() + "*" + suffix :
                prefix + suffix;
    }
    
    /**
     * @return lyrical line formatted for html-text
     */
    public String toHtmlText() {
        return syllable.isPresent() ?
                prefix + "<b>" + syllable.get() + "</b>" + suffix + "<br>" :
                prefix + suffix + "<br>";
    }
    
    /**
     * @return true if this is an instrumental
     */
    public boolean isInstrumental() {
        return isInstrumental;
    }

}
