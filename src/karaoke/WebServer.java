/* Copyright (c) 2017-2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package karaoke;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import karaoke.web.HeadersFilter;
import karaoke.web.LogFilter;

/**
 * HTTP web game server.
 * 
 * <p>PS4 instructions: the specifications of {@link #WebServer(Board, int)},
 * {@link #port()}, {@link #start()}, and {@link #stop()} are required.
 */
public class WebServer {
    
    private final HttpServer server;
    private final ABC song;
    
    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 404;
    
    // Abstraction function:
    //  AF(server, board, players) = a WebServer defined by serverSocket 
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
        
        LogFilter log = new LogFilter();
        HeadersFilter headers = new HeadersFilter();
        // allow requests from web pages hosted anywhere
        headers.add("Access-Control-Allow-Origin", "*");
        // all responses will be plain-text UTF-8
        headers.add("Content-Type", "text/plain; charset=utf-8");

        HttpContext look = server.createContext("/play/", this::handle);
        look.getFilters().addAll(Arrays.asList(log, headers));
    }

    // checks that rep invariant is maintained
    private void checkRep() {
        assert server != null;
        assert song != null;
    }

    private void handle(HttpExchange exchange) throws IOException {
        checkRep();
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String player = path.substring(base.length());

        final String response = "";
        
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.println(response);
        
        exchange.close();
        checkRep();
    }
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        checkRep();
        return server.getAddress().getPort();
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        checkRep();
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
        checkRep();
    }
    
    /**
     * Stop this server. Once stopped, this server cannot be restarted.
     */
    public void stop() {
        checkRep();
        System.err.println("Server will stop");
        server.stop(0);
        checkRep();
    }
}
