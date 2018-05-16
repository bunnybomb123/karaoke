package karaoke.songs;

/**
 * immutable object representing a Tempo
 */
public class Tempo {
    
	/* Abstraction function
	 * 	AF(beatLength, beatsPerMinute, symbol)
	 * 		= an immutable Tempo representing beatsPerMinute beats of length beatLength 
	 * 		in a minute, and the String representation is symbol.
	 * 
	 * Rep invariant
	 * 	beatLength, beatsPerMinute > 0
	 *  symbol is not empty string, and contains a "/" and a "="
	 * 
	 * Safety from rep exposure
	 * 	args passed into the constructor, beatLength and beatsPerMinute, are immutable
	 * 
	 * Thread safety
	 * 	This object is immutable with no beneficent mutation 
	 */
	
    private final double beatLength;
    private final int beatsPerMinute;
    private final String symbol;

    /* checks the rep invariant is maintained */
    private void checkRep() {
    	assert beatLength > 0;
    	assert beatsPerMinute > 0;
    	assert !symbol.isEmpty();
    	assert symbol.indexOf("/") >= 0;
    	assert symbol.indexOf("=") >= 0;
    }
    
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
    
    @Override
    public String toString() {
    	return symbol;
    }
    
    @Override
    public boolean equals(Object that) {
    	return that instanceof Tempo && sameValue((Tempo) that);
    }

    /* returns true if that has same tempo value as this */
	private boolean sameValue(Tempo that) {
		return this.beatLength == that.beatLength && this.beatsPerMinute == that.beatsPerMinute
				&& this.symbol.equals(that.symbol);
	}
	
	@Override
	public int hashCode() {
		return (int) (this.beatLength + this.beatsPerMinute + this.symbol.hashCode());
	}
}