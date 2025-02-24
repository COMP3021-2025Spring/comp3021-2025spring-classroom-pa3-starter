/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * ChatManager class
 */
public class ChatManager {
    /**
     * The shell prompt for the ChatManager repl
     */
    private static final String shellPrompt = "ChatManager> ";

    /**
     * The banner, generated from: <a href="https://patorjk.com/software/taag/#p=display&f=Ogre&t=LLM%20ChatManager">...</a>
     */
    private static final String banner = """
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
    private static final Map<String, String> menus = new LinkedHashMap<>() {
        {
            put("chat", "start a new chat session");
            put("list", "list previous sessions");
            put("load", "load from a previous session");
            put("help", "show this help message");
            put("exit", "exit the program");
        }
    };

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
    public static String getChatClients() {
        StringBuilder sb = new StringBuilder();
        try {
            for (Class<? extends ChatClient> subType : getSubClasses()) {
                String modelName = subType.getField("clientName").get(null).toString();
                sb.append(modelName).append(" ");
            }
        } catch (ReflectiveOperationException e) {
            Utils.printlnError(e.getMessage());
        }
        return sb.toString();
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
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Utils.printInfo(shellPrompt);
            switch (scanner.nextLine()) {
                case "chat":
                    System.out.println("Available LLM Chat Clients: " + getChatClients() + ", select your LLM client: ");
                    Utils.printInfo(shellPrompt);
                    try {
                        ChatClient chatClient = getChatClient(scanner.next());
                        chatClient.repl();
                    } catch (InvalidClientNameException e) {
                        Utils.printlnError(e.getMessage());
                    }
                    scanner.nextLine();
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
                    return;
                    // ignore empty lines
                case "":
                    break;
                default:
                    Utils.printlnError("Invalid command");
            }
        }
    }
}
