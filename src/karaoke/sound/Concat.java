package karaoke.sound;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Concat represents two pieces of music played one after the other.
 */
public class Concat implements Music {

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
     * Play this concatenation.
     */
    @Override
    public void play(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer) {
        first.play(player, atBeat, lyricConsumer);
        second.play(player, atBeat + first.duration(), lyricConsumer);
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
