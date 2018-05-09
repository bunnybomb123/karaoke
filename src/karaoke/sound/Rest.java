package karaoke.sound;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Rest represents a pause in a piece of music.
 */
public class Rest implements Music {

    /* Abstraction function:
     *  AF(duration) = a rest with a duration (in beats)
     * 
     * Rep invariant:
     *  duration >= 0
     * 
     * Safety from rep exposure:
     *  all fields are final and immutable
     * 
     * Thread safety argument:
     *  This object is immutable, and there is no beneficent mutation
     * 
     */
    
    private final double duration;

    private void checkRep() {
        assert duration >= 0;
    }

    /**
     * Make a Rest that lasts for duration beats.
     * @param duration duration in beats, must be >= 0
     */
    public Rest(double duration) {
        this.duration = duration;
        checkRep();
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.on(this);
    }

    /**
     * @return duration of this rest
     */
    @Override
    public double duration() {
        return duration;
    }

    /**
     * Play this rest.
     */
    @Override
    public void play(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer) {
        return;
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Rest other = (Rest) obj;
        return duration == other.duration;
    }

    @Override
    public String toString() {
        return "z" + duration;
    }
}
