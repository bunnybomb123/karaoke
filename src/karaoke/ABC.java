package karaoke;

import java.util.function.Consumer;

import karaoke.sound.Music;
import karaoke.sound.SequencePlayer;

/**
 * An immutable representation of an abc file.
 */
public class ABC {
    
    /* Abstraction function
     *  AF(music, composer, keySignature, meter, beatsPerMinute) =
     *      an ABC file representing the Music music, with composer composer,
     *      key signature keySignature, meter meter, and beats per minute
     *      beatsPerMinute.
     *      
     * Rep invariant:
     *  all fields not null
     * 
     * Safety from rep exposure:
     *  all fields private, final, and of immutable data types.
     * 
     * Thread safety argument:
     *  This object is immutable, and there is no beneficent mutation
     */
    
    private final Music music;

    private final String composer;
    private final String keySignature;
    private final double meter;
    private final double beatsPerMinute;
    
    /**
     * creates a new ABC file
     * 
     * @param music Musical representation of the ABC file
     * @param keySignature keySignature of the music
     * @param meter meter of the music
     */
    public ABC(Music music, String keySignature, double meter, 
            double beatsPerMinute, String composer) {
        this.music = music;
        this.keySignature = keySignature;
        this.meter = meter;
        this.composer = composer;
        this.beatsPerMinute = beatsPerMinute;
    }
    
    /**
     * Plays the music that the ABC file represents
     * 
     * @param player player to play on
     * @param lyricConsumer function called when new lyrics are played
     */
    public void play(SequencePlayer player, Consumer<String> lyricConsumer) {
        music.play(player, 0, lyricConsumer);
    }
    
    /**
     * @return this piece's music object
     */
    public Music getMusic() {
        return music;
    }

    /**
     * @return this piece's composer
     */
    public String getComposer() {
        return composer;
    }

    /**
     * @return this piece's key signature
     */
    public String getKeySignature() {
        return keySignature;
    }

    /**
     * @return this piece's meter
     */
    public double getMeter() {
        return meter;
    }

    /**
     * @return the number of beats per minute in this piece
     */
    public double getBeatsPerMinute() {
        return beatsPerMinute;
    }
    
}