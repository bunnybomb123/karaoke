package karaoke;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.WebServer.ServerListener;
import karaoke.WebServer.Signal;
import karaoke.WebServer.Signal.Type;
import karaoke.parser.ABCParser;
import karaoke.sound.Lyric;
import karaoke.sound.MidiSequencePlayer;
import karaoke.sound.Note;
import karaoke.sound.SequencePlayer;

public class Jukebox {
    
    // TODO does this need to be a ConcurrentLinkedDeque?
    private final Deque<ABC> jukebox = new ConcurrentLinkedDeque<>();
    private Optional<ABC> currentSong = Optional.empty();
    private final List<Listener> listeners = new ArrayList<>();
    
    /**
     * Add a new song to the jukebox.
     * @param song song in ABC format
     */
    public synchronized void addSong(ABC song) {
        jukebox.add(song);
        System.err.println("Added " + song.getTitle() + " at position " + (jukebox.size() + 1) + " in queue");
    }
    
    /**
     * Play the first song in the jukebox, or do nothing if there is no song to play.
     * @return whether play request succeeded
     */
    public synchronized boolean play() {
        if (jukebox.isEmpty()) {
            System.err.println("No song to play");
            return false;
        }
        else {
            ABC song = jukebox.pop();
            currentSong = Optional.of(song);
            System.err.println("current song: " + song.getTitle());
            final int beatsPerMinute = song.getBeatsPerMinute();
            final int ticksPerBeat = 64;
            SequencePlayer player;
            try {
                player = new MidiSequencePlayer(beatsPerMinute, ticksPerBeat);
            } catch (InvalidMidiDataException | MidiUnavailableException e1) {
                throw new RuntimeException("midi problems");
            }
            
            // start song and play
            song.load(player, lyric -> broadcast(Signal.lyric(lyric)));
            player.addEvent(0, beat -> broadcast(Signal.SIGNAL_SONG_START));
            player.addEvent(song.getMusic().duration(), beat -> broadcast(Signal.SIGNAL_SONG_END));
            player.play();
            
            System.err.println("Playing " + song.getTitle());
            return true;
        }
    }
    
    /**
     * Add a server listener for callback.
     * 
     * @param listener listener to add
     */
    public synchronized void addListener(Listener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a server listener from callback.
     * 
     * @param listener listener to remove
     */
    public synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Broadcast a signal to all server listeners.
     * @param signal signal to broadcast
     */
    private synchronized void broadcast(Signal signal) {
        for (Listener listener : new ArrayList<>(listeners))
            listener.signalReceived(signal);
    }
    
    /**
     * A listener for WebServer, called back whenever signal is received.
     */
    public interface Listener {
        
        /**
         * Called back whenever signal is received.
         * @param signal signal broadcast from server
         */
        public void signalReceived(Signal signal);
    
    }
    
    /**
     * Represents a signal broadcast from a Jukebox to any JukeboxListeners.
     */
    public static class Signal {
        
        public static final Signal SIGNAL_SONG_START = new Signal(Type.SONG_START);
        public static final Signal SIGNAL_SONG_END = new Signal(Type.SONG_END);
        public static final Signal SIGNAL_SONG_CHANGE = new Signal(Type.SONG_CHANGE);
        
        /**
         * @param lyric lyric being broadcast
         * @return Signal representing the lyric
         */
        public static Signal lyric(Lyric lyric) {
            return new Signal(lyric);
        }
        
        /**
         * Types of signals, can represent a song starting, a song ending, a new song being loaded, or a lyric being broadcast.
         */
        public static enum Type {
            SONG_START, SONG_END, SONG_CHANGE, LYRIC
        }
        
        private final Type type;
        private final Optional<Lyric> lyric;
        
        // Abstraction function:
        //  AF(type, lyric) = a signal broadcast from a web server to its listeners,
        //      where the type of signal is type, and
        //      if the signal is a lyric broadcast, lyric.get() is the lyric being broadcast
        // Representation invariant:
        //  fields are not null
        //  type == Type.LYRIC implies lyric.isPresent()
        // Safety from rep exposure:
        //  the board that is passed into the constructor (in ServerMain)
        //      is only intended to be used with this WebServer. Thus, no
        //      inadvertent mutation will happen from outside this class.
        // Thread safety argument:
        //  intended to be a singleton server; thus no need to worry about
        //      other threads accessing or mutating it.
        
        private void checkRep() {
            assert type != null;
            assert lyric != null;
            assert type != Type.LYRIC || lyric.isPresent();
        }
        
        /**
         * Creates a Signal representing a lyric.
         * @param lyric lyric being broadcast
         */
        private Signal(Lyric lyric) {
            this.type = Type.LYRIC;
            this.lyric = Optional.of(lyric);
            checkRep();
        }
        
        /**
         * Creates a Signal representing anything other than a lyric.
         * @param type type of signal, must not be Type.LYRIC
         */
        private Signal(Type type) {
            this.type = type;
            this.lyric = Optional.empty();
            checkRep();
        }
        
        /**
         * @return type of signal
         */
        public Type getType() {
            return type;
        }
        
        /**
         * @return lyric being broadcast, requires that this signal's type is Type.LYRIC
         */
        public Lyric getLyric() {
            return lyric.get();
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(type, lyric);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            
            final Signal other = (Signal) obj;
            return type == other.type
                    && lyric.equals(other.lyric);
        }

        @Override
        public String toString() {
            return type == Type.LYRIC ? lyric.get().getBoldedLine() : type.toString();
        }
        
    }
    
}
