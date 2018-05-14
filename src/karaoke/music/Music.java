package karaoke.music;

import java.util.Optional;
import java.util.function.Consumer;

import karaoke.lyrics.Lyric;
import karaoke.playback.SequencePlayer;

/**
 * Music represents a piece of music played by multiple instruments.
 */
public interface Music {
    
    /**
     * @return an empty music sequence
     */
    public static Music empty() {
        return new Rest(0);
    }
    
    /**
     * @param duration duration in beats, must be >= 0
     * @return rest that lasts for duration beats
     */
    public static Music rest(double duration) {
        return new Rest(duration);
    }
    
    /**
     * @param duration duration in beats, must be >= 0
     * @param pitch pitch to play
     * @param instrument instrument to use
     * @param lyric optional lyric to play
     * @return note played by instrument for duration beats, with optional lyric
     */
    public static Music note(double duration, Pitch pitch, Instrument instrument, Optional<Lyric> lyric) {
        return new Note(duration, pitch, instrument, lyric);
    }
    
    /**
     * @param first music to play first
     * @param second music to play second
     * @return music sequence that plays first followed by second
     */
    public static Music concat(Music first, Music second) {
        return new Concat(first, second);
    }
    
    /**
     * @param first first music to play, defines this Music's duration
     * @param second second music to play
     * @return music sequence that plays first at the same time as second
     */
    public static Music together(Music first, Music second) {
        return new Together(first, second);
    }
    
    /*
     * Datatype Definition
     * Music = Rest(duration: double)
     *          + Note(duration: double, pitch: Pitch, instrument: Instrument, lyric: Optional<Lyric>)
     *          + Concat(first: Music, second: Music)
     *          + Together(first: Music, second: Music)
     */

    /**
     * A function from Music to R, defined by cases for each variant.
     * @param <R> the type of the result of the function
     */
    public interface Visitor<R> {
        /**
         * @param concat music as Concat
         * @return result of calling visitor on Concat
         */
        public R on(Concat concat);
        
        /**
         * @param note music as Note
         * @return result of calling visitor on Note
         */
        public R on(Note note);
        
        /**
         * @param rest music as Rest
         * @return result of calling visitor on Rest
         */
        public R on(Rest rest);
        
        /**
         * @param together music as Together
         * @return result of calling visitor on Together
         */
        public R on(Together together);
    }

    /**
     * Apply a function (represented by a visitor) to this music.
     * @param <R> the type of the result
     * @param visitor function to apply
     * @return the result of applying visitor on this
     */
    public <R> R accept(Visitor<R> visitor);

    /**
     * @return total duration of this piece in beats
     */
    double duration();

    /**
     * Load this music into specified SequencePlayer.
     * @param player player to play on
     * @param atBeat when to play
     * @param lyricConsumer function called when new lyrics are played
     */
    void load(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer);

}
