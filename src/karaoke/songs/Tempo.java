package karaoke.songs;


public class Tempo {
    
    private final double beatLength;
    private final int beatsPerMinute;
    private final String symbol;

    private void checkRep() {}
    
    /**
     * Creates a new Tempo top/bottom=beatsPerMinute
     * @param beatLength beat length as a meter fraction
     * @param beatsPerMinute beats per minute
     */
    public Tempo(Meter beatLength, int beatsPerMinute) {
        this.beatLength = beatLength.value();
        this.beatsPerMinute = beatsPerMinute;
        this.symbol = beatLength.top() + "/" + beatLength.bottom() + "=" + beatsPerMinute;
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