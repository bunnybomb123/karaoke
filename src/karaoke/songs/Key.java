package karaoke.songs;

import java.util.HashMap;
import java.util.Map;

public enum Key {
    Fbm(-11),
    Cbm(-10),
    Gbm(-9),
    Fb(-8), Dbm(-8),
    Cb(-7), Abm(-7),
    Gb(-6), Ebm(-6),
    Db(-5), Bbm(-5),
    Ab(-4), Fm(-4),
    Eb(-3), Cm(-3),
    Bb(-2), Gm(-2),
    F(-1), Dm(-1),
    C(0), Am(0),
    G(1), Em(1),
    D(2), Bm(2),
    A(3), F$m(3),
    E(4), C$m(4),
    B(5), G$m(5),
    F$(6), D$m(6),
    C$(7), A$m(7),
    G$(8), E$m(8),
    D$(9), B$m(9),
    A$(10),
    E$(11),
    B$(12);
    
    private final String [] sharps = {"F", "C", "G", "D", "A", "E", "B"};
    private final String [] flats = {"B", "E", "A", "D", "G", "C", "F"};
    
    private final Map<String, Integer> keySignature;
    
    private Key(int num) {
        this.keySignature = new HashMap<>();
        for (int i = 0; i < num; i++) {
            String sharp = sharps[i % sharps.length];
            keySignature.put(sharp, keySignature.getOrDefault(sharp, 0) + 1);
        }
        for (int i = 0; i < -num; i++) {
            String flat = flats[i % flats.length];
            keySignature.put(flat, keySignature.getOrDefault(flat, 0) + 1);
        }
    }
    
    public AccidentalMap getAccidentalMap() {
        return new AccidentalMap(keySignature);
    }
}
