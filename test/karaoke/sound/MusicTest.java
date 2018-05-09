package karaoke.sound;

import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.junit.Test;

import karaoke.ABC;

/**
 * Test that Music is played correctly.
 * @category no_didit
 */
public class MusicTest {

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // helper function for testing expectedness of abc files
    private void helperTest(ABC expected, ABC actual) {
        
    }
    
    @Test
    public void testSample1() {
        Music music = new Concat(new Note(1, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("bonono"))), new Note(2, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("bananas"))));
        //Music music = new Concat(new Note(2, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("bananas"))), new Concat(new Rest(2), new Note(1, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("sausage")))));
        final int beatsPerMinute = 180;
        final int ticksPerBeat = 64;
        SequencePlayer player;
        try {
            player = new MidiSequencePlayer(beatsPerMinute, ticksPerBeat);
        } catch (InvalidMidiDataException | MidiUnavailableException e1) {
            throw new RuntimeException("midi problems");
        }
        
        System.out.println("hi");
        
        // start song and play
        music.play(player, 0, lyric -> System.out.println(lyric.getLine()));
        player.play();
    }
}
