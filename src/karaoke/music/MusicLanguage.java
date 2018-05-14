package karaoke.music;

import static karaoke.music.Pitch.OCTAVE;
import static karaoke.music.Music.empty;
import static karaoke.music.Music.rest;
import static karaoke.music.Music.note;
import static karaoke.music.Music.concat;
import static karaoke.music.Music.together;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MusicLanguage defines static methods for constructing and manipulating Music expressions.
 */
public class MusicLanguage {

    /**
     * Prevent instantiation but allow subclassing.
     */
    protected MusicLanguage() {}

    ////////////////////////////////////////////////////
    // Factory methods
    ////////////////////////////////////////////////////

    /**
     * Make Music from a string using a variant of abc notation
     *    (see http://www.walshaw.plus.com/abc/examples/).
     * 
     * <p> The notation consists of whitespace-delimited symbols representing
     * either notes or rests. The vertical bar | may be used as a delimiter
     * for measures; notes() treats it as a space.
     * Grammar:
     * <pre>
     *     notes ::= symbol*
     *     symbol ::= . duration          // for a rest
     *              | pitch duration      // for a note
     *     pitch ::= accidental letter octave*
     *     accidental ::= empty string    // for natural,
     *                  | _               // for flat,
     *                  | ^               // for sharp
     *     letter ::= [A-G]
     *     octave ::= '                   // to raise one octave
     *              | ,                   // to lower one octave
     *     duration ::= empty string      // for 1-beat duration
     *                | /n                // for 1/n-beat duration
     *                | n                 // for n-beat duration
     *                | n/m               // for n/m-beat duration
     * </pre>
     * <p> Examples (assuming 4/4 common time, i.e. 4 beats per measure):
     *     C     quarter note, middle C
     *     A'2   half note, high A
     *     _D/2  eighth note, middle D flat
     * 
     * @param notes string of notes and rests in simplified abc notation given above
     * @param instr instrument to play the notes with
     * @return the music in notes played by instr
     */
    public static Music notes(String notes, Instrument instr) {
        Music music = empty();
        for (String sym : notes.split("[\\s|]+")) {
            if (!sym.isEmpty()) {
                music = concat(music, parseSymbol(sym, instr));
            }
        }
        return music;
    }

    /* Parse a symbol into a Note or a Rest. */
    private static Music parseSymbol(String symbol, Instrument instr) {
        Matcher m = Pattern.compile("(?<pitch>[^/0-9]*)(?<numerator>[0-9]+)?(?<denominator>/[0-9]+)?").matcher(symbol);
        
        if (!m.matches()) throw new IllegalArgumentException("couldn't understand " + symbol);

        String pitchSymbol = m.group("pitch");

        double duration = 1.0;
        if (m.group("numerator") != null) duration *= Integer.valueOf(m.group("numerator"));
        if (m.group("denominator") != null) duration /= Integer.valueOf(m.group("denominator").substring(1));

        if (pitchSymbol.equals(".")) return rest(duration);
        else return note(duration, parsePitch(pitchSymbol), instr, Optional.empty());
    }

    /* Parse a symbol into a Pitch. */
    private static Pitch parsePitch(String symbol) {
        if (symbol.endsWith("'"))
            return parsePitch(symbol.substring(0, symbol.length()-1)).transpose(OCTAVE);
        else if (symbol.endsWith(","))
            return parsePitch(symbol.substring(0, symbol.length()-1)).transpose(-OCTAVE);
        else if (symbol.startsWith("^"))
            return parsePitch(symbol.substring(1)).transpose(1);
        else if (symbol.startsWith("_"))
            return parsePitch(symbol.substring(1)).transpose(-1);
        else if (symbol.length() != 1)
            throw new IllegalArgumentException("can't understand " + symbol);
        else
            return new Pitch(symbol.charAt(0));
    }

    ////////////////////////////////////////////////////
    // Functional objects
    ////////////////////////////////////////////////////
    
    // none

    ////////////////////////////////////////////////////
    // Generic functions
    ////////////////////////////////////////////////////

    // compose : (T->U) x (U->V) -> (T->V)
    public static <T,U,V> Function<T,V> compose(Function<T,U> f, Function<U,V> g) {
        return new Function<T,V>(){
            public V apply(T t) {
                return g.apply(f.apply(t));
            }
        };
    }

    ////////////////////////////////////////////////////
    // Producers
    ////////////////////////////////////////////////////

    /**
     * Transpose all notes upward or downward in pitch.
     * @param m music
     * @param semitonesUp semitones by which to transpose
     * @return m' such that for all notes n in m, the corresponding note n' in m'
     *         has n'.pitch() == n.pitch().transpose(semitonesUp), and m' is
     *         otherwise identical to m
     */
    public static Music transpose(Music m, int semitonesUp) {
        throw new UnsupportedOperationException();
        //return m.accept(new TransposeVisitor(semitonesUp));
    }

    public static Music delay(Music m, double delay) {
        return concat(rest(delay), m);
    }

    public static Music round(Music m, double delay, int times) {
        // to implement Round using Canon, we should call Canon and pass 
        // in the identity function as the argument
        if (times == 0) {
            return empty();
        } else if (times == 1) {
            return m;
        } else {
            return together(m, delay(round(m, delay + delay, times-1), delay));
        }
    }

    public static Music repeat(Music m, int times, Function<Music, Music> f) {
        if (times == 0) {
            return empty();
        }
        if (times == 1) {
            return m;
        }
        return concat(m, repeat(f.apply(m), times - 1, f));
    }
    
}
