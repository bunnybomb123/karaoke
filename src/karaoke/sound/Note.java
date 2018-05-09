package karaoke.sound;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Note represents a note played by an instrument.
 */
public class Note implements Music {
    
    /* Abstraction function:
     *  AF(duration, pitch, instrument, lyric) = 
     *      a note with a duration (in beats), a pitch, an instrument it's played on,
     *      and optionally a lyric sung on the note
     * 
     * Rep invariant:
     *  duration >= 0
     *  all fields are not null
     * 
     * Safety from rep exposure:
     *  all fields are private, final, and immutable
     *  
     * Thread safety argument:
     *  This object and its field are all immutable, and there is no 
     *  beneficent mutation
     */

    private final double duration;
    private final Pitch pitch;
    private final Instrument instrument;
    private final Optional<Lyric> lyric;

    private void checkRep() {
        assert duration >= 0;
        assert pitch != null;
        assert instrument != null;
        assert lyric != null;
    }

    /**
     * Make a Note played by instrument for duration beats.
     * @param duration duration in beats, must be >= 0
     * @param pitch pitch to play
     * @param instrument instrument to use
     * @param lyric optional lyric to play
     */
    public Note(double duration, Pitch pitch, Instrument instrument, Optional<Lyric> lyric) {
        this.duration = duration;
        this.pitch = pitch;
        this.instrument = instrument;
        this.lyric = lyric;
        checkRep();
    }

    /**
     * @return pitch of this note
     */
    public Pitch pitch() {
        return pitch;
    }

    /**
     * @return instrument that should play this note
     */
    public Instrument instrument() {
        return instrument;
    }
    
    /**
     * @return lyric of this note
     */
    public Optional<Lyric> lyric() {
        return lyric;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.on(this);
    }
    
    /**
     * @return duration of this note
     */
    @Override
    public double duration() {
        return duration;
    }

    /**
     * Play this note.
     */
    @Override
    public void load(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer) {
        player.addNote(instrument, pitch, atBeat, duration);
        if (lyric.isPresent())
            player.addEvent(atBeat, beat -> lyricConsumer.accept(lyric.get()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, pitch, instrument, lyric);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Note other = (Note) obj;
        return duration == other.duration
                && instrument.equals(other.instrument)
                && pitch.equals(other.pitch)
                && lyric.equals(other.lyric);
    }

    @Override
    public String toString() {
        return pitch.toString() + duration;
    }
}
