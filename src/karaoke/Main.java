package karaoke;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import karaoke.web.WebServer;

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
        
        final int port = 8080;
        final WebServer server;
        
        try {
            server = new WebServer(port);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException("unable to start webserver", e);
        }
        
        while (!arguments.isEmpty())
            addSong(arguments.remove());
        
        String publicIPAddress = getPublicIPAddress();
        
        System.out.println("Server running, browse to one of these URLs to view lyrics for a particular voice");
        System.out.println("or omit the voice to view lyrics for a piece without voices:");
        System.out.println("http://" + publicIPAddress + ":8080/textStream[/voice]");
        System.out.println("http://" + publicIPAddress + ":8080/htmlStream[/voice]");
        System.out.println("http://" + publicIPAddress + ":8080/htmlWaitReload[/voice]");
        System.out.println();
        System.out.println("To add songs to the jukebox, enter the command \"addSong sample.abc\" or browse to:");
        System.out.println("http://" + publicIPAddress + ":8080/addSong/[songfile]");
        System.out.println("To play the next song, enter the command \"play\" or browse to:");
        System.out.println("http://" + publicIPAddress + ":8080/play");
        System.out.println("To end the server, enter the command \"quit\".");
        System.out.println("Ready for commands:");
        
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String[] command = in.nextLine().split("\\s+");
            try {
                if (command[0].equals("addSong"))
                    addSong(command[1]);
                else if (command[0].equals("play"))
                    play();
                else if (command[0].equals("quit"))
                    break;
                else
                    System.out.println("invalid command");
            } catch (Exception e) {
                System.out.println("invalid command");
            }
        }
        
        server.stop();
        in.close();
        System.out.println("bye");
        System.exit(0);
    }
    
    /**
     * Add a song to the server's jukebox.
     * @param abcFile song file to add
     */
    private static void addSong(String abcFile) {
        try (
            Scanner response = new Scanner(new URL("http://localhost:8080/addSong/" + abcFile).openStream())
        ) {
            System.out.println(response.useDelimiter("\\A").next());
        } catch (IOException e) {
            System.err.println("unable to send request");
        }
    }
    
    /**
     * Play the next song on the server's jukebox.
     */
    private static void play() {
        try (
            Scanner response = new Scanner(new URL("http://localhost:8080/play").openStream())
        ) {
            System.out.println(response.useDelimiter("\\A").next());
        } catch (IOException e) {
            System.err.println("unable to send request");
        }
    }
    
    /**
     * @return the server's public IP address
     */
    private static String getPublicIPAddress() {
        try (
            Scanner response = new Scanner(new URL("http://bot.whatismyipaddress.com").openStream())
        ) {
            return response.useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException("unable to obtain public IP address");
        }
    }
    
//    final String abcFileContents;
//    
//    try (Scanner scanner = new Scanner(new File(abcFile))) {
//        abcFileContents = scanner.useDelimiter("\\Z").next();
//    } catch (FileNotFoundException e) {
//        System.err.println(abcFile + " not found");
//        return;
//    }
//    
//    final ABC song;
//    
//    try {
//        song = ABCParser.parse(abcFileContents);
//    } catch (UnableToParseException e) {
//        System.err.println("unable to parse " + abcFile);
//        return;
//    }
}
