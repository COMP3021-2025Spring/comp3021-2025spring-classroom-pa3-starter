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
    protected String replPrompt = "ChatClient> ";

    /**
     * The API key
     */
    protected String apiKey;

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
    boolean readAndSetKey(String apiKeyFile) {
        try {
            String apiKey = Files.readString(Path.of(apiKeyFile)).trim();
            setApiKey(apiKey);
            return true;
        } catch (IOException e) {
            Utils.printlnError("Failed to read the API key: " + e.getMessage());
            return false;
        }
    }

    /**
     * The time created
     */
    protected long timeCreated;

    /**
     * The time last opened
     */
    protected long timeLastOpen;

    /**
     * The time last exit
     */
    protected long timeLastExit;

    /**
     * Message class, consisting of role and content
     */
    protected static class Message {
        String role;
        String content;
        int tokens;

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

        public Message(String role, String content, int tokens) {
            this.role = role;
            this.content = content;
            this.tokens = tokens;
        }

        public void setTokens(int tokens) {
            this.tokens = tokens;
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

        public void addMessage(String role, String content, int tokens) {
            messageList.add(new Message(role, content, tokens));
        }

        public Message getLastMessage() {
            return messageList.get(messageList.size() - 1);
        }

        /**
         * Convert the messages to JSON format used in POST request
         *
         * @return the JSON string
         */
        public JSONArray toPOSTData() {
            JSONArray messageList = new JSONArray();
            for (Message message : this.messageList) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("role", message.role);
                messageJson.put("content", message.content);
                messageList.put(messageJson);
            }
            return messageList;
        }

        /**
         * Convert the messages to JSON format used in persistence
         *
         * @return the JSON string
         */
        public JSONArray toJSON() {
            JSONArray messageList = new JSONArray();
            for (Message message : this.messageList) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("role", message.role);
                messageJson.put("content", message.content);
                messageJson.put("tokens", message.tokens);
                messageList.put(messageJson);
            }
            return messageList;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Message message : messageList) {
                String prompt = message.role.equals("user") ? " --> " : " <-- ";
                sb.append(Utils.toInfo(message.role)).append(Utils.toInfo(prompt)).append(message.content).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * The total prompt tokens queried by the ChatClient
     */
    protected int totalPromptTokens;

    /**
     * The total completion tokens queried by the ChatClient
     */
    protected int totalCompletionTokens;

    /**
     * The temperature of the ChatClient
     */
    protected int temperature = 1;

    /**
     * The messages to save all conversation history
     */
    protected Messages messages = new Messages();

    /**
     * The tags for filtering ChatClient
     */
    protected HashSet<String> tags = new HashSet<>();

    /**
     * The description of the ChatClient
     */
    protected String description = "";

    /**
     * Add a tag to the ChatClient
     *
     * @param tags the tag to add
     */
    public void addTags(String[] tags) {
        this.tags.addAll(List.of(tags));
    }

    /**
     * Remove a tag from the ChatClient
     *
     * @param tag the tag to remove
     */
    public void removeTag(String tag) {
        tags.remove(tag);
    }

    /**
     * Get the description of the ChatClient
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the name of the ChatClient
     *
     * @return the name of the ChatClient
     */
    protected abstract String getClientName();

    /**
     * Get the maximum tokens allowed for the ChatClient
     *
     * @return the maximum tokens allowed
     */
    protected abstract int getClientMaxTokens();

    /**
     * The menu, a map of command and description
     */
    static final Map<String, String> menus = new LinkedHashMap<>() {
        {
            put("file", "upload a file");
            put("history", "show the conversation history");
            put("tag", "tag current session");
            put("untag", "untag current session");
            put("desc", "set a description to current session");
            put("help", "show this help message");
            put("exit", "exit the program");
        }
    };

    /**
     * Print the help message
     */
    static void printHelp() {
        System.out.println("Available commands:");
        for (Map.Entry<String, String> entry : menus.entrySet()) {
            System.out.print("- ");
            Utils.printInfo(entry.getKey());
            System.out.println(": " + entry.getValue());
        }
    }

    /**
     * Default constructor of ChatClient
     */
    public ChatClient() {
        // set the time created and last open
        timeCreated = Utils.getCurrentTime();
        timeLastOpen = timeCreated;

        String apiKeyFile = String.format("keys/%s.txt", getClientName());
        if (Files.exists(Path.of(apiKeyFile)) && readAndSetKey(apiKeyFile)) {
            System.out.println("Default API key loaded from: " + apiKeyFile);
        } else {
            while (true) {
                System.out.println("Specify the file path of the API key: ");
                apiKeyFile = new Scanner(System.in).next();
                if (readAndSetKey(apiKeyFile)) {
                    System.out.println("API key loaded from: " + apiKeyFile);
                    break;
                }
            }
        }

        // manually get the token number of system prompt
        messages.addMessage("system", systemPrompt);
    }

    /**
     * Constructor of ChatClient when deserializing from JSON
     *
     * @param session the JSON object
     */
    public ChatClient(JSONObject session) {
        apiKey = session.getString("apiKey");
        temperature = session.getInt("temperature");
        totalPromptTokens = session.getInt("totalPromptTokens");
        totalCompletionTokens = session.getInt("totalCompletionTokens");
        session.getJSONArray("tags").forEach(tag -> tags.add(tag.toString()));
        tags = new HashSet<>(session.getJSONArray("tags").toList().stream().map(Object::toString).toList());
        description = session.getString("description");
        timeCreated = session.getInt("timeCreated");
        timeLastExit = session.getInt("timeLastExit");
        timeLastOpen = Utils.getCurrentTime();
        messages = new Messages();
        JSONArray messagesJson = session.getJSONArray("messages");
        for (int i = 0; i < messagesJson.length(); i++) {
            JSONObject messageJson = messagesJson.getJSONObject(i);
            messages.addMessage(messageJson.getString("role"), messageJson.getString("content"), messageJson.getInt(
                    "tokens"));
        }
    }

    /**
     * Get the POST data for the ChatClient
     *
     * @return the POST data in JSONObject
     */
    protected JSONObject getPOSTData() {
        JSONObject postData = new JSONObject();
        postData.put("temperature", temperature);
        postData.put("messages", messages.toPOSTData());
        return postData;
    }

    /**
     * upload a file and query the content
     *
     * @param filePath the file path
     */
    void uploadFile(String filePath) {
        try {
            String content = Files.readString(Path.of(filePath)).trim();
            if (content.length() >= getClientMaxTokens()) {
                Utils.printlnError("The file content has exceed maximum (" + getClientMaxTokens() + ") tokens.");
                return;
            }
            System.out.println(query(content));
        } catch (IOException e) {
            Utils.printlnError(e.getMessage());
        }
    }

    /**
     * Chatting Read-Eval-Print Loop
     */
    public void repl() {
        Utils.printlnInfo("Welcome to " + getClientName() + " ChatClient!");
        printHelp();
        LineReader lineReader = LineReaderBuilder.builder().build();
        while (true) {
            try {
                Utils.printInfo(replPrompt);
                String line = lineReader.readLine();
                String[] tokens = line.split(" ");
                if (tokens.length == 0) {
                    continue;
                }
                String command = tokens[0];
                String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
                switch (command) {
                    case "file":
                        String filePath = (args.length == 0) ? lineReader.readLine("specify the file path: ") : args[0];
                        uploadFile(filePath);
                        break;
                    case "history":
                        System.out.println(messages);
                        break;
                    case "tag":
                        addTags(args);
                        break;
                    case "untag":
                        if (args.length != 1) {
                            Utils.printlnError("Usage: tag [tag]");
                            break;
                        }
                        removeTag(args[0]);
                        break;
                    case "desc":
                        if (args.length == 0) {
                            Utils.printlnError("Usage: description [description]");
                            break;
                        }
                        setDescription(String.join(" ", args));
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        throw new EndOfFileException();
                    case "":
                        break;
                    default:
                        Utils.printInfo(getClientName() + "> ");
                        System.out.println(query(line));
                }
            } catch (UserInterruptException | EndOfFileException e) {
                return;
            }
        }
    }


    /**
     * Get the client UID for indexing
     *
     * @return the client UID
     */
    String getClientUID() {
        return getClientName() + "_" + Utils.timeToFilename(timeCreated);
    }

    /**
     * Save the client to a JSON file
     */
    void saveClient() {
        JSONObject clientJson = toJSON();
        Utils.writeJSON(clientJson, getClientUID());
    }

    /**
     * @param prompt the prompt to send to the LLM model
     * @return the response from the LLM model
     */
    public abstract String query(String prompt);

    /**
     * Serialize the ChatClient instance to JSON
     *
     * @return the JSON object
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("clientName", getClientName());
        json.put("apiKey", apiKey);
        json.put("temperature", temperature);
        json.put("timeCreated", timeCreated);
        json.put("timeLastOpen", timeLastOpen);
        json.put("timeLastExit", Utils.getCurrentTime());
        json.put("tags", new JSONArray(tags));
        json.put("description", description);
        json.put("totalPromptTokens", totalPromptTokens);
        json.put("totalCompletionTokens", totalCompletionTokens);
        json.put("messages", messages.toJSON());
        return json;
    }
}
