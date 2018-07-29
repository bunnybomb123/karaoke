package karaoke.songs;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import karaoke.music.Music;
import karaoke.music.Together;

/**
 * An immutable representation of an abc file.
 */
public class ABC {
    
    /* Abstraction function
     *  AF(parts, music, indexNumber, title, keySignature, composer, voices, meter, defaultNote, tempo, beatsPerMinute) =
     *      an ABC file representing the Music music, with each voice mapped to a part by parts,
     *      with index number indexNumber, title title, key signature keySignature, composer composer,
     *      voices ranging across the set voices, meter meter, default note length defaultNote, tempo tempo, and
     *      beats per minute (where beats are default note lengths) beatsPerMinute
     *      
     * Rep invariant:
     *  all fields not null
     *  voices.equals(parts.keySet())
     *  there is at least one voice
     * 
     * Safety from rep exposure:
     *  defensive copying in instantiation of parts and voices
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
    private final Set<String> voices;
    private final Meter meter;
    private final Meter defaultNote;
    private final Tempo tempo;
    private final int beatsPerMinute;
    
    /**
     * Creates a new ABC song.
     * 
     * @param parts Musical representation of the ABC file, split up into voice parts
     *              "" is a key reserved for music without a voice part
     * @param fields fields in the ABC file header, must include an 'X' field (for index number),
     *              a 'T' field (for title), and a 'K' field (for key signature)
     */
    public ABC(Map<String, Music> parts, Map<Character, Object> fields) {
        if (!fields.containsKey('X') | !fields.containsKey('T') | !fields.containsKey('K')) {
            throw new IllegalArgumentException("Need to specify a title, index number, and key.");
        }
        
        this.parts = Collections.unmodifiableMap(new HashMap<>(parts));
        this.music = parts.values().stream().reduce((part1, part2) -> new Together(part1, part2)).get();
        this.indexNumber = (Integer) fields.get('X');
        this.title = (String) fields.get('T');
        this.keySignature = (Key) fields.get('K');
        this.composer = (String) fields.getOrDefault('C', "Unknown");
        this.voices = parts.keySet();

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
        
        checkRep();
    }
    

    /*
     * Check rep invariant.
     */
    private void checkRep() {
        assert parts != null;
        assert music != null;
        assert title != null;
        assert keySignature != null;
        assert composer != null;
        assert voices != null;
        assert meter != null;
        assert defaultNote != null;
        assert tempo != null;
        assert voices.equals(parts.keySet());
        assert !voices.isEmpty();
    }
    
    /**
     * Gets a summary representation of this piece
     * 
     * @return a summary representation of this piece
     */
    public String getInfo() {
        return title + " by " + composer;
    }
    
    /**
     * Gets the music associated with the whole piece
     * 
     * @return the music associated with this piece
     */
    public Music getMusic() {
        return music;
    }
    
    /**
     * Gets the music associated with a voice part
     * 
     * @param voice name of voice part, or "" for music without a voice part
     * @return the Music associated with the specified voice part
     */
    public Music getVoicePart(String voice) {
        return parts.get(voice);
    }
    
    /**
     * Gets the index number of the piece
     * 
     * @return this piece's index number
     */
    public int getIndexNumber() {
        return indexNumber;
    }

    /**
     *  Gets the composer of the piece
     *  
     *  @return this piece's composer 
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the key signature of the piece
     * 
     *  @return this piece's key signature 
     */
    public Key getKeySignature() {
        return keySignature;
    }
    
    /**
     * Gets the composer of the piece
     * 
     *  @return this piece's composer 
     */
    public String getComposer() {
        return composer;
    }
    
    /**
     * Gets the voices associated with a piece
     * 
     *  @return this piece's voices 
     */
    public Set<String> getVoices() {
        return new HashSet<String>(voices);
    }

    /** 
     * Get the meter associated with the piece
     * 
     * @return this piece's meter 
     */
    public Meter getMeter() {
        return meter;
    }
    
    /** 
     * Gets the default note of the piece
     * 
     * @return this piece's default note length as a meter fraction 
     */
    public Meter getDefaultNote() {
        return defaultNote;
    }
    
    /** 
     * Gets the tempo of the piece
     * 
     * @return this piece's tempo 
     */
    public Tempo getTempo() {
        return tempo;
    }

    /** 
     * Gets the number of beats per minute of the piece
     * 
     * @return the # of beats per minute in this piece, based on the default note 
     */
    public int getBeatsPerMinute() {
        return beatsPerMinute;
    }
    
    @Override
    public String toString() {
        return title + " by " + composer + "\nmusic: " + music + "\nmeter: " + meter
                + "\ntempo: " + tempo + "\ndefaultNote: " + defaultNote + "\nindexNumber: "
                + indexNumber + "\nvoices: "+ voices + "\nkeySignature: "+ keySignature  
                + "\nbeatsPerMinute: "+ beatsPerMinute; 
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof ABC && sameValue((ABC) that);
    }


    /**
     * Determines if two ABC's have equivalent fields
     * 
     * @param that object to compare this object to
     * @return boolean indicating whether or not this object has equivalent fields to the other object
     */
    private boolean sameValue(ABC that) {
        return parts.equals(that.parts)
                && music.equals(that.music) 
                && indexNumber == that.indexNumber
                && title.equals(that.title)
                && keySignature.equals(that.keySignature)
                && composer.equals(that.composer)
                && voices.equals(that.voices)
                && meter.equals(that.meter)
                && defaultNote.equals(that.defaultNote)
                && tempo.equals(that.tempo)
                && beatsPerMinute == that.beatsPerMinute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts, music, indexNumber, title, keySignature, composer, voices,
                meter, defaultNote, tempo, beatsPerMinute);
    }
    
}