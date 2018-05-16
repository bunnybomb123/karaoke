package karaoke.songs;

import java.util.HashMap;
import java.util.Map;

import karaoke.music.Pitch;

/**
 * An AccidentalMap is a map of pitches to accidentals that is instantiated with a particular key signature
 * and can be updated to accommodate new accidentals.
 */
public class AccidentalMap {
    
    /* Abstraction function
     *  AF(keySignature, accidentals) = the accidental map for keySignature, updated to include mappings
     *      for the accidentals in accidentals
     * 
     * Rep invariant
     *  fields are not null
     * 
     * Safety from rep exposure
     *  all fields are private and final
     *  no fields are returned
     *  defensive copying in instantiation of keySignature
     */
    
    private final Map<String, Integer> keySignature;
    private final Map<Pitch, Integer> accidentals = new HashMap<>();
    
    /**
     * Create a new AccidentalMap for the given key signature.
     * @param keySignature key signature, which contains the sharps and flats of a key
     */
    public AccidentalMap(Map<String, Integer> keySignature) {
        this.keySignature = new HashMap<>(keySignature);
        checkRep();
    }
    
    /*
     * Check rep invariant.
     */
    private void checkRep() {
        assert keySignature != null;
        assert accidentals != null;
    }
    
    /**
     * Update this accidental map with a new accidental.
     * @param pitch note on which an accidental was found, must be a white note
     * @param accidental accidental on note in signed half steps
     */
    public void put(Pitch pitch, int accidental) {
        accidentals.put(pitch, accidental);
        checkRep();
    }
    
    /**
     * @param pitch a white note
     * @return accidental on given note according to this accidental map
     */
    public int get(Pitch pitch) {
        if (accidentals.containsKey(pitch))
            return accidentals.get(pitch);
        else
            return keySignature.getOrDefault(pitch.note(), 0);
    }
    
    /**
     * Removes all accidentals encountered during a measure, leaving only the underlying key signature.
     * Should be called every measure.
     */
    public void refresh() {
        accidentals.clear();
        checkRep();
    }
    
}
