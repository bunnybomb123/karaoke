package karaoke.sound;

import java.util.Optional;

/**
 * Lyric represents either a lyrical line (including the syllable being sung, if any) or the absence of a lyric during an instrumental.
 */
public class Lyric {

    /**
     * @return a Lyric representing the absence of a lyric during an instrumental
     */
    public static Lyric instrumental() {
        return new Lyric();
    }
    
    private final Optional<String> syllable;
    private final String line;
    private final String boldedLine;
    private final boolean isInstrumental;
    
    /* Abstraction function:
     *  AF(syllable, line, boldedLine, isInstrumental) =
     *      a lyric if isInstrumental is false, represented by the pair (syllable, line) denoting
     *      a lyrical line and the syllable being sung, if any; or
     *      the absence of a lyric if isInstrumental is true.
     *      line and boldedLine are text representations of either the lyric or instrumental,
     *      where the syllable being sung is highlighted in boldedLine, if any.
     * 
     * Rep invariant:
     *  when syllable is not present, boldedLine and line are identical
     *  when syllable is present, boldedLine is identical to line except that
     *      the syllable in line is emphasized to be "*" + syllable + "*"
     * 
     * Safety from rep exposure:
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
        this.syllable = Optional.empty();
        this.line = line;
        this.boldedLine = line;
        this.isInstrumental = false;
    }
    
    /**
     * Creates a Lyric representing a lyrical line including a syllable being sung at line.substring(beginIndex, endIndex).
     * @param line lyrical line
     * @param beginIndex start index of syllable being sung, inclusive
     * @param endIndex end index of syllable being sung, exclusive
     */
    public Lyric(String line, int beginIndex, int endIndex) {
        this.syllable = Optional.of(line.substring(beginIndex, endIndex));
        this.line = line;
        this.boldedLine = line.substring(0, beginIndex) +
                          "*" + syllable + "*" +
                          line.substring(endIndex);
        this.isInstrumental = false;
    }
    
    /*
     * Creates a Lyric representing the absence of a lyric during an instrumental, with text representation "***instrumental***"
     */
    private Lyric() {
        this.syllable = Optional.empty();
        this.line = "***instrumental***";
        this.boldedLine = this.line;
        this.isInstrumental = true;
    }
    
    /**
     * @return syllable being sung, if any
     */
    public Optional<String> getSyllable() {
        return syllable;
    }
    
    /**
     * @return lyrical line in plain-text
     */
    public String getLine() {
        return line;
    }
    
    /**
     * @return lyrical line with syllable being sung bolded, if any
     */
    public String getBoldedLine() {
        return boldedLine;
    }
    
    /**
     * @return true if this is an instrumental
     */
    public boolean isInstrumental() {
        return isInstrumental;
    }

}
