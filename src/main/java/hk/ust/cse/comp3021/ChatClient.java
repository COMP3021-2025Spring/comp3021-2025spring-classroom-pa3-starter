/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import hk.ust.cse.comp3021.annotation.*;
import hk.ust.cse.comp3021.client.GPT4oClient;
import hk.ust.cse.comp3021.exception.*;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Abstract class for ChatClient
 */
public abstract class ChatClient implements Serializable {
    /**
     * The system prompt
     * TODO: annotate as ignore for the systemPrompt
     */
    protected String systemPrompt = "You are a helpful assistant.";

    /**
     * The shell prompt for the ChatClient repl
     * TODO: annotate as ignore for the replPrompt
     */
    protected String replPrompt = "ChatClient> ";

    /**
     * The API key
     * TODO: annotate as secret for the apiKey
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
     * The time created, <a href="https://www.epochconverter.com/">...</a>
     * TODO: annotate a range check for the timeCreated, between March 1, 2025 and June 1, 2025
     */
    protected long timeCreated;

    /**
     * The time last opened, <a href="https://www.epochconverter.com/">...</a>
     * TODO: annotate a range check for the timeLastOpen, between March 1, 2025 and June 1, 2025
     */
    protected long timeLastOpen;

    /**
     * The time last exit, <a href="https://www.epochconverter.com/">...</a>
     * TODO: annotate a range check for the timeLastExit, between March 1, 2025 and June 1, 2025
     */
    protected long timeLastExit;

    /**
     * The total prompt tokens queried by the ChatClient
     * TODO: annotate a range check for the totalPromptTokens, above 0
     */
    protected int totalPromptTokens;

    /**
     * The total completion tokens queried by the ChatClient
     * TODO: annotate a range check for the totalCompletionTokens, above 0
     */
    protected int totalCompletionTokens;

    /**
     * The temperature of the ChatClient
     * TODO: annotate a range check for the temperature, between 0 and 2.0
     */
    protected double temperature = 1;

    /**
     * The messages to save all conversation history
     */
    protected Messages messages = new Messages();

    /**
     * The tags for filtering ChatClient
     * TODO: annotate a filter for the tags
     */
    protected HashSet<String> tags = new HashSet<>();

    /**
     * The description of the ChatClient
     * TODO: annotate a filter for the description
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
     * TODO: annotate as ignore for the menus
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
    public ChatClient(JSONObject session) throws PersistenceException {
        fromJSON(session);
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
                String[] tokens = line.split("\\s+");
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
        // TODO: implement the client saving, remember to update timeLastExit
    }

    /**
     * @param prompt the prompt to send to the LLM model
     * @return the response from the LLM model
     */
    public abstract String query(String prompt);

    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    /**
     * Serialize the ChatClient instance to JSON, guided by the annotations
     *
     * @return the JSON object
     */
    @Override
    public JSONObject toJSON() {
        // TODO: implement the serialization, use reflection to check the annotation and type of each field to be
        //  serialized, then perform corresponding serialization actions
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void fromJSON(JSONObject jsonObject) throws PersistenceException {
        // TODO: implement the deserialization, use reflection to check the annotation and type of each field to be
        //  deserialized and find their corresponding JSON key, then perform corresponding deserialization actions.
        //  Throw PersistenceException if any error occurs.
        throw new PersistenceException("Not implemented yet");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatClient that = (ChatClient) o;
        for (Field field : ChatClient.getAllFields(GPT4oClient.class)) {
            field.setAccessible(true);
            try {
                if (!field.get(this).equals(field.get(that))) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                Utils.printlnError(e.getMessage());
            }
        }
        return true;
    }
}
