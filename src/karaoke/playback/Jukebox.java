package karaoke.playback;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import karaoke.lyrics.Lyric;
import karaoke.songs.ABC;

public class Jukebox {
    
    /* Abstraction function
     *  AF(currentSong, queuedSongs, isPlaying, listeners)
     *      = a jukebox that is currently playing currentSong, has
     *      queuedSongs waiting in the queue.
     *      
     * Representation invariant
     *  no fields are null
     *  
     * Safety from rep exposure
     *  
     * 
     * Thread safety argument
     * 
     */
    
    private Optional<ABC> currentSong = Optional.empty();
    private final Deque<ABC> queuedSongs = new ArrayDeque<>();
    private boolean isPlaying = false;
    private final List<Listener> listeners = new ArrayList<>();
    
    
    /**
     * Create a new empty Jukebox.
     */
    public Jukebox() {}
    
    /**
     * @return song being played or next to be played if jukebox is not playing,
     *         may not exist if jukebox is empty
     */
    public synchronized Optional<ABC> getCurrentSong() {
        return currentSong;
    }
    
    /**
     * @return list of songs in queue
     */
    public synchronized List<ABC> getQueuedSongs() {
        return new ArrayList<>(queuedSongs);
    }
    
    /**
     * @return whether jukebox is playing a song
     */
    public synchronized boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * Add a new song to the jukebox.
     * @param song song in ABC format
     */
    public synchronized void addSong(ABC song) {
        queuedSongs.add(song);
        if (!currentSong.isPresent())
            updateCurrentSong();
        System.err.println("Added " + song.getTitle() + " at position " + (queuedSongs.size() + 1) + " in queue");
    }
    
    /**
     * Play the first song in the jukebox, or do nothing if there is no song to play.
     * @return whether play request succeeded
     */
    public synchronized boolean play() {
        if (!currentSong.isPresent()) {
            System.err.println("No song to play");
            return false;
        }
        
        ABC song = currentSong.get();
        SequencePlayer player = SequencePlayer.load(song, lyric -> broadcast(Signal.lyric(lyric)));
        player.addEvent(0, beat -> {
            isPlaying = true;
            broadcast(Signal.SIGNAL_SONG_START);
        });
        player.addEvent(song.getMusic().duration(), beat -> {
            isPlaying = false;
            updateCurrentSong();
            broadcast(Signal.SIGNAL_SONG_END);
        });
        player.play();
        
        System.err.println("Playing " + song.getTitle());
        return true;
    }
    
    /**
     * Updates current song by taking from first song in queue, if it exists.
     * Replaces current song if it exists.
     */
    private synchronized void updateCurrentSong() {
        currentSong = queuedSongs.isEmpty() ? Optional.empty() :
                                              Optional.of(queuedSongs.remove());
        broadcast(Signal.SIGNAL_SONG_CHANGE);
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
            return type == Type.LYRIC ? lyric.get().toPlainText() : type.toString();
        }
        
    }
    
}
