package karaoke.sound;

import java.util.function.Consumer;

/**
 * Music represents a piece of music played by multiple instruments.
 */
public interface Music {

    /**
     * A function from Music to R, defined by cases for each variant.
     * @param <R> the type of the result of the function
     */
    /*public interface Visitor<R> {
        public R on(Concat concat);
        public R on(Note note);
        public R on(Rest rest);
        public R on(Together together);
    }*/

    /**
     * Apply a function (represented by a visitor) to this music.
     * @param <R> the type of the result
     * @param visitor function to apply
     * @return the result of applying visitor on this
     */
    /*public <R> R accept(Visitor<R> visitor);*/

    /**
     * @return total duration of this piece in beats
     */
    double duration();

    /**
     * Play this piece.
     * @param player player to play on
     * @param atBeat when to play
     * @param lyricConsumer function called when new lyrics are played
     */
    void play(SequencePlayer player, double atBeat, Consumer<String> lyricConsumer);

}
