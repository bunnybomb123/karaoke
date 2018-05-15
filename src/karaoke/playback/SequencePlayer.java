package karaoke.playback;

import java.util.function.Consumer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import karaoke.lyrics.Lyric;
import karaoke.music.Instrument;
import karaoke.music.Pitch;
import karaoke.songs.ABC;

/**
 * Schedules and plays a sequence of notes at given times.
 * Times and durations are specified in doubles, but implementations of this
 * interface may round to implementation-specific precision.
 * For example, 0.501 beats may be rounded to 0.5 beats.
 * (In MidiSequencePlayer, this precision is controlled by the ticksPerBeat
 * parameter.)
 */
public interface SequencePlayer {

    /**
     * Create a sequence player with a song loaded.
     * @param song ABC song to load into sequence player
     * @param lyricConsumer lyricConsumer function called when new lyrics are played, or null to ignore lyrics
     * @return new SequencePlayer with song loaded
     */
    public static SequencePlayer load(ABC song, Consumer<Lyric> lyricConsumer) {
        final int beatsPerMinute = song.getBeatsPerMinute();
        final int ticksPerBeat = 64;
        
        SequencePlayer player;
        try {
            player = new MidiSequencePlayer(beatsPerMinute, ticksPerBeat);
        } catch (InvalidMidiDataException | MidiUnavailableException e1) {
            throw new RuntimeException("midi problems");
        }
        
        song.getMusic().load(player, 0, lyricConsumer);
        return player;
    }
    
    /**
     * Schedule a note to be played starting at startBeat for the duration numBeats.
     * @param instr instrument for the note
     * @param pitch pitch value of the note
     * @param startBeat the starting beat
     * @param numBeats the number of beats the note is played
     */
    public void addNote(Instrument instr, Pitch pitch, double startBeat, double numBeats);

    /**
     * Schedule a callback when the synthesizer reaches a time.
     * @param atBeat beat at which to call the callback
     * @param callback function to call, with type double->void. 
     *              The double parameter is the time when actually called, in beats.
     *              This time may be slightly different from atBeat because of rounding.
     */
    public void addEvent(double atBeat, Consumer<Double> callback);
    
    /**
     * Play the scheduled music.
     */
    public void play();
    
    /**
     * Play the scheduled music, waiting until the music is finished.
     */
    public void playUntilFinished();

}
