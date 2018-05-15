package karaoke.songs;


public class Tempo {
    
    private final double beatLength;
    private final int beatsPerMinute;
    private final String symbol;

    private void checkRep() {}
    
    /**
     * Creates a new Tempo top/bottom=beatsPerMinute
     * @param top numerator of the beat length
     * @param bottom denominator of the beat length
     * @param beatsPerMinute beats per minute
     */
    public Tempo(int top, int bottom, int beatsPerMinute) {
        this.beatLength = (double) top / bottom;
        this.beatsPerMinute = beatsPerMinute;
        this.symbol = top + "/" + bottom + "=" + beatsPerMinute;
        checkRep();
    }
    
    /**
     * @return this tempo's beat length in decimal representation
     */
    public double beatLength() {
        return beatLength;
    }
    
    /**
     * @return this tempo's beats per minute
     */
    public int beatsPerMinute() {
        return beatsPerMinute;
    }
    
    /**
     * @return this tempo's symbol
     */
    public String symbol() {
        return symbol;
    }
}