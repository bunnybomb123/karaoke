package karaoke.songs;

import java.util.Objects;

/**
 * A Meter is a fraction, often used to determine the number of beats in a measure.
 */
public class Meter {
    
    /* Abstraction function
     *  AF(top, bottom, value, symbol) = the meter fraction top/bottom, with
     *      decimal value value and string representation symbol
     * 
     * Rep invariant
     *  top, bottom, value > 0
     *  symbol is common time ("C"), cut time ("C|"), or represents top/bottom
     *
     * Safety from rep exposure
     *  args passed into the constructor are primitive int, thus immutable
     * 
     * Thread safety
     *  This object and its fields are immutable with no beneficent mutation
     */

    private final int top;
    private final int bottom;
    private final double value;
    private final String symbol;

    private void checkRep() {
        assert symbol.equals("C") || symbol.equals("C|") || symbol.equals(top + "/" + bottom);
        assert top > 0;
        assert bottom > 0;
        assert value > 0;
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
    
    @Override
    public String toString() {
        return symbol;
    }
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Meter && sameValue((Meter) that);
    }

    /* returns true if that has same tempo value as this */
    private boolean sameValue(Meter that) {
        return this.top == that.top && this.bottom == that.bottom && this.value == that.value
                && this.symbol.equals(that.symbol);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(top, bottom, value, symbol);
    }

}
