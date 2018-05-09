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
    
    // helper function for testing expectedness of Music
    private void play(Music music) {
        final int beatsPerMinute = 50;
        final int ticksPerBeat = 64;
        SequencePlayer player;
        try {
            player = new MidiSequencePlayer(beatsPerMinute, ticksPerBeat);
        } catch (InvalidMidiDataException | MidiUnavailableException e1) {
            throw new RuntimeException("midi problems");
        }
        
        music.load(player, 0, System.out::println);
        
        // add a listener at the end of the piece to tell main thread when it's done
        Object lock = new Object();
        player.addEvent(music.duration(), beat -> {
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
    public void testSample1() {
        //Music music = new Concat(new Note(1, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("bonono"))), new Note(2, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("bananas"))));
        Music music = new Together(new Note(3, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("bananas"))), new Concat(new Rest(0), new Note(3, new Pitch('G'), Instrument.PIANO, Optional.of(new Lyric("sausage")))));
        play(music);
    }
}
