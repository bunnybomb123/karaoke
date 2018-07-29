package karaoke.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executors;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.lyrics.Lyric;
import karaoke.parser.ABCParser;
import karaoke.playback.Jukebox;
import karaoke.playback.Jukebox.Listener;
import karaoke.playback.Jukebox.Signal;
import karaoke.playback.Jukebox.Signal.Type;
import karaoke.songs.ABC;

/**
 * HTTP web karaoke server.
 */
public class WebServer {
    
    private final HttpServer server;
    private final Jukebox jukebox = new Jukebox();
    
    private static final int SUCCESS_CODE = 200;
    
    // Abstraction function:
    //  AF(server, jukebox) =
    //      a web server that plays songs from a jukebox of ABC songs
    //
    // Representation invariant:
    //  fields are not null
    //
    // Safety from rep exposure:
    //  No fields are passed in as parameters or returned by any methods
    //  All fields are private and final
    //
    // Thread safety argument:
    //  Each exchange:HttpExchange is confined to a single thread
    //  jukebox:Jukebox is threadsafe
    //  server:HttpServer is confined and not used in any handle methods
    //  
    
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
        
        checkRep();
    }

    /**
     * Checks to make sure the rep invariants are satisfied
     */
    private void checkRep() {
        assert server != null;
        assert jukebox != null;
    }

    /**
     * HTTP handler that adds a song to our current list of songs to play
     * 
     * @param exchange http exchange currently in progress
     * @throws IOException thrown if there is a network problem 
     */
    private void handleAddSong(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        PrintWriter out = getPrintWriter(exchange);
        
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();
        final String abcFile = path.length() > base.length() ? path.substring(base.length() + 1) : "";
        
        try (
            Scanner scan = new Scanner(new File("sample-abc/" + abcFile))
        ) {
            ABC song = ABCParser.parse(scan.useDelimiter("\\A").next());
            int position = jukebox.addSong(song);
            if (position == 0)
                out.println("Next song is " + song.getInfo());
            else
                out.println("Added " + song.getInfo() + " at position " + position + " in queue");
        } catch (FileNotFoundException e) {
            out.println(abcFile + " not found");
        } catch (UnableToParseException e) {
            out.println("Unable to parse " + abcFile);
        }
        exchange.close();
    }
    
    /**
     * HTTP handler that plays the next song from our current list of songs to play
     * 
     * @param exchange http exchange currently in progress
     * @throws IOException thrown if there is a network problem
     */
    private void handlePlay(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        PrintWriter out = getPrintWriter(exchange);
        
        boolean success = jukebox.play();
        Optional<ABC> song = jukebox.getCurrentSong();
        if (success)
            out.println("Now playing " + song.get().getInfo());
        else if (jukebox.isPlaying())
            out.println("Jukebox is already playing " + song.get().getInfo());
        else
            out.println("Jukebox is empty");
        exchange.close();
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
        PrintWriter out = getPrintWriter(exchange);
        
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();
        final String voice = path.length() > base.length() ? path.substring(base.length() + 1) : "";
        
        Optional<ABC> next = jukebox.getCurrentSong();
        if (next.isPresent())
            out.println("Next song is " + next.get().getInfo());
        else
            out.println("Jukebox is empty");
        
        jukebox.addListener(new Listener() {
            @Override
            public void signalReceived(Signal signal) {
                try {
                    Optional<ABC> song = jukebox.getCurrentSong();
                    switch(signal.getType()) {
                    case SONG_START:
                        out.println("Now playing " + song.get().getInfo());
                        out.println("--------------------");
                        break;
                    case SONG_END:
                        out.println("--------------------");
                        break;
                    case SONG_CHANGE:
                        if (song.isPresent())
                            out.println("Next song is " + song.get().getInfo());
                        else
                            out.println("Jukebox is empty");
                        break;
                    case LYRIC:
                        Lyric lyric = signal.getLyric();
                        if (!voice.equals("") && song.get().getVoices().size() > 1 && !lyric.getVoice().equals(voice))
                            return;
                        out.println(lyric.toPlainText());
                        break;
                    default:
                        throw new RuntimeException();
                    }
                } catch (Exception e) {
                    exchange.close();
                    jukebox.removeListener(this);
                }
            }
        });
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
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        PrintWriter out = getPrintWriter(exchange);
        
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();
        final String voice = path.length() > base.length() ? path.substring(base.length() + 1) : "";
        
        Optional<ABC> next = jukebox.getCurrentSong();
        if (next.isPresent())
            out.println("Next song is " + next.get().getInfo() + "<br>");
        else
            out.println("Jukebox is empty<br>");
        
        jukebox.addListener(new Listener() {
            @Override
            public void signalReceived(Signal signal) {
                try {
                    Optional<ABC> song = jukebox.getCurrentSong();
                    switch(signal.getType()) {
                    case SONG_START:
                        out.println("Now playing " + song.get().getInfo() + "<br>");
                        out.println("--------------------<br>");
                        break;
                    case SONG_END:
                        out.println("--------------------<br>");
                        break;
                    case SONG_CHANGE:
                        if (song.isPresent())
                            out.println("Next song is " + song.get().getInfo() + "<br>");
                        else
                            out.println("Jukebox is empty<br>");
                        break;
                    case LYRIC:
                        Lyric lyric = signal.getLyric();
                        if (!voice.equals("") && song.get().getVoices().size() > 1 && !lyric.getVoice().equals(voice))
                            return;
                        out.println(lyric.toHtmlText());
                        break;
                    default:
                        throw new RuntimeException();
                    }
                    // send some Javascript to browser that makes it scroll down to the bottom of the page,
                    // so that the last line sent is always in view
                    out.println("<script>document.body.scrollIntoView(false)</script>");
                } catch (Exception e) {
                    exchange.close();
                    jukebox.removeListener(this);
                }
            }
        });
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
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        PrintWriter out = getPrintWriter(exchange);
        
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();
        final String voice = path.length() > base.length() ? path.substring(base.length() + 1) : "";
        
        jukebox.addListener(new Listener() {
            @Override
            public void signalReceived(Signal signal) {
                if (signal.getType() == Type.LYRIC) {
                    ABC song = jukebox.getCurrentSong().get();
                    Lyric lyric = signal.getLyric();
                    if (!voice.equals("") && song.getVoices().size() > 1 && !lyric.getVoice().equals(voice))
                        return;
                    out.println(lyric.toHtmlText());
                    out.println("<script>location.reload()</script>");
                    exchange.close();
                    jukebox.removeListener(this);
                }
            }
        });
    }
    
    /**
     * given an HttpExchange, return a PrintWriter that prints to 
     * this exchange
     * 
     * @param exchange
     * @return out
     * @throws IOException
     */
    private static PrintWriter getPrintWriter(HttpExchange exchange) throws IOException {
        final int lengthNotKnownYet = 0;
        exchange.sendResponseHeaders(SUCCESS_CODE, lengthNotKnownYet);
        
        // get output stream to write to web browser
        final boolean autoflushOnPrintln = true;
        PrintWriter out = new PrintWriter(
                              new OutputStreamWriter(
                                  exchange.getResponseBody(), 
                                  StandardCharsets.UTF_8), 
                              autoflushOnPrintln);
        
        // IMPORTANT: some web browsers don't start displaying a page until at least 2K bytes
        // have been received.  So we'll send a line containing 2K spaces first.
        final int enoughBytesToStartStreaming = 2048;
        for (int i = 0; i < enoughBytesToStartStreaming; ++i) {
            out.print(' ');
        }
        out.println(); // also flushes
        
        return out;
    }
    
    /** 
     * Gets the port on which the server is listening for connections
     * 
     * @return the port on which this server is listening for connections 
     */
    public int port() {
        checkRep();
        return server.getAddress().getPort();
    }
    
    /** 
     * Starts this server in a new background thread.
     */
    public void start() {
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
        checkRep();
    }
    
    /** 
     * Stops this server. Once stopped, this server cannot be restarted. 
     */
    public void stop() {
        System.err.println("Server will stop");
        server.stop(0);
        checkRep();
    }
    
}
