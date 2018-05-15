package karaoke.songs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import karaoke.music.Music;
import karaoke.music.Together;

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
     *  defensive copying in instantiation of lyricalElements
     *  all other fields private, final, and of immutable data types
     * 
     * Thread safety argument:
     *  This object is immutable, and there is no beneficent mutation
     */
    
    private final Map<String, Music> parts;
    private final Music music;
    private final int indexNumber;
    private final String title;
    private final Key keySignature;
    private final String composer;
    private final Meter meter;
    private final Meter defaultNote;
    private final Tempo tempo;
    private final int beatsPerMinute;
    
    /**
     * creates a new ABC file
     * 
     * @param parts Musical representation of the ABC file, split up into voice parts
     *              "" is a key reserved for music without a voice part
     * @param fields other fields in the ABC file header. must include
     *      a 'T' field (for title) and a 'K' field (for key signature)
     */
    public ABC(Map<String, Music> parts, Map<Character, Object> fields) {
        this.parts = new HashMap<String, Music>(parts);
        this.music = parts.values().stream().reduce((part1, part2) -> new Together(part1, part2)).get();
        this.indexNumber = (Integer) fields.get('X');
        this.title = (String) fields.get('T');
        this.keySignature = (Key) fields.get('K');
        this.composer = (String) fields.getOrDefault('C', "Unknown");
        
        final Meter defaultMeter = new Meter(4, 4);
        this.meter = (Meter) fields.getOrDefault('M', defaultMeter);
        
        final double baseline = .75;
        final Meter defaultNote1 = new Meter(1, 16);
        final Meter defaultNote2 = new Meter(1, 8);
        this.defaultNote = (Meter) fields.getOrDefault('L', 
                this.meter.value() < baseline ? defaultNote1 : defaultNote2);
        
        final int defaultTempo = 100;
        this.tempo = (Tempo) fields.getOrDefault('Q', new Tempo(defaultNote, defaultTempo));
        
        this.beatsPerMinute = (int) (tempo.beatsPerMinute() * tempo.beatLength() / defaultNote.value());
    }
    
    /** @return the Music associated with this abc piece */
    public Music getMusic() {
        return music;
    }
    
    /**
     * @param voice name of voice part, or "" for music without a voice part
     * @return the Music associated with the specified voice part
     */
    public Music getVoicePart(String voice) {
        return parts.get(voice);
    }
    
    /**
     * @return this piece's index number
     */
    public int getIndexNumber() {
        return indexNumber;
    }

    /** @return this piece's composer */
    public String getTitle() {
        return title;
    }

    /** @return this piece's key signature */
    public Key getKeySignature() {
        return keySignature;
    }
    
    /** @return this piece's composer */
    public String getComposer() {
        return composer;
    }

    /** @return this piece's meter */
    public Meter getMeter() {
        return meter;
    }
    
    /** @return this piece's default note length as a meter fraction */
    public Meter getDefaultNote() {
        return defaultNote;
    }
    
    /** @return this piece's tempo */
    public Tempo getTempo() {
        return tempo;
    }

    /** @return the # of beats per minute in this piece, based on the
     * default note */
    public int getBeatsPerMinute() {
        return beatsPerMinute;
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
        return parts.equals(that.parts)
                && music.equals(that.music) 
                && indexNumber == that.indexNumber
                && title.equals(that.title)
                && keySignature.equals(that.keySignature)
                && composer.equals(that.composer)
                && meter.equals(that.meter)
                && defaultNote.equals(that.defaultNote)
                && tempo.equals(that.tempo)
                && beatsPerMinute == that.beatsPerMinute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts, music, indexNumber, title, keySignature, composer,
                meter, defaultNote, tempo, beatsPerMinute);
    }
    
}