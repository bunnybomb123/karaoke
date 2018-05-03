package karaoke.sound;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.junit.Test;
/**
 * Test some super complicated stuff.
 * @category no_didit
 */
public class SequencePlayerTest {

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    /**
     * helper function that plays out a file as a sort of manual test.
     * @param beatsPerMinute tempo in quarter notes per minute
     * @param ticksPerBeat allows up to 1/ticksPerBeat-beat notes to be played with fidelity
     * @param beats array of beat counts to play the song
     * @param pitches array of pitches (in abc notation) or chords in the song 
     *      length must be equal to that of beats
     *      chords are represented by strings of letters with optional accidentals
     *      rests are represented by "z"
     * @param lyrics array of lyrics aligned to each note
     *      if length is not equal to that of beats, empty lyrics are filled in
     *      and excess lyrics are ignored
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    private static void helperPlayFile(final int beatsPerMinute, final int ticksPerBeat,
            final double[] beats, final String[] pitches, final String[] lyrics) 
                    throws MidiUnavailableException, InvalidMidiDataException {
        Instrument piano = Instrument.PIANO;

        SequencePlayer player = new MidiSequencePlayer(beatsPerMinute, ticksPerBeat);
        
        // addNote(instr, pitch, startBeat, numBeats) schedules a note with pitch value 'pitch'
        // played by 'instr' starting at 'startBeat' to be played for 'numBeats' beats.
        
        double startBeat = 0;
        
        for (int i = 0; i < pitches.length; i++) {
            final double numBeats = beats[i];
            System.out.println("beats: " + numBeats);
            
            for (char pitch : pitches[i].toCharArray())
                if (pitch == 'z')
                    continue;
                else if ((int) pitch >= 97)
                    player.addNote(piano, new Pitch((char)((int)pitch-32)).transpose(Pitch.OCTAVE), startBeat, numBeats);
                else
                    player.addNote(piano, new Pitch(pitch), startBeat, numBeats);
            
            String lyric = i < lyrics.length ? lyrics[i] : "";
            player.addEvent(startBeat, (Double beat) -> System.out.println(lyric));
            
            startBeat += numBeats;
            System.out.println("duration: " + startBeat);
        }
        
        // add a listener at the end of the piece to tell main thread when it's done
        Object lock = new Object();
        player.addEvent(startBeat, (Double beat) -> {
            synchronized (lock) {
                lock.notify();
            }
        });
        
        // print the configured player
        System.out.println(player);

        // play!
        player.play();
        
        // wait until player is done
        // (not strictly needed here, but useful for JUnit tests)
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                return;
            }
        }
        System.out.println("done playing");
    }
    
    @Test
    public void testPiece1() throws MidiUnavailableException, InvalidMidiDataException {
        final double [] beats = {1,1,3./4,1./4,1,3./4,1./4,3./4,1./4,2,1./3,1./3,1./3,
                1./3,1./3,1./3,1./3,1./3,1./3,1./3,1./3,1./3,3./4,1./4,3./4,1./4,2};
        final String [] pitches = {"C", "C", "C", "D", "E", "E", "D", "E", "F",
                "G", "c", "c", "c", "G", "G", "G", "E", "E", "E", "C", "C",
                "C", "G", "F", "E", "D", "C"};
        final String [] lyrics = {};
        helperPlayFile(140, 64, beats, pitches, lyrics);
    }
    
    @Test
    public void testPiece2() throws MidiUnavailableException, InvalidMidiDataException {
        final double [] beats = {1./2,1./2,1./2,1./2,1./2,1./2,1,1,1,1,1,3./2,1./2,1,1,
                1./2,1,1,1./2,1,2./3,2./3,2./3,1,1./2,1./2,1./2,1,1./2,1./2,3./4,3./4};
        final String [] pitches = {"^Fe", "^Fe", "z", "^Fe", "z", "^Fc", "^Fe",
                "GBg", "z", "G", "z", "c", "G", "z", "E", "E", "A", "B", "_B", "A",
                "G", "e", "g", "a", "f", "g", "z", "e", "c", "d", "B", "z"};
        final String [] lyrics = {};
        helperPlayFile(200, 64, beats, pitches, lyrics);
    }
    
    @Test
    public void testPiece3() throws MidiUnavailableException, InvalidMidiDataException {
        final double [] beats = {/*TODO*/};
        final String [] pitches = {/*TODO*/};
        final String [] lyrics = {/*TODO*/};
        helperPlayFile(50, 64, beats, pitches, lyrics);
    }
    
}
