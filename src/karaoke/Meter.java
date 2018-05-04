package karaoke;

/**
 * 
 * @author ericaweng
 *
 */
public class Meter {
    
    private final int top;
    private final int bottom;

    /**
     * creates a new Meter
     * @param top numerator of the meter
     * @param bottom denominator of the meter
     */
    public Meter(int top, int bottom) {
        this.top = top;
        this.bottom = bottom;
    }
    
    /**
     * @return this meter's decimal representation
     */
    public double toDecimal() {
        return (double) top / bottom;
    }
    
    @Override
    public String toString() {
        return top + " / " + bottom;
    }
}
