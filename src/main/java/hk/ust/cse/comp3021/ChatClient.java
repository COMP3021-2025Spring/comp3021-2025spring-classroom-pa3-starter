/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Abstract class for ChatClient
 */
public abstract class ChatClient {
    /**
     * The system prompt
     */
    protected String systemPrompt = "You are a helpful assistant.";

    /**
     * The API key
     */
    protected String apiKey;

    /**
     * The time created
     */
    protected final String timeCreated;

    /**
     * The messages to save all conversation history
     */
    protected Messages messages = new Messages();

    /**
     * Get the name of the ChatClient
     *
     * @return the name of the ChatClient
     */
    protected abstract String getClientName();

    /**
     * Set the API key
     *
     * @param apiKey the API key
     */
    protected void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Read the API key from the file and set it
     *
     * @param apiKeyFile the file path of the API key
     */
    void readAndSetKey(String apiKeyFile) {
        try {
            String apiKey = Files.readString(Path.of(apiKeyFile));
            setApiKey(apiKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default constructor of ChatClient
     */
    public ChatClient() {
        timeCreated = Utils.getCurrentTime();
        String apiKeyFile = String.format("keys/%s.txt", getClientName());
        if (Files.exists(Path.of(apiKeyFile))) {
            System.out.println("Default API key loaded from: " + apiKeyFile);
            readAndSetKey(String.format("keys/%s.txt", getClientName()));
            return;
        }

        System.out.println("Specify the file path of the API key: ");
        try {
            apiKeyFile = new Scanner(System.in).next();
            readAndSetKey(apiKeyFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        messages.addMessage("system", systemPrompt);
    }

    /**
     * Chatting Read-Eval-Print Loop
     */
    public void repl() {
        Utils.printGreen("Welcome to " + getClientName() + " ChatClient!");
        System.out.println(" (type 'exit' to exit)");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Utils.printGreen(getClientName() + "> ");
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                System.out.println("Session " + getClientUID() + " ended");
                break;
            }
            // start chat
            System.out.println(query(input));
        }
    }

    /**
     * Message class, consisting of role and content
     */
    protected static class Message {
        String role;
        String content;

        /**
         * Constructor of Message
         *
         * @param role    role in LLM: system, user, assistant
         * @param content content of the message
         */
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * Messages consists of a list of {@link Message}
     */
    protected static class Messages {
        /**
         * List of {@link Message}
         */
        List<Message> messageList = new ArrayList<>();

        /**
         * Add a {@link Message} to the list
         *
         * @param role    role in LLM: system, user, assistant
         * @param content content of the message
         */
        public void addMessage(String role, String content) {
            messageList.add(new Message(role, content));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Message message : messageList) {
                sb.append(String.format("{\"role\": \"%s\", \"content\": \"%s\"}", message.role, message.content));
                if (messageList.indexOf(message) != messageList.size() - 1) {
                    sb.append(", ");
                }
            }
            return String.format("{\"messages\": [%s]}", sb);
        }
    }

    /**
     * Get the client UID for indexing
     * @return the client UID
     */
    String getClientUID() {
        return getClientName() + "-" + timeCreated;
    }

    /**
     * Save the client to a JSON file
     */
    void saveClient() {
        JSONObject clientJson = toJson();
        String clientUID = getClientUID();
        String clientFileName = String.format("clients/%s.json", clientUID);
        try {
            Files.writeString(Path.of(clientFileName), clientJson.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param prompt the prompt to send to the LLM model
     * @return the response from the LLM model
     */
    abstract public String query(String prompt);

    /**
     * Convert the ChatClient instance to JSON
     * @return the JSON object
     */
    abstract public JSONObject toJson();
}
