/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Abstract class for ChatClient
 */
public abstract class ChatClient {
    /**
     * The system prompt
     */
    protected String systemPrompt = "You are a helpful assistant.";

    /**
     * The shell prompt for the ChatClient repl
     */
    protected String shellPrompt = "ChatClient> ";

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

    protected abstract int getClientMaxTokens();

    /**
     * The menu, a map of command and description
     */
    private static final Map<String, String> menus = new LinkedHashMap<>() {
        {
            put("file", "upload a file");
            put("history", "show the conversation history");
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
        Utils.printlnInfo("Welcome to " + getClientName() + " ChatClient!");
        printHelp();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Utils.printInfo(shellPrompt);
            String input = scanner.nextLine();
            switch (input) {
                case "file":
                    System.out.print("Specify the file path: ");
                    String filePath = scanner.nextLine();
                    try {
                        String content = Files.readString(Path.of(filePath)).trim();
                        if (content.length() >= getClientMaxTokens()) {
                            Utils.printlnError("The file content is too long, we only support up to " + getClientMaxTokens() + " tokens.");
                            break;
                        }
                        System.out.println(query(content));
                    } catch (IOException e) {
                        Utils.printlnError(e.getMessage());
                    }
                    break;
                case "history":
                    System.out.println(messages);
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("Session " + getClientUID() + " ended");
                    saveClient();
                    return;
                default:
                    Utils.printInfo(getClientName() + "> ");
                    System.out.println(query(input));
            }
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

        /**
         * Convert the messages to JSON format used in POST request
         *
         * @return the JSON string
         */
        public JSONObject toJSON() {
            JSONObject postData = new JSONObject();
            JSONArray messageList = new JSONArray();
            for (Message message : this.messageList) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("role", message.role);
                messageJson.put("content", message.content);
                messageList.put(messageJson);
            }
            postData.put("messages", messageList);
            return postData;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Message message : messageList) {
                sb.append(message.role).append(": ").append(message.content).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Get the client UID for indexing
     *
     * @return the client UID
     */
    String getClientUID() {
        return getClientName() + "-" + timeCreated;
    }

    /**
     * Save the client to a JSON file
     */
    void saveClient() {
        JSONObject clientJson = toJSON();
        String clientUID = getClientUID();
        String clientFileName = String.format("sessions/%s.json", clientUID);
        try {
            Files.writeString(Path.of(clientFileName), clientJson.toString());
            System.out.println("Session saved to " + clientFileName);
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
     * Serialize the ChatClient instance to JSON
     *
     * @return the JSON object
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("clientName", getClientName());
        json.put("apiKey", apiKey);
        json.put("timeCreated", timeCreated);
        return json;
    }

    /**
     * Deserialize the ChatClient instance from JSON
     *
     * @param json the JSON object
     * @return the ChatClient instance
     */
    abstract public ChatClient fromJSON(JSONObject json);
}
