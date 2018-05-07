package karaoke;

/**
 * 
 * @author ericaweng
 *
 */
public class Meter {
    
    private final double meter;
    private final String symbol;

    private void checkRep() {
        assert symbol.equals("C") || symbol.equals("C|") || symbol.contains("/"); // TODO
    }
    
    /**
     * Creates a new Meter top/bottom
     * @param top numerator of the meter
     * @param bottom denominator of the meter
     */
    public Meter(int top, int bottom) {
        this.meter = (double) top / bottom;
        this.symbol = top + "/" + bottom;
        checkRep();
    }
    
    /**
     * Creates a new Meter with symbol C or C|
     * @param symbol symbol of the meter
     */
    public Meter(String symbol) {
        this.meter = 1.0;
        this.symbol = symbol;
        checkRep();
    }
    
    /**
     * @return this meter's decimal representation
     */
    public double meter() {
        return meter;
    }
    
    /**
     * @return this meter's symbol
     */
    public String symbol() {
        return symbol;
    }
}
