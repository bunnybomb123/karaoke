package karaoke.songs;

import java.util.HashMap;
import java.util.Map;

import karaoke.music.Pitch;

public class AccidentalMap {
    
    private final Map<String, Integer> keySignature;
    private final Map<Pitch, Integer> accidentals = new HashMap<>();
    
    public AccidentalMap(Map<String, Integer> keySignature) {
        this.keySignature = new HashMap<>(keySignature);
    }
    
    public void setAccidental(Pitch pitch, int accidental) {
        accidentals.put(pitch, accidental);
    }
    
    public int getAccidental(Pitch pitch) {
        if (accidentals.containsKey(pitch))
            return accidentals.get(pitch);
        else
            return keySignature.getOrDefault(pitch.note(), 0);
    }
    
}
