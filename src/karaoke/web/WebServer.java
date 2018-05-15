package karaoke.web;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import karaoke.lyrics.Lyric;
import karaoke.music.Concat;
import karaoke.music.Instrument;
import karaoke.music.Music;
import karaoke.music.Note;
import karaoke.music.Pitch;
import karaoke.playback.Jukebox;
import karaoke.playback.Jukebox.Listener;
import karaoke.songs.ABC;

/**
 * HTTP web karaoke server.
 */
public class WebServer {
    
    private final HttpServer server;
    private final Jukebox jukebox = new Jukebox();
    
    private static final int SUCCESS_CODE = 200;
    private static ABC newABC() {
        Music m1 = new Note(2, new Pitch('C').transpose(-Pitch.OCTAVE), Instrument.PIANO, Optional.of(new Lyric("1", "hey, babe hey-hey!", 0, 4)));
        Music m2 = new Note(2, new Pitch('C'), Instrument.PIANO, Optional.of(new Lyric("1", "hey, babe hey-hey!", 5, 9)));
        Music m3 = new Note(1, new Pitch('C').transpose(Pitch.OCTAVE), Instrument.PIANO, Optional.of(new Lyric("1", "hey, babe hey-hey!", 10, 13)));
        Music m4 = new Note(1, new Pitch('C').transpose(2*Pitch.OCTAVE), Instrument.PIANO, Optional.of(new Lyric("1", "hey, babe hey-hey!", 14, 18)));
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
    
    /**
     * for debuging use only
     * @param args
     * @throws IOException
     */
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
        server.createContext("/addSong", this::handleAddSong);
        server.createContext("/play", this::handlePlay);
        server.createContext("/textStream", this::handleTextStream);
        server.createContext("/htmlStream", this::handleHtmlStream);
        server.createContext("/htmlWaitReload", this::handleHtmlWaitReload);
    }

    // checks that rep invariant is maintained
    private void checkRep() {
        assert server != null;
        assert jukebox != null;
    }
    
    private void handlePlay(HttpExchange exchange) {
        
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
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");

    }
    
    /**
     * given an HttpExchange, return a PrintWriter that prints to 
     * this exchange
     * @param exchange
     * @return out
     * @throws IOException
     */
    private static PrintWriter helperGetPrintWriter(HttpExchange exchange) throws IOException {
        final int lengthNotKnownYet = 0;
        exchange.sendResponseHeaders(SUCCESS_CODE, lengthNotKnownYet);
        
        // get output stream to write to web browser
        final boolean autoflushOnPrintln = true;
        PrintWriter out = new PrintWriter(
                              new OutputStreamWriter(
                                  exchange.getResponseBody(), 
                                  StandardCharsets.UTF_8), 
                              autoflushOnPrintln);
        return out;
    }
    
    /*
     * adds a listener to the jukebox for every single client connected to the server.
     * The listener listens for lyrics and song start/end/change info, and broadcasts 
     * it to all clients in html format.
     */
    private void setUpLyricsStreaming(PrintWriter out) {
        Listener listener =  new Listener() {
            @Override
            public void signalReceived(Jukebox.Signal signal) {
                System.out.println("Signal is: " + signal);
                switch (signal.getType()) {
                case LYRIC:
                    out.println(signal.getLyric().toHtmlText());
                    break;
                case SONG_CHANGE:
                    out.println("Song changed!");
                    printCurrentSongDetails();
                    break;
                case SONG_END:
                	out.println("Song over.");
                    break;
                case SONG_START:
//                	out.println("Song has begun!");
                    break;
                default:
                    throw new RuntimeException("Should never get here");
                }
            }
            private void printCurrentSongDetails() {
                out.println(jukebox.getCurrentSong().get().getTitle());
            }
        };
        jukebox.addListener(listener);
    }
    
    /**
     * Sends an HTML stream to the web browser
     * 
     * @param exchange request/reply object
     * @throws IOException if network problem
     * @throws InvalidMidiDataException 
     * @throws MidiUnavailableException 
     */
    private void handleHtmlStream(HttpExchange exchange) throws IOException {
        checkRep();
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();
        final String startSong = path.substring(base.length() + 1);

        if (startSong.equals("start")) {
            jukebox.addSong(newABC());
            jukebox.play();
        }
        
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        PrintWriter out = helperGetPrintWriter(exchange);
        try {
            enoughBytesToStartStreaming(out);
            setUpLyricsStreaming(out);
            // autoscroll
            out.println("<script>document.body.scrollIntoView(false)</script>");
        } finally {
//            exchange.close();
            System.err.println("done streaming request");
        }
        checkRep();
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
        final String base = exchange.getHttpContext().getPath();
        final String startSong = path.substring(base.length() + 1);

        if (startSong.equals("start")) {
            jukebox.play();
        }
        
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        PrintWriter out = helperGetPrintWriter(exchange);
        try {
            enoughBytesToStartStreaming(out);
            setUpLyricsStreaming(out);
            out.println("<script>location.reload()</script>");
        } finally {
//            exchange.close();
            System.err.println("done streaming request");
        }
        checkRep();
    }    
    
    private static void enoughBytesToStartStreaming(PrintWriter out) {
        final int enoughBytesToStartStreaming = 2048;
        for (int i = 0; i < enoughBytesToStartStreaming; ++i) {
            out.print(' ');
        }
        out.println(); // also flushes
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
