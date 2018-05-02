package karaoke.sound;

import static org.junit.Assert.*;

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
     * @param beatsPerMinute: beats per minute of the song to play. 
     *      Q in abc notation.
     * @param ticksPerBeat: I think it's usually 64
     * @param beats: array of beat counts to play the the song.
     * @param pitches: array of pitches (in abc notation) in the song. 
     *      length must be equal to that of beats.
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    private static void helperPlayFile(final int beatsPerMinute, 
            final int ticksPerBeat, final double[] beats, final char[] pitches) 
                    throws MidiUnavailableException, InvalidMidiDataException {
        Instrument piano = Instrument.PIANO;

        SequencePlayer player = new MidiSequencePlayer(beatsPerMinute, ticksPerBeat);
        
        // addNote(instr, pitch, startBeat, numBeats) schedules a note with pitch value 'pitch'
        // played by 'instr' starting at 'startBeat' to be played for 'numBeats' beats.
        
        double startBeat = 0;
        
        for (int i = 0; i < pitches.length; i++) {
            final double numBeats = beats[i];
            System.out.println("numbeats:"+numBeats);
            char pitch = pitches[i];
            if ((int) pitch >= 97)
                player.addNote(piano, new Pitch((char)((int)pitch-32)).transpose(Pitch.OCTAVE), startBeat, numBeats);
            else    
                player.addNote(piano, new Pitch(pitch), startBeat, numBeats);
            startBeat+=numBeats;
            System.out.println(startBeat);
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
        final double [] beats = {1,1,3./4,1./4,1,3./4,1./4,3./4,1./4,2,1./3,1./3,1./3
                ,1./3,1./3,1./3,1./3,1./3,1./3,1./3,1./3,1./3,3./4,1./4,3./4,1./4,2};
        final char [] pitches = {'C', 'C', 'C', 'D', 'E', 'E', 'D', 'E', 'F',
                'G', 'c', 'c', 'c', 'G', 'G', 'G', 'E', 'E', 'E', 'C', 'C',
                'C', 'G', 'F', 'E', 'D', 'C'};
        helperPlayFile(120, 64, beats, pitches);
    }
    
    @Test
    public void testPiece2() throws MidiUnavailableException, InvalidMidiDataException {
        final double [] beats = {/*TODO*/};
        final char [] pitches = {/*TODO*/};
        helperPlayFile(120 /*TODO*/, 64, beats, pitches);
    }
    
    @Test
    public void testPiece3() throws MidiUnavailableException, InvalidMidiDataException {
        final double [] beats = {/*TODO*/};
        final char [] pitches = {/*TODO*/};
        helperPlayFile(120 /*TODO*/, 64, beats, pitches);
    }
    
}
