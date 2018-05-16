package karaoke.web;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.BindException;
import java.net.URL;
import java.util.Scanner;

import org.junit.Test;

/**
 * Test WebServer
 * @category no_didit
 */
public class WebServerTest {
    
    /*
     * Testing Strategy for WebServer.java:
     *  
     *  start: success, BindException
     *  
     *  add song requests: next song, queue, fail
     *  
     *  play requests: success, fail because empty, fail because busy
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // start: success
    @Test
    public void testStartServerNoError() throws IOException {
        // Tests to make sure 
        final int port = 8080;
        WebServer server = new WebServer(port);
        server.start();
        server.stop();
    }
    
    // start: BindException
    @Test(expected=BindException.class)
    public void testStartTwoServersSamePort() throws IOException {
        // Tests to make sure an error is thrown when two servers listen on the same port
        final int port = 8081;
        WebServer server = new WebServer(port);
        server.start();
        try {
            // Should fail here
            new WebServer(port).start();
            fail();
        } finally {
            server.stop();
        }
    }
    
    // play requests: fail because empty
    @Test
    public void testPlayEmptyRequest() throws IOException {
        final int port = 8082;
        WebServer server = new WebServer(port);
        server.start();
        try {
            checkResponse(server, "/play", "Jukebox is empty");
        } finally {
            server.stop();
        }
    }
    
    // add song requests: next song, queue; play requests: success, fail because busy
    @Test
    public void testPlaySongsRequest() throws IOException {
        final int port = 8083;
        WebServer server = new WebServer(port);
        server.start();
        try {
            checkResponse(server, "/addSong/fur_elise.abc", "Next song is Bagatelle No.25 in A, WoO.59 by Ludwig van Beethoven");
            checkResponse(server, "/addSong/scale.abc", "Added Simple scale by Unknown at position 1 in queue");
            checkResponse(server, "/play", "Now playing Bagatelle No.25 in A, WoO.59 by Ludwig van Beethoven");
            checkResponse(server, "/play", "Jukebox is already playing Bagatelle No.25 in A, WoO.59 by Ludwig van Beethoven");
        } finally {
            server.stop();
        }
    }
    
    // add song requests: fail
    @Test
    public void testPlayInvalidFile() throws IOException {
        final int port = 8084;
        WebServer server = new WebServer(port);
        server.start();
        try {
            checkResponse(server, "/addSong/bad.abc", "bad.abc not found");
        } finally {
            server.stop();
        }
    }
    
    /**
     * Assert that response when sending request to server is expected response.
     * @param server the web server
     * @param request request to server, such as "/addSong/sample1.abc" or "/play"
     * @param expected expected response
     * @throws IOException if request fails to send
     */
    private static void checkResponse(WebServer server, String request, String expected) throws IOException {
        Scanner response = new Scanner(new URL("http://localhost:" + server.port() + request).openStream());
        String actual = response.useDelimiter("\\A").next();
        response.close();
        assert actual.contains(expected);
    }
}