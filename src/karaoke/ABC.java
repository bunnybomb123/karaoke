package karaoke;

import java.util.Map;
import java.util.function.Consumer;

import karaoke.sound.Lyric;
import karaoke.sound.Music;
import karaoke.sound.SequencePlayer;
import karaoke.sound.Together;

/**
 * An immutable representation of an abc file.
 */
public class ABC {
    
    /* Abstraction function
     *  AF(parts, music, title, keySignature, meter, beatsPerMinute, defaultNote, composer) =
     *      an ABC file representing the Music music, with each voice mapped to a part by parts,
     *      with key signature keySignature, meter meter, beats per minute beatsPerMinute,
     *      default note length defaultNote, and composer composer.
     *      
     * Rep invariant:
     *  all fields not null
     *  parts contains at least 1 key
     * 
     * Safety from rep exposure:
     *  all fields private, final, and of immutable data types.
     * 
     * Thread safety argument:
     *  This object is immutable, and there is no beneficent mutation
     */
    
    private final Map<String, Music> parts;
    private final Music music;
    private final String title;
    private final String keySignature;
    private final Meter meter;
    private final int beatsPerMinute;
    private final double defaultNote;
    private final String composer;
    
    /**
     * creates a new ABC file
     * 
     * @param parts Musical representation of the ABC file, split up into voice parts
     *              "" is a key reserved for music without a voice part
     * @param fields other fields in the ABC file header. must include
     *      a 'T' field (for title) and a 'K' field (for key signature)
     */
    public ABC(Map<String, Music> parts, Map<Character, Object> fields) {
        this.parts = parts;
        this.music = parts.values().stream().reduce((part1, part2) -> new Together(part1, part2)).get();
        this.title = (String) fields.get('T');
        this.keySignature = (String) fields.get('K');
        
        final Meter defaultMeter = new Meter(4, 4);
        this.meter = (Meter) fields.getOrDefault('M', defaultMeter);
        
        final double baseline = .75;
        final double defaultNote1 = 1./16;
        final double defaultNote2 = 1./8;
        this.defaultNote = (double) fields.getOrDefault('L', 
                this.meter.meter() < baseline ? defaultNote1 : defaultNote2);
        
        // must fix; look at spec. if Q is specified it depends on its own L
        this.beatsPerMinute = (int) fields.getOrDefault('Q', 100);
        this.composer = (String) fields.getOrDefault('C', "Unknown");
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
    
    /**
     * @param part name of voice part, or "" for music without a voice part
     * @return the Music associated with the specified voice part
     */
    public Music getVoicePart(String part) {
        return parts.get(part);
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
    public int getBeatsPerMinute() {
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
                && keySignature.equals(that.getKeySignature())
                && meter.equals(that.getMeter())
                && beatsPerMinute == beatsPerMinute
                && defaultNote == defaultNote
                && composer.equals(that.getComposer())
                && parts.equals(that.parts);
    }

    @Override
    public int hashCode() {
        return (int) (music.hashCode() + title.hashCode() + keySignature.hashCode()
        + meter.hashCode() + beatsPerMinute + defaultNote + composer.hashCode())
        + parts.hashCode();
    }
    
}