package karaoke.songs;

/**
 * 
 * @author ericaweng
 *
 */
public class Meter {
    
    private final int top;
    private final int bottom;
    private final double value;
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
        this.top = top;
        this.bottom = bottom;
        this.value = (double) top / bottom;
        this.symbol = top + "/" + bottom;
        checkRep();
    }
    
    /**
     * Creates a new Meter with symbol C or C|
     * @param symbol symbol of the meter
     */
    public Meter(String symbol) {
        final int commonTime = 4;
        final int cutTime = 2;
        if (symbol.equals("C")) {
            this.top = commonTime;
            this.bottom = commonTime;
        }
        else if (symbol.equals("C|")) {
            this.top = cutTime;
            this.bottom = cutTime;
        }
        else
            throw new RuntimeException("incorrect meter symbol");
        
        this.value = 1.0;
        this.symbol = symbol;
        checkRep();
    }
    
    /**
     * @return this meter's numerator
     */
    public int top() {
        return top;
    }
    
    /**
     * @return this meter's denominator
     */
    public int bottom() {
        return bottom;
    }
    
    /**
     * @return this meter's decimal representation
     */
    public double value() {
        return value;
    }
    
    /**
     * @return this meter's symbol
     */
    public String symbol() {
        return symbol;
    }
}
