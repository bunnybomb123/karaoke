package karaoke.sound;

import java.util.Optional;

/**
 * Lyric represents either a lyric (including the syllable being sung, if any) or the absence of a lyric during an instrumental.
 */
public class Lyric {

    public static Lyric instrumental() {
        return new Lyric();
    }
    
    /* Abstraction function:
     *  AF(syllable, line, boldedLine, isInstrumental) =
     *      a lyric if isInstrumental is false, represented by the pair (syllable, line) denoting
     *      a line of lyrics and the syllable being sung, if any; or
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
    
    private final Optional<String> syllable;
    private final String line;
    private final String boldedLine;
    private final boolean isInstrumental;
    
    public Lyric(String line) {
        this.syllable = Optional.empty();
        this.line = line;
        this.boldedLine = line;
        this.isInstrumental = false;
    }
    
    public Lyric(String line, int beginIndex, int endIndex) {
        this.syllable = Optional.of(line.substring(beginIndex, endIndex));
        this.line = line;
        this.boldedLine = line.substring(0, beginIndex) +
                          "*" + syllable + "*" +
                          line.substring(endIndex);
        this.isInstrumental = false;
    }
    
    private Lyric() {
        this.syllable = Optional.empty();
        this.line = "***instrumental***";
        this.boldedLine = this.line;
        this.isInstrumental = true;
    }
    
    public Optional<String> getSyllable() {
        return syllable;
    }
    
    public String getLine() {
        return line;
    }
    
    public String getBoldedLine() {
        return boldedLine;
    }
    
    public boolean isInstrumental() {
        return isInstrumental;
    }

}
