package karaoke.sound;

import java.util.function.Consumer;

public class Measure implements Music {
    
    private final double duration;
    private final Music music;
    
    /**
     * Constructs a Measure object to wrap around the given Music object
     * @param duration duration in beats, must be >= 0
     * @param music music in measure, must satisfy music.duration() <= duration
     */
    public Measure(double duration, Music music) {
        this.duration = duration;
        this.music = music;
    }

    @Override
    public double duration() {
        return duration;
    }

    @Override
    public void play(SequencePlayer player, double atBeat, Consumer<Lyric> lyricConsumer) {
        music.play(player, atBeat, lyricConsumer);
    }
    
    
}