package karaoke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;

import edu.mit.eecs.parserlib.UnableToParseException;
import karaoke.parser.ABCParser;

/**
 * Main entry point of your application.
 */
public class Main {

    /**
     * TODO
     * @param args TODO
     */
    public static void main(String[] args) {
        final Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        
        final int port;
        final String abcFileContents;
        final ABC song;
        
        try {
            port = Integer.parseInt(arguments.remove());
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new IllegalArgumentException("missing or invalid PORT", e);
        }
        
        try (Scanner scanner = new Scanner(new File(arguments.remove()))) {
            abcFileContents = scanner.useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("file not found", e);
        }
        
        try {
            song = ABCParser.parse(abcFileContents);
        } catch (UnableToParseException e) {
            throw new RuntimeException("Unable to parse abc file", e);
        }
        
        try {
            new WebServer(song, port).start();
        } catch (IOException e) {
            throw new RuntimeException("Unable start webserver", e);
        }
        
    }
}
