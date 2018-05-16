package karaoke.music;

import java.util.Objects;
import java.util.function.Consumer;

import karaoke.lyrics.Lyric;
import karaoke.playback.SequencePlayer;

/**
 * Concat represents two pieces of music played one after the other.
 */
public class Concat implements Music {

    /* Abstraction function:
     *  AF(first, second) = 
     *      the concatenation of two pieces of music, first and second,
     *      played one after the other
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
     * Make a Music sequence that plays first followed by second.
     * @param first music to play first
     * @param second music to play second
     */
    public Concat(Music first, Music second) {
        this.first = first;
        this.second = second;
        checkRep();
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
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.on(this);
    }

    /**
     * @return duration of this concatenation
     */
    @Override
    public double duration() {
        return first.duration() + second.duration();
    }

    /**
     * Load this concatenation.
     */
    @Override
    public void load(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer) {
        first.load(player, atBeat, lyricConsumer);
        second.load(player, atBeat + first.duration(), lyricConsumer);
    }
    
    @Override 
    public Music augment(double augmentationFactor) {
        Music newFirst = first.augment(augmentationFactor);
        Music newSecond = second.augment(augmentationFactor);
        return new Concat(newFirst, newSecond);
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
        
        final Concat other = (Concat) obj;
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public String toString() {
        return first + " " + second;
    }
}
