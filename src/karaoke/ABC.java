import java.util.function.Consumer;

import karaoke.sound.Music;
import karaoke.sound.Pitch;
import karaoke.sound.SequencePlayer;

public class ABC {
    
    // Fields:
    private final Music music;
    private String composer;
    private Pitch keySignature;
    private double meter;
    
    
    /**
     * Representation of an ABC file
     * 
     * @param music Musical representation of the ABC file
     * @param keySignature keySignature of the music
     * @param meter meter of the music
     */
    public ABC(Music music, Pitch keySignature, double meter) {
        this.music = music;
        this.keySignature = keySignature;
        this.meter = meter;
    }
    
    /**
     * Plays the music that the ABC file represents
     * 
     * @param player music player to play the music on
     * @param atBeat 
     * @param lyricConsumer
     */
    public void play(SequencePlayer player, double atBeat, Consumer<String> lyricConsumer) {
        music.play(player, 0, lyricConsumer);
    }
    
    
}