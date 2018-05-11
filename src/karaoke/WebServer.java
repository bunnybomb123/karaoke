package karaoke;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.Jukebox.Signal;
import karaoke.parser.ABCParser;
import karaoke.sound.Concat;
import karaoke.sound.Instrument;
import karaoke.sound.Lyric;
import karaoke.sound.MidiSequencePlayer;
import karaoke.sound.Music;
import karaoke.sound.Note;
import karaoke.sound.Pitch;
import karaoke.sound.SequencePlayer;

/**
 * HTTP web karaoke server.
 */
public class WebServer {
    
    private final HttpServer server;
    private final Jukebox jukebox = new Jukebox();
    
    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 404;
    
    private static ABC newABC() {
        Music m1 = new Note(2, new Pitch('C').transpose(-Pitch.OCTAVE), Instrument.PIANO, Optional.of(new Lyric("hey,")));
        Music m2 = new Note(2, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("babe")));
        Music m3 = new Note(1, new Pitch('C').transpose(Pitch.OCTAVE), Instrument.PIANO, Optional.of(new Lyric("hey")));
        Music m4 = new Note(1, new Pitch('C').transpose(2*Pitch.OCTAVE), Instrument.PIANO, Optional.of(new Lyric("hey!")));
        Music music = new Concat(m1, new Concat(m2, new Concat(m3, m4)));
        
        final Map<String, Music> parts = new HashMap<>();
        parts.put("", music);
        
        final Map<Character, Object> fields = new HashMap<>();
        fields.put('T', "sample 1");
        fields.put('K', "C");
        
        ABC expected = new ABC(parts, fields);
        return expected;
    }
    // Abstraction function:
    //  AF(server, jukebox, currentSong, listeners) =
    //      a web server that plays songs from a jukebox of ABC songs,
    //      is currently playing currentSong.get() if currentSong.isPresent(),
    //      and has a list of listeners notified whenever the server broadcasts a signal
    //
    // Representation invariant:
    //  fields are not null
    //
    // Safety from rep exposure:
    //  No fields are passed in as parameters or returned by any methods
    //  All fields except currentSong are private and final
    //  currentSong is private and an immutable object
    //
    // Thread safety argument:
    //  Each exchange:HttpExchange is confined to a single thread
    //  All public methods are synchronized by this object's lock
    //  All non-synchronized private methods do not access any fields
    //  
    
    public static void main (String args[]) throws IOException {
        new WebServer(8080).start();
    }
    
    /**
     * Make a new karaoke server that listens for connections on port.
     * 
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    public WebServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());

        // register handlers
        server.createContext("/textStream", this::handleTextStream);
        server.createContext("/htmlStream", this::handleHtmlStream);
        server.createContext("/htmlWaitReload", this::handleHtmlWaitReload);
    }

    // checks that rep invariant is maintained
    private void checkRep() {
        assert server != null;
        assert jukebox != null;
    }
    
    /**
     * This handler sends a plain text stream to the web browser,
     * one line at a time, pausing briefly between each line.
     * Returns after the entire stream has been sent.
     * 
     * @param exchange request/reply object
     * @throws IOException if network problem
     */
    private void handleTextStream(HttpExchange exchange) throws IOException {
        
    }
    
    /**
     * Sends an HTML stream to the web browser
     * 
     * @param exchange request/reply object
     * @throws IOException if network problem
     * @throws InvalidMidiDataException 
     * @throws MidiUnavailableException 
     */
    private void handleHtmlStream(HttpExchange exchange) {
        final String path = exchange.getRequestURI().getPath();
        System.err.println("received request " + path);
        
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        final int lengthNotKnownYet = 0;
        exchange.sendResponseHeaders(SUCCESS_CODE, lengthNotKnownYet);
        
        // get output stream to write to web browser
        final boolean autoflushOnPrintln = true;
        PrintWriter out = new PrintWriter(
                              new OutputStreamWriter(
                                  exchange.getResponseBody(), 
                                  StandardCharsets.UTF_8), 
                              autoflushOnPrintln);
        
        try {
            final int enoughBytesToStartStreaming = 2048;
            for (int i = 0; i < enoughBytesToStartStreaming; ++i) {
                out.print(' ');
            }
            out.println(); // also flushes

            Jukebox.ServerListener listener =  new Jukebox.ServerListener() {
                
                @Override
                public void signalReceived(Jukebox.Signal signal) {
                
                    switch (signal.getType()) {
                    case LYRIC:
                        out.println(signal.getLyric());
                        break;
                    case SONG_CHANGE:
                        out.println("Song changed!");
                        printCurrentSongDetails();
                        break;
                    case SONG_END:
                        break;
                    case SONG_START:
                        break;
                    default:
                        throw new RuntimeException("Should never get here");
                    }
                }
                private void printCurrentSongDetails() {
                    out.println(jukebox.getCurrentSong());
                }
            };
            addListener(listener);
            // TODO add song-is-over listener
            play();
            out.println("<script>document.body.scrollIntoView(false)</script>");
        } finally {
            exchange.close();
            System.err.println("done streaming request");
        }
    }

    /**
     * This handler waits for an event to occur in the server
     * before sending a complete HTML page to the web browser.
     * The page ends with a Javascript command that immediately starts
     * reloading the page at the same URL, which causes this handler to be
     * run, wait for the next event, and send an updated HTML page.
     * In this simple example, the "server event" is just a brief timeout, but it
     * could synchronize with another thread instead.
     * 
     * @param exchange request/reply object
     * @throws IOException if network problem
     */
    private void handleHtmlWaitReload(HttpExchange exchange) throws IOException {
        checkRep();
        
        final String path = exchange.getRequestURI().getPath();
        System.err.println("received request " + path);

        // must call sendResponseHeaders() before calling getResponseBody()
        final int successCode = 200;
        final int lengthNotKnownYet = 0;
        exchange.sendResponseHeaders(successCode, lengthNotKnownYet);

        // get output stream to write to web browser
        final boolean autoflushOnPrintln = true;
        PrintWriter out = new PrintWriter(
                              new OutputStreamWriter(
                                  exchange.getResponseBody(), 
                                  StandardCharsets.UTF_8), 
                              autoflushOnPrintln);
        
        try {

            // Wait until an event occurs in the server.
            // In this example, the event is just a brief fixed-length delay, but it
            // could synchronize with another thread instead.
            final int millisecondsToWait = 200;
            try {
                Thread.sleep(millisecondsToWait);
            } catch (InterruptedException e) {
                return;
            }
            
            // Send a full HTML page to the web browser
            out.println(System.currentTimeMillis() + "<br>");
            
            out.println("<script>location.reload()</script>");
            
        } finally {
            exchange.close();
        }
        System.err.println("done streaming request");
        
    }    
    
    /** @return the port on which this server is listening for connections */
    public synchronized int port() {
        checkRep();
        return server.getAddress().getPort();
    }
    
    /** Start this server in a new background thread. */
    public synchronized void start() {
        checkRep();
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
        checkRep();
    }
    
    /** Stop this server. Once stopped, this server cannot be restarted. */
    public synchronized void stop() {
        checkRep();
        System.err.println("Server will stop");
        server.stop(0);
        checkRep();
    }
    
}
