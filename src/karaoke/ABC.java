package karaoke;

import java.util.Map;
import java.util.function.Consumer;

import karaoke.sound.Lyric;
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
    private final Meter meter;
    private final double beatsPerMinute;
    private final double defaultNote;
    private final String composer;
    
    /**
     * creates a new ABC file
     * 
     * @param music Musical representation of the ABC file
     * @param other other fields in the ABC file header. must include
     *      a 'T' field (for title) and a 'K' field (for key signature)
     */
    public ABC(Music music, Map<Character, Object> other) {
        this.music = music;
        this.title = (String) other.get('T');
        this.keySignature = (String) other.get('K');
        this.meter = (Meter) other.getOrDefault('M', new Meter(4, 4));
        this.defaultNote = (double) other.getOrDefault('L', 
                this.meter.toDecimal() < .75 ? 1./16 : 1./8);
        
        // must fix; look at spec. if Q is specified it depends on its own L
        this.beatsPerMinute = (int) other.getOrDefault('Q', 100);
        this.composer = (String) other.getOrDefault('C', "Unknown");
    }
    
    /**
     * Plays the music that the ABC file represents
     * 
     * @param player player to play on
     * @param lyricConsumer function called when new lyrics are played
     */
    public void play(SequencePlayer player, Consumer<Lyric> lyricConsumer) {
        music.play(player, 0, lyricConsumer);
    }
    
    /** @return the Music associated with this abc piece */
    public Music getMusic() {
        return music;
    }

    /** @return this piece's composer */
    public String getTitle() {
        return title;
    }

    /** @return this piece's key signature */
    public String getKeySignature() {
        return keySignature;
    }

    /** @return this piece's meter */
    public Meter getMeter() {
        return meter;
    }

    /** @return the # of beats per minute in this piece, based on the
     * default note */
    public double getBeatsPerMinute() {
        return beatsPerMinute;
    }

    /** @return this piece's default note length */
    public double getDefaultNote() {
        return defaultNote;
    }

    /** @return this piece's composer */
    public String getComposer() {
        return composer;
    }
    
    @Override
    public String toString() {
        return "title: " + title + "key: " + keySignature 
                + "music: " + music.toString();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof ABC && sameValue((ABC) that);
    }

    // returns true if that ABC object has same value as this;
    // for use in equals()
    private boolean sameValue(ABC that) {
        return music.equals(that.getMusic()) 
                && title.equals(that.getTitle())
                && keySignature.equals(that.getKeySignature());
    }

    @Override
    public int hashCode() {
        return music.hashCode() + title.hashCode() + keySignature.hashCode();
    }
    
}