package karaoke.sound;


public class Lyric {

    /* Abstraction function:
     *  AF(line) = an immutable line of lyrics
     * 
     * Rep invariant:
     *  line is not null
     * 
     * Safety from rep exposure:
     * 
     * Thread safety argument:
     *  This object and its field are all immutable, and there is no 
     *  beneficent mutation
     */
    
    private final String line;
    
    public Lyric(String line) {
        this.line = line;
    }
    
    @Override
    public String getLine() {
        return line;
    }

}
