package karaoke.sound;

import java.util.function.Consumer;

/**
 * Music represents a piece of music played by multiple instruments.
 */
public interface Music {
    
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
     * Load this music into specified midi player
     * @param player player to play on
     * @param atBeat when to play
     * @param lyricConsumer function called when new lyrics are played
     */
    void play(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer);

}
