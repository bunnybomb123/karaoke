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
    private final String title;
    private final String keySignature;
//    private final Meter meter;
//    private final double beatsPerMinute;
    
    /**
     * creates a new ABC file
     * 
     * @param music Musical representation of the ABC file
     * @param title title of this abc piece
     * @param keySignature keySignature of the music
     */
    public ABC(Music music, String title, String keySignature) {
        this.music = music;
        this.title = title;
        this.keySignature = keySignature;
        fillInTheRest();
    }
    
    private void fillInTheRest() {
//        if (meter =! null )
//        if (beatsPerMinute != null)
//        this.beatsPerMinute = beatsPerMinute;
//        this.keySignature = keySignature;
//        this.meter = meter;
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
        return title;
    }

    /**
     * @return this piece's key signature
     */
    public String getKeySignature() {
        return keySignature;
    }
    
}