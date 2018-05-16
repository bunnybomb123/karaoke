package karaoke.music;

import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.junit.Test;

import karaoke.lyrics.Lyric;
import karaoke.music.Concat;
import karaoke.music.Instrument;
import karaoke.music.Music;
import karaoke.music.Note;
import karaoke.music.Pitch;
import karaoke.music.Rest;
import karaoke.music.Together;
import karaoke.playback.MidiSequencePlayer;
import karaoke.playback.SequencePlayer;

/**
 * Test that Music is played correctly.
 * @category no_didit
 */
public class MusicTest {
	
	/* Testing Strategy
	 * input partitions:
	 * 	presence of Note, Together, Concat, Rest
	 * 	notes have duration 0, (0, 1), 1, >1
	 * music has lyrics, doesn't have lyrics
	 * 
	 * Cover all parts
	 */

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
        
        music.load(player, 0, lyric -> System.out.println(lyric.getLine()));
        
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
    
    // input: Note, Together, Rest, has lyrics; duration = 3
    @Test
    public void testSample1() {
        Music music = new Together(new Note(3, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("","bananas"))), new Concat(new Rest(0), new Note(3, new Pitch('G'), Instrument.PIANO, Optional.of(new Lyric("sausage")))));
        play(music);
    }
    
    // input: Concat, Note, has lyrics; duration = 1
    @Test
    public void testSample2() {
        Music music = new Concat(new Note(1, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("","bonono"))), new Note(2, new Pitch('E'), Instrument.PIANO, Optional.of(new Lyric("bananas"))));
        play(music);  
    }
    
    // input: Rest, Note, no lyrics; duration = 1/2, 1/4, 1/8, 1/16, 1/32, 4, 0, 8
    @Test
    public void testMiniScale() {
    	Music C = new Note(1./2, new Pitch('C'), Instrument.PIANO, Optional.empty());
    	Music D = new Note(1./4, new Pitch('D'), Instrument.PIANO, Optional.empty());
    	Music E = new Note(1./8, new Pitch('E'), Instrument.PIANO, Optional.empty());
    	Music F = new Note(1./16, new Pitch('F'), Instrument.PIANO, Optional.empty());
    	Music G = new Note(1./32, new Pitch('G'), Instrument.PIANO, Optional.empty());
    	Music A = new Note(4, new Pitch('A'), Instrument.PIANO, Optional.empty());
    	Music B = new Note(0, new Pitch('B'), Instrument.PIANO, Optional.empty());
    	Music c = new Note(8, new Pitch('C').transpose(Pitch.OCTAVE), Instrument.PIANO, Optional.empty());

    	Music [] array = {C, D,E,F,G,A,B,c}; 
        Music music = concatChain(array);
        play(music);  
    }
    
    /* returns a concatentation of all pieces of music in list */
    private Music concatChain(Music[] list) {
    	Music existing = new Rest(0);
    	for (Music m : list)
    		existing = new Concat(existing, m);
    	return existing;
    }
}
