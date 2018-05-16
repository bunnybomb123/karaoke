package karaoke.music;

import java.util.Objects;
import java.util.function.Consumer;

import karaoke.lyrics.Lyric;
import karaoke.playback.SequencePlayer;

/**
 * Concat represents two pieces of music played simultaneously.
 */
public class Together implements Music {

    /* Abstraction function:
     *  AF(first, second) = 
     *      the layering of two pieces of music, first and second,
     *      played simultaneously
     * 
     * Rep invariant:
     *  all fields are not null
     * 
     * Safety from rep exposure:
     *  all fields are private, final, and immutable
     *  
     * Thread safety argument:
     *  This object and its field are all immutable, and there is no 
     *  beneficent mutation
     */
    
    private final Music first;
    private final Music second;
    
    private void checkRep() {
        assert first != null;
        assert second != null;
    }

    /**
     * Make a Music sequence that plays first at the same time as second.
     * @param first first music to play, defines this Music's duration
     * @param second second music to play
     */
    public Together(Music first, Music second) {
        this.first = first;
        this.second = second;
        checkRep();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.on(this);
    }

    /**
     * @return first piece in this concatenation
     */
    public Music first() {
        return first;
    }

    /**
     * @return second piece in this concatenation
     */
    public Music second() {
        return second;
    }
    
    /**
     * @return duration of this chord, defined to be the duration of the first note
     */
    @Override
    public double duration() {
        return first.duration();
    }

    /**
     * Load this chord.
     */
    @Override
    public void load(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer) {
        first.load(player, atBeat, lyricConsumer);
        second.load(player, atBeat, lyricConsumer);
    }
    
    @Override 
    public Music augment(double augmentationFactor) {
        Music newFirst = first.augment(augmentationFactor);
        Music newSecond = second.augment(augmentationFactor);
        return new Together(newFirst,newSecond);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Together other = (Together) obj;
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public String toString() {
        return "["+first.toString() + second.toString()+"]";
    }

}
