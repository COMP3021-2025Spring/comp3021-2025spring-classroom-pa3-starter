/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * ChatManager class
 */
public class ChatManager {
    /**
     * The shell prompt for the ChatManager repl
     */
    static final String replPrompt = Utils.toInfo("ChatManager> ");

    /**
     * The banner, generated from: <a href="https://patorjk.com/software/taag/#p=display&f=Ogre&t=LLM%20ChatManager">...</a>
     */
    static final String banner = """
               __    __             ___ _           _                                           \s
              / /   / /   /\\/\\     / __\\ |__   __ _| |_  /\\/\\   __ _ _ __   __ _  __ _  ___ _ __\s
             / /   / /   /    \\   / /  | '_ \\ / _` | __|/    \\ / _` | '_ \\ / _` |/ _` |/ _ \\ '__|
            / /___/ /___/ /\\/\\ \\ / /___| | | | (_| | |_/ /\\/\\ \\ (_| | | | | (_| | (_| |  __/ |  \s
            \\____/\\____/\\/    \\/ \\____/|_| |_|\\__,_|\\__\\/    \\/\\__,_|_| |_|\\__,_|\\__, |\\___|_|  \s
                                                                                 |___/          \s
            """;

    /**
     * The menu, a map of command and description
     */
    static final Map<String, String> menus = new LinkedHashMap<>() {
        {
            put("chat", "start a new chat session");
            put("list", "list previous sessions");
            put("load", "load from a previous session");
            put("help", "show this help message");
            put("exit", "exit the program");
        }
    };

    static {
        Logger.getLogger("org.jline").setLevel(Level.OFF);
        Logger.getLogger("org.reflections").setLevel(Level.OFF);
    }

    /**
     * Initialize the terminal for jline
     */
    static final Terminal terminal;

    static {
        try {
            terminal = TerminalBuilder.builder().jansi(true).system(true).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Print the help message
     */
    private static void printHelp() {
        System.out.println("Available commands:");
        for (Map.Entry<String, String> entry : menus.entrySet()) {
            System.out.print("- ");
            Utils.printInfo(entry.getKey());
            System.out.println(": " + entry.getValue());
        }
    }

    /**
     * Get all the subclasses of ChatClient
     *
     * @return a set of subclasses of ChatClient
     */
    public static Set<Class<? extends ChatClient>> getSubClasses() {
        Reflections reflections = new Reflections("hk.ust.cse.comp3021");
        return reflections.getSubTypesOf(ChatClient.class);
    }

    /**
     * Show names of all available chat clients
     */
    public static String getChatClientNames() {
        List<String> clientNames = new ArrayList<>();
        try {
            for (Class<? extends ChatClient> subType : getSubClasses()) {
                String clientName = subType.getField("clientName").get(null).toString();
                if (clientName.contains("_"))
                    Utils.printlnError("Invalid client name " + clientName + ", ignored");
                else
                    clientNames.add(clientName);
            }
        } catch (ReflectiveOperationException e) {
            Utils.printlnError(e.getMessage());
        }
        return String.join(" | ", clientNames);
    }

    /**
     * Invalid client name exception
     */
    public static class InvalidClientNameException extends Exception {
        public InvalidClientNameException(String message) {
            super(message);
        }
    }

    /**
     * Get the chat client by the client name
     *
     * @param clientName the client name
     * @return the chat client instance
     */
    @Nonnull
    public static ChatClient getChatClient(String clientName) throws InvalidClientNameException {
        try {
            for (Class<? extends ChatClient> subType : getSubClasses()) {
                String modelName = subType.getField("clientName").get(null).toString();
                if (modelName.equals(clientName)) {
                    System.out.println("Creating " + clientName + " client...");
                    return subType.getDeclaredConstructor().newInstance();
                }
            }
        } catch (ReflectiveOperationException e) {
            Utils.printlnError(e.getMessage());
        }
        throw new InvalidClientNameException("Invalid client name: " + clientName);
    }

    /**
     * List all the previously stored sessions
     */
    public static void listSessions() {
        try (Stream<Path> paths = Files.walk(Paths.get("sessions"))) {
            paths.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .forEach(System.out::println);
        } catch (IOException e) {
            Utils.printlnError("Error listing sessions: " + e.getMessage());
        }
    }

    /**
     * Top-level Read-Eval-Print Loop
     */
    public static void repl() {
        Utils.printlnInfo(banner + "Welcome to LLM ChatManager!");
        printHelp();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        while (true) {
            try {
                Utils.printInfo(replPrompt);
                String line = reader.readLine();
                switch (line) {
                    case "chat":
                        System.out.println("Available LLM Chat Clients: " + getChatClientNames());
                        String clientName = reader.readLine("select your LLM client: ");
                        try {
                            ChatClient chatClient = getChatClient(clientName);
                            chatClient.repl();
                            System.out.println("Session " + chatClient.getClientUID() + " ended");
                            chatClient.saveClient();
                        } catch (InvalidClientNameException e) {
                            Utils.printlnError(e.getMessage());
                        }
                        break;
                    case "list":
                        listSessions();
                        break;
                    case "load":
                        Utils.printlnError("Not implemented yet");
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        throw new EndOfFileException();
                    case "":
                        break;
                    default:
                        Utils.printlnError("Invalid command");
                }
            } catch (UserInterruptException | EndOfFileException e) {
                return;
            }
        }
    }
}
