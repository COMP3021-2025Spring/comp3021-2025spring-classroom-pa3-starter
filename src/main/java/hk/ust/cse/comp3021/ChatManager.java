/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * ChatManager class
 */
public class ChatManager {
    /**
     * The shell prompt
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
            put("chat", "start a chat session");
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
            Utils.printGreen(entry.getKey());
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
                String modelName = subType.getField("modelName").get(null).toString();
                sb.append(modelName).append(" ");
            }
        } catch (ReflectiveOperationException e) {
            Utils.printlnRed(e.getMessage());
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
                String modelName = subType.getField("modelName").get(null).toString();
                if (modelName.equals(clientName)) {
                    System.out.println("Creating " + clientName + " client");
                    return subType.getDeclaredConstructor().newInstance();
                }
            }
        } catch (ReflectiveOperationException e) {
            Utils.printlnRed(e.getMessage());
        }
        throw new InvalidClientNameException("Invalid client name: " + clientName);
    }

    /**
     * Top-level Read-Eval-Print Loop
     */
    public static void repl() {
        Utils.printlnGreen(banner + "Welcome to LLM ChatManager!");
        printHelp();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Utils.printGreen(shellPrompt);
            switch (scanner.next()) {
                case "chat":
                    System.out.println("Available LLM Chat Clients: " + getChatClients() + ", select your LLM client: ");
                    Utils.printGreen(shellPrompt);
                    try {
                        ChatClient chatClient = getChatClient(scanner.next());
                        chatClient.repl();
                        System.out.println("Chat session ended!");
                    } catch (InvalidClientNameException e) {
                        Utils.printlnRed(e.getMessage());
                    }
                    break;
                case "load":
                    Utils.printlnRed("Not implemented yet");
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    return;
                default:
                    Utils.printlnRed("Invalid command");
            }
        }
    }
}
