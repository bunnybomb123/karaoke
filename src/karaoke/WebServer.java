package karaoke;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import karaoke.sound.MidiSequencePlayer;
import karaoke.sound.SequencePlayer;

/**
 * HTTP web karaoke server.
 */
public class WebServer {
    
    private final HttpServer server;
    private final ABC song;
    
    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 404;
    
    // Abstraction function:
    //  AF(server, song) = a WebServer defined by serverSocket 
    //      that runs the Memory Scramble game with game board board, and 
    //      multiple players players whose actions can be accessed via web 
    //      by their key in players
    // Representation invariant:
    //  fields are not null
    // Safety from rep exposure:
    //  the board that is passed into the constructor (in ServerMain)
    //      is only intended to be used with this WebServer. Thus, no
    //      inadvertent mutation will happen from outside this class.
    // Thread safety argument:
    //  intended to be a singleton server; thus no need to worry about
    //      other threads accessing or mutating it.
    
    /**
     * Make a new karaoke server using board that listens for connections on port.
     * 
     * @param song song to play
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    public WebServer(ABC song, int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.song = song;
        
        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());

        // register handlers
//        server.createContext("/textStream", this::handleText);
        server.createContext("/htmlStream", this::handleHtmlStream);
        server.createContext("/htmlWaitReload", this::handleHtmlWaitReload);

        // start the server
        server.start();
        System.out.println("server running");
    }

    // checks that rep invariant is maintained
    private void checkRep() {
        assert server != null;
        assert song != null;
    }

    /**
     * This handler sends a stream of HTML to the web browser,
     * pausing briefly between each line of output.
     * Returns after the entire stream has been sent.
     * 
     * @param exchange request/reply object
     * @throws IOException if network problem
     */
    private void handleHtmlStream(HttpExchange exchange) throws IOException {
        final String path = exchange.getRequestURI().getPath();
        System.err.println("received request " + path);
    
        final boolean autoscroll = true; //path.endsWith("/autoscroll");
        
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(SUCCESS_CODE, 0);

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
            
            final int beatsPerMinute = song.getBeatsPerMinute();
            final int ticksPerBeat = 64;
            SequencePlayer player = new MidiSequencePlayer(beatsPerMinute, ticksPerBeat);
           
            player.addEvent(startBeat, (Double beat) -> {
                synchronized (lock) {
                    lock.notify();
                }
            });
            for (int i = 0; i < numberOfLinesToSend; ++i) {
                
                // start song and play
                this.song.play(player, (lyric) -> out.println(lyric + "<br>"));

                if (autoscroll)
                    out.println("<script>document.body.scrollIntoView(false)</script>");
                
                // wait a bit
                try {
                    Thread.sleep(millisecondsBetweenLines);
                } catch (InterruptedException e) {
                    return;
                }
            }
            
        } finally {
            exchange.close();
        }
        System.err.println("done streaming request");
    }

    private void handleHtmlWaitReload(HttpExchange exchange) throws IOException {
        checkRep();
        
        final String path = exchange.getRequestURI().getPath();
        System.err.println("received request " + path);

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(SUCCESS_CODE, 0);

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
    public int port() {
        checkRep();
        return server.getAddress().getPort();
    }
    
    /** Start this server in a new background thread. */
    public void start() {
        checkRep();
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
        checkRep();
    }
    
    /** Stop this server. Once stopped, this server cannot be restarted. */
    public void stop() {
        checkRep();
        System.err.println("Server will stop");
        server.stop(0);
        checkRep();
    }
}



class StreamingExample {

    /**
     * Web server that demonstrates several ways to stream text to a web browser.
     *     
     * @param args not used
     * @throws IOException if network failure
     */
    public static void main(String[] args) throws IOException {
        
        // make a web server
        final int serverPort = 4567;
        final HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        
        // handle concurrent requests with multiple threads
        
        System.out.println("http://localhost:4567/textStream");
        System.out.println("http://localhost:4567/htmlStream");
        System.out.println("http://localhost:4567/htmlStream/autoscroll");
        System.out.println("http://localhost:4567/htmlWaitReload");
    }
    
    /**
     * This handler sends a plain text stream to the web browser,
     * one line at a time, pausing briefly between each line.
     * Returns after the entire stream has been sent.
     * 
     * @param exchange request/reply object
     * @throws IOException if network problem
     */
    private static void textStream(HttpExchange exchange) throws IOException {
        final String path = exchange.getRequestURI().getPath();
        System.err.println("received request " + path);

        // plain text response
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");

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
            // IMPORTANT: some web browsers don't start displaying a page until at least 2K bytes
            // have been received.  So we'll send a line containing 2K spaces first.
            final int enoughBytesToStartStreaming = 2048;
            for (int i = 0; i < enoughBytesToStartStreaming; ++i) {
                out.print(' ');
            }
            out.println(); // also flushes
            
            final int numberOfLinesToSend = 100;
            final int millisecondsBetweenLines = 200;
            for (int i = 0; i < numberOfLinesToSend; ++i) {
                
                // print a line of text
                out.println(System.currentTimeMillis()); // also flushes

                // wait a bit
                try {
                    Thread.sleep(millisecondsBetweenLines);
                } catch (InterruptedException e) {
                    return;
                }
            }
            
        } finally {
            exchange.close();
        }
        System.err.println("done streaming request");
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
    private static void htmlWaitReload(HttpExchange exchange) throws IOException {
        final String path = exchange.getRequestURI().getPath();
        System.err.println("received request " + path);

        // html response
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        
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
            
            // End the page with Javascript that causes the browser to immediately start 
            // reloading this URL, so that this handler runs again and waits for the next event
            out.println("<script>location.reload()</script>");
            
        } finally {
            exchange.close();
        }
        System.err.println("done streaming request");
    }

}
