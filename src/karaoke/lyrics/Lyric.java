package karaoke.lyrics;

import java.util.Optional;

/**
 * Immutable datatype representing either a lyrical line (including the syllable being 
 * sung, if any) or the absence of a lyric during an instrumental.
 */
public class Lyric {
    
    private final String voice;
    private final String prefix;
    private final Optional<String> syllable;
    private final String suffix;
    private final boolean isInstrumental;
    
    /* Abstraction function:
     *  AF(voice, prefix, syllable, suffix, isInstrumental) =
     *      an immutable lyric if isInstrumental is false,
     *          represented by the tuple (prefix, syllable, suffix)
     *          denoting the lyrical line prefix + syllable.get() + suffix where
     *          syllable is the syllable being sung, if any; or
     *      the absence of a lyric if isInstrumental is true.
     *      voice is the voice part of the lyric.
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
     * @param voice voice part
     * @param line lyrical line
     */
    public Lyric(String voice, String line) {
        this.voice = voice;
        this.prefix = line;
        this.syllable = Optional.empty();
        this.suffix = "";
        this.isInstrumental = false;
        checkRep();
    }
    
    /**
     * Creates a Lyric representing a lyrical line including a syllable being sung at line.substring(beginIndex, endIndex).
     * @param voice voice part
     * @param line lyrical line
     * @param beginIndex start index of syllable being sung, inclusive
     * @param endIndex end index of syllable being sung, exclusive
     */
    public Lyric(String voice, String line, int beginIndex, int endIndex) {
        this.voice = voice;
        this.prefix = line.substring(0, beginIndex);
        this.syllable = Optional.of(line.substring(beginIndex, endIndex));
        this.suffix = line.substring(endIndex);
        this.isInstrumental = false;
        checkRep();
    }
    
    /**
     * Creates a Lyric representing the absence of a lyric during an instrumental.
     * @param voice voice part
     */
    public Lyric(String voice) {
        this.voice = voice;
        this.prefix = "***instrumental***";
        this.syllable = Optional.empty();
        this.suffix = "";
        this.isInstrumental = true;
        checkRep();
    }
    
    private void checkRep() {
        assert voice != null;
        assert prefix != null;
        assert syllable != null;
        assert suffix != null;
    }
    
    /**
     * @return this lyric's voice part
     */
    public String getVoice() {
        return voice;
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
    
    @Override
    public boolean equals(Object that) {
    	return that instanceof Lyric && sameValue((Lyric) that);
    }
    
    /* returns true if that is same Lyric as this*/
    private boolean sameValue(Lyric that) {
    	return that.voice.equals(this.voice) && this.prefix.equals(that.prefix)
    			&& this.suffix.equals(that.suffix) && this.syllable.equals(that.syllable)
    			&& this.isInstrumental == that.isInstrumental;
    }
    
    @Override
    public String toString() {
    	return "voice:" + this.voice + "\nprefix + syllable + suffix: " 
    			+ this.prefix + this.syllable + this.suffix + "\nisInstrumental: " 
    			+ isInstrumental;
    }

}
