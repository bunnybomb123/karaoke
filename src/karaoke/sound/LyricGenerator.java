package karaoke.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import karaoke.Jukebox.Signal;

/**
 * Lyric represents either a lyrical line (including the syllable being sung, if any) or the absence of a lyric during an instrumental.
 */
public class LyricGenerator {
    
    private final List<String> lyricalElements;
    private final String line;
    private final List<Integer> lyricLengths;
    private int index = 0;
    private int beginIndex = 0;
    
    /* Abstraction function:
     *  AF(prefix, syllable, suffix, isInstrumental) =
     *      a lyric if isInstrumental is false,
     *          represented by the tuple (prefix, syllable, suffix)
     *          denoting the lyrical line prefix + syllable.get() + suffix where
     *          syllable is the syllable being sung, if any; or
     *      the absence of a lyric if isInstrumental is true.
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
     * @param line lyrical line
     */
    public LyricGenerator(List<String> lyricalElements) {
        this.lyricalElements = new ArrayList<>(lyricalElements);
        Stream<String> lyrics = lyricalElements.stream().map(text -> text.replace("~", " ").replace("\\-", "-"));
        this.line = String.join("", lyrics.collect(Collectors.toList()));
        this.lyricLengths = lyrics.map(String::length).collect(Collectors.toList());
        checkRep();
    }
    
    private void checkRep() {
        assert line != null;
    }
    
    public Optional<Lyric> next() {
        
        
    }
    
    public void loadNextMeasure() {
        
    }
    
    private String formatSyllable(String lyricalElement) {
        return lyricalElement.replace("~", " ").replace("\\-", "-");
    }
    
    /**
    * Represents a signal broadcast from a Jukebox to any JukeboxListeners.
    */
   private class LyricalElement {
       
       public static final Signal SIGNAL_SONG_START = new Signal(Type.SONG_START);
       public static final Signal SIGNAL_SONG_END = new Signal(Type.SONG_END);
       public static final Signal SIGNAL_SONG_CHANGE = new Signal(Type.SONG_CHANGE);
       
       /**
        * @param lyric lyric being broadcast
        * @return Signal representing the lyric
        */
       public static Signal lyric(Lyric lyric) {
           return new Signal(lyric);
       }
       
       /**
        * Types of signals, can represent a song starting, a song ending, a new song being loaded, or a lyric being broadcast.
        */
       public static enum Type {
           SONG_START, SONG_END, SONG_CHANGE, LYRIC
       }
       
       private final Type type;
       private final Optional<Lyric> lyric;
       
       // Abstraction function:
       //  AF(type, lyric) = a signal broadcast from a web server to its listeners,
       //      where the type of signal is type, and
       //      if the signal is a lyric broadcast, lyric.get() is the lyric being broadcast
       // Representation invariant:
       //  fields are not null
       //  type == Type.LYRIC implies lyric.isPresent()
       // Safety from rep exposure:
       //  the board that is passed into the constructor (in ServerMain)
       //      is only intended to be used with this WebServer. Thus, no
       //      inadvertent mutation will happen from outside this class.
       // Thread safety argument:
       //  intended to be a singleton server; thus no need to worry about
       //      other threads accessing or mutating it.
       
       private void checkRep() {
           assert type != null;
           assert lyric != null;
           assert type != Type.LYRIC || lyric.isPresent();
       }
       
       /**
        * Creates a Signal representing a lyric.
        * @param lyric lyric being broadcast
        */
       private Signal(Lyric lyric) {
           this.type = Type.LYRIC;
           this.lyric = Optional.of(lyric);
           checkRep();
       }
       
       /**
        * Creates a Signal representing anything other than a lyric.
        * @param type type of signal, must not be Type.LYRIC
        */
       private Signal(Type type) {
           this.type = type;
           this.lyric = Optional.empty();
           checkRep();
       }
       
       /**
        * @return type of signal
        */
       public Type getType() {
           return type;
       }
       
       /**
        * @return lyric being broadcast, requires that this signal's type is Type.LYRIC
        */
       public Lyric getLyric() {
           return lyric.get();
       }
       
       @Override
       public int hashCode() {
           return Objects.hash(type, lyric);
       }

       @Override
       public boolean equals(Object obj) {
           if (this == obj) return true;
           if (obj == null) return false;
           if (getClass() != obj.getClass()) return false;
           
           final Signal other = (Signal) obj;
           return type == other.type
                   && lyric.equals(other.lyric);
       }

       @Override
       public String toString() {
           return type == Type.LYRIC ? lyric.get().toPlainText() : type.toString();
       }
       
   }

}
