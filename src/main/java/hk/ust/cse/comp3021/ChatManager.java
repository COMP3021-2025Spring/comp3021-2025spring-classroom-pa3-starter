/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import hk.ust.cse.comp3021.client.GPT4oClient;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.util.Scanner;
import java.util.Set;

/**
 * ChatManager class
 */
public class ChatManager {
    /**
     * The shell prompt
     */
    private static final String shellPrompt = "ChatManager> ";

    /**
     * Print the help message
     */
    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("1. chat: start a chat session");
        System.out.println("2. help: show this help message");
        System.out.println("3. exit: exit the program");
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
            sb.append(System.lineSeparator());
        } catch (ReflectiveOperationException e) {
            System.err.println(e.getMessage());
        }
        return sb.toString();
    }

    /**
     * Get the chat client by the client name
     *
     * @param clientName the client name
     * @return the chat client instance
     */
    @Nonnull
    public static ChatClient getChatClient(String clientName) {
        try {
            for (Class<? extends ChatClient> subType : getSubClasses()) {
                String modelName = subType.getField("modelName").get(null).toString();
                if (modelName.equals(clientName)) {
                    System.out.println("Creating " + clientName + " client");
                    return subType.getDeclaredConstructor().newInstance();
                }
            }
        } catch (ReflectiveOperationException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("Wrong client name, use gpt-4o as default");
        return new GPT4oClient();
    }

    /**
     * Top-level Read-Eval-Print Loop
     */
    public static void repl() {
        System.out.println("Welcome to LLM ChatManager!");
        printHelp();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(shellPrompt);
            switch (scanner.next()) {
                case "1":
                case "chat":
                    System.out.print("Available LLM Chat Clients: " + getChatClients());
                    System.out.print(shellPrompt + "Select your LLM client: ");
                    ChatClient chatClient = getChatClient(scanner.next());
                    chatClient.repl();
                    System.out.println("Chat session ended!");
                    break;
                case "2":
                case "help":
                    printHelp();
                    break;
                case "3":
                case "exit":
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }
}
