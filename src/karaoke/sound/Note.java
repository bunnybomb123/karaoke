package karaoke.sound;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Note represents a note played by an instrument.
 */
public class Note implements Music {

    private final double duration;
    private final Pitch pitch;
    private final Instrument instrument;
    private final Optional<String> lyric;

    private void checkRep() {
        assert duration >= 0;
        assert pitch != null;
        assert instrument != null;
    }

    /**
     * Make a Note played by instrument for duration beats.
     * @param duration duration in beats, must be >= 0
     * @param pitch pitch to play
     * @param instrument instrument to use
     * @param lyric optional lyric to play
     */
    public Note(double duration, Pitch pitch, Instrument instrument, Optional<String> lyric) {
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
    public void play(SequencePlayer player, double atBeat, Consumer<String> lyricConsumer) {
        player.addNote(instrument, pitch, atBeat, duration);
        if (lyric.isPresent())
            player.addEvent(atBeat, beat -> lyricConsumer.accept(lyric.get()));
    }

    @Override
    public int hashCode() {
        long durationBits = Double.doubleToLongBits(duration);
        return (int) (durationBits ^ (durationBits >>> Integer.SIZE))
                + instrument.hashCode()
                + pitch.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Note other = (Note) obj;
        return duration == other.duration
                && instrument.equals(other.instrument)
                && pitch.equals(other.pitch);
    }

    @Override
    public String toString() {
        return pitch.toString() + duration;
    }
}
