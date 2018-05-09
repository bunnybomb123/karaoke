package karaoke.web;

import static org.junit.Assert.assertEquals; 
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.junit.Test;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.ABC;
import karaoke.sound.Concat;
import karaoke.sound.Instrument;
import karaoke.sound.Music;
import karaoke.sound.Note;
import karaoke.sound.Pitch;


public class WebServerTest {
    
    /*
     * Testing Strategy for WebServer.java:
     * 
     * Strategy for WebServer constructor:
     * Partitions: 
     * port number: valid port number, invalid port number, used port number
     * Input files: valid file, invalid file
     * Number of input files: 0, 1, 2+ 
     * 
     * 
     * 
     * 
     * 
     * 
     */
    
    private int PORT_NUMBER = 8080;
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testStartServerNoError() throws IOException {
        // Tests to make sure 
        karaoke.WebServer server = new karaoke.WebServer(PORT_NUMBER);
        
    }
    
    @Test
    public void testStartTwoServersSamePort() throws IOException {
        // Tests to make sure an error is thrown when two servers listen on the same port
        
        karaoke.WebServer server = new karaoke.WebServer(PORT_NUMBER);
        try {
            // Should fail here
            karaoke.WebServer secondServer = new karaoke.WebServer(PORT_NUMBER);
            assert false;
        } catch (IOException e) {
            
        }
    }
        
    
    @Test
    public void testPlayFile() {
        // Start the server
        // Send the request to load with multiple files
        // Send the play request
        // Wait a certain amount of time
        // Make sure it ends
        // Make sure it printed all the correct lyrics
    }
    
    @Test public void testPlayInvalidFile() {
        
    }
    
    
}