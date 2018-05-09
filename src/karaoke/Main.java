package karaoke;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Main entry point of your application.
 */
public class Main {

    /**
     * Starts a web server ready to play ABC songs.
     * @param args list of .abc files to add to server's jukebox
     */
    public static void main(String[] args) {
        final Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        
        final int port;
        
        try {
            port = Integer.parseInt(arguments.remove());
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new IllegalArgumentException("missing or invalid PORT", e);
        }
        
        try {
            new WebServer(port).start();
        } catch (IOException e) {
            throw new RuntimeException("Unable start webserver", e);
        }
        return;
//        
//        try (Scanner scanner = new Scanner(new File(arguments.remove()))) {
//            abcFileContents = scanner.useDelimiter("\\Z").next();
//        } catch (FileNotFoundException e) {
//            throw new IllegalArgumentException("file not found", e);
//        }
//        
//        try {
//            song = ABCParser.parse(abcFileContents);
//        } catch (UnableToParseException e) {
//            throw new RuntimeException("Unable to parse abc file", e);
//        }
//        
        
    }
}
