package karaoke.songs;

import java.util.HashMap;
import java.util.Map;

import karaoke.music.Pitch;

public class AccidentalMap {
    
    private final Map<String, Integer> keySignature;
    private final Map<Pitch, Integer> accidentals = new HashMap<>();
    
    /**
     * Create a new AccidentalMap for the given key signature.
     * @param keySignature key signature, which contains the sharps and flats of a key
     */
    public AccidentalMap(Map<String, Integer> keySignature) {
        this.keySignature = new HashMap<>(keySignature);
    }
    
    /**
     * Update this accidental map with a new accidental.
     * @param pitch note on which an accidental was found, must be a white note
     * @param accidental accidental on note in signed half steps
     */
    public void put(Pitch pitch, int accidental) {
        accidentals.put(pitch, accidental);
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
    }
    
}
