package karaoke.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.BindException;
import java.net.URL;
import java.util.Scanner;

import org.junit.Test;


public class WebServerTest {
    
    /*
     * Testing Strategy for WebServer.java:
     * Functions: port, start, stop, WebServer (Constructor)
     * inputs: port
     * outputs: 
     * 
     * constructor:
     * 	Partitions: 
     * 		port number: valid port number, invalid port number, used port number
     * 	Input files: valid file, invalid file
     * 	Number of input files: 0, 1, 2+ 
     * 
     * Cover all parts
     */
    
    private static int PORT_NUMBER = 8080;
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testStartServerNoError() throws IOException {
        // Tests to make sure 
        WebServer server = new WebServer(PORT_NUMBER);
        server.start();
        server.stop();
    }
    
    @Test(expected=BindException.class)
    public void testStartTwoServersSamePort() throws IOException {
        // Tests to make sure an error is thrown when two servers listen on the same port
        WebServer server = new WebServer(PORT_NUMBER);
        server.start();
        try {
            // Should fail here
            new WebServer(PORT_NUMBER).start();
            fail();
        } finally {
            server.stop();
        }
    }
    
    @Test
    public void testPlayEmptyRequest() throws IOException {
        WebServer server = new WebServer(PORT_NUMBER);
        server.start();
        try {
            checkResponse(server, "/play", "Jukebox is empty");
        } finally {
            server.stop();
        }
    }
    
    @Test
    public void testPlaySongsRequest() throws IOException {
        WebServer server = new WebServer(PORT_NUMBER);
        server.start();
        try {
            checkResponse(server, "/addSong/sample1.abc", "Next song is sample 1 by Unknown");
            checkResponse(server, "/play", "Now playing sample 1 by unknown");
        } finally {
            server.stop();
        }
    }
        
    @Test
    public void testPlayFile() {
    	// Manual Test:
        // Start the server
        // Send the request to load with multiple files
        // Send the play request
        // Wait a certain amount of time
        // Make sure it ends
        // Make sure it printed all the correct lyrics
    }
    
    @Test
    public void testPlayInvalidFile() {
        
    }
    
    /**
     * Assert that response when sending request to server is expected response.
     * @param server the web server
     * @param request request to server, such as "/addSong/sample1.abc" or "/play"
     * @param expected expected response
     * @throws IOException if request fails to send
     */
    private static void checkResponse(WebServer server, String request, String expected) throws IOException {
        Scanner response = new Scanner(new URL("http://localhost:" + PORT_NUMBER + request).openStream());
        String actual = response.useDelimiter("\\A").next();
        response.close();
        assert actual.contains(expected);
    }
}