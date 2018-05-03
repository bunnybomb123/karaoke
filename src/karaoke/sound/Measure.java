
import java.util.function.Consumer;

public class Measure implements Music {
    
    private final Music music;
    
    /**
     * Constructs a Measure object to wrap around the given Music object
     * @param music in the measure
     */
    public Measure(Music music) {
        this.music = music;
    }

    @Override
    public double duration() {
        return music.duration();
    }

    @Override
    public void play(SequencePlayer player, double atBeat, Consumer<String> lyricConsumer) {
        music.play(player, atBeat, lyricConsumer);
    }
    
    
}