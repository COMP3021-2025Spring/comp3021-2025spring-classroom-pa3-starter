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
     */
    @JsonIgnore
    protected String systemPrompt = "You are a helpful assistant.";

    /**
     * The shell prompt for the ChatClient repl
     */
    @JsonIgnore
    protected String replPrompt = "ChatClient> ";

    /**
     * The API key
     */
    @JsonSecret
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
     */
    @JsonRangeCheck(minLong = 1740787200, maxLong = 1748735999)
    protected long timeCreated;

    /**
     * The time last opened, <a href="https://www.epochconverter.com/">...</a>
     */
    @JsonRangeCheck(minLong = 1740787200, maxLong = 1748735999)
    protected long timeLastOpen;

    /**
     * The time last exit, <a href="https://www.epochconverter.com/">...</a>
     */
    @JsonRangeCheck(minLong = 1740787200, maxLong = 1748735999)
    protected long timeLastExit;

    /**
     * The total prompt tokens queried by the ChatClient
     */
    @JsonRangeCheck(minInt = 0)
    protected int totalPromptTokens;

    /**
     * The total completion tokens queried by the ChatClient
     */
    @JsonRangeCheck(minInt = 0)
    protected int totalCompletionTokens;

    /**
     * The temperature of the ChatClient
     */
    @JsonRangeCheck(minDouble = 0, maxDouble = 2)
    protected double temperature = 1;

    /**
     * The messages to save all conversation history
     */
    protected Messages messages = new Messages();

    /**
     * The tags for filtering ChatClient
     */
    @JsonFilter
    protected HashSet<String> tags = new HashSet<>();

    /**
     * The description of the ChatClient
     */
    @JsonFilter
    protected String description = "";

    /**
     * The sessionUID of the ChatClient
     */
    @JsonIgnore
    protected String sessionUID;

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
    @JsonIgnore
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
        // generate a sessionUID for new client
        sessionUID = Utils.generateUID();

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
     * @throws PersistenceException if the JSON object is invalid when checking the annotations
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
     * Save the client instance to a session in JSON format
     */
    void saveClient(String user) {
        timeLastExit = Utils.getCurrentTime();
        JSONObject session = toJSON();
        SessionManager.setSession(user, sessionUID, session);
    }

    /**
     * @param prompt the prompt to send to the LLM model
     * @return the response from the LLM model
     */
    public abstract String query(String prompt);

    /**
     * Get all fields of the class and its superclasses
     *
     * @param clazz the class
     * @return the array of fields
     */
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
        JSONObject jsonObject = new JSONObject();
        // iterate though all fields of the derived ChatClient class
        Field[] allFields = getAllFields(this.getClass());
        for (Field field : allFields) {
            field.setAccessible(true);
            try {
                // parse annotations and perform their actions
                Object fieldValue = field.get(this);
                if (field.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }
                if (field.isAnnotationPresent(JsonSecret.class)) {
                    JsonSecret secret = field.getAnnotation(JsonSecret.class);
                    String key = secret.key();
                    fieldValue = Utils.encrypt(fieldValue.toString(), key);
                }
                if (field.isAnnotationPresent(JsonFilter.class)) {
                    if (fieldValue instanceof String fieldString) {
                        for (String kw : field.getAnnotation(JsonFilter.class).kwList()) {
                            fieldString = fieldString.replaceAll(kw, "*".repeat(kw.length()));
                        }
                        fieldValue = fieldString;
                    } else if (fieldValue instanceof Collection<?> fieldCollection) {
                        for (String kw : field.getAnnotation(JsonFilter.class).kwList()) {
                            fieldCollection.remove(kw);
                        }
                    }
                }
                // JsonRangeCheck and JsonCheck is ignored when serializing

                // start serializing the field
                // if the field is of org.json supported type: int, long, double, String, Collection, etc
                if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
                    jsonObject.put(field.getName(), fieldValue);
                } else if (field.get(this) instanceof Collection<?>) {
                    jsonObject.put(field.getName(), new JSONArray((Collection<?>) fieldValue));
                } else if (fieldValue instanceof Serializable fieldSerializable) {
                    // if the field is self-defined class, it must implement Serializable
                    JSONObject fieldJson = fieldSerializable.toJSON();
                    jsonObject.put(field.getName(), fieldJson);
                } else {
                    Utils.printlnError("Failed to serialize the field: " + field.getName());
                }
            } catch (IllegalAccessException e) {
                Utils.printlnError("Failed to serialize the field: " + field.getName() + e.getMessage());
            }
        }
        return jsonObject;
    }

    @Override
    public void fromJSON(JSONObject jsonObject) throws PersistenceException {
        // iterate though all fields of the derived ChatClient class
        Field[] allFields = getAllFields(this.getClass());
        for (Field field : allFields) {
            field.setAccessible(true);
            try {
                // parse annotations and perform their actions
                if (field.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }
                String fieldName = field.getName();
                Object fieldValue = switch (field.getType().getName()) {
                    case "int" -> jsonObject.getInt(fieldName);
                    case "long" -> jsonObject.getLong(fieldName);
                    case "double" -> jsonObject.getDouble(fieldName);
                    case "java.lang.String" -> jsonObject.getString(fieldName);
                    default -> jsonObject.get(fieldName);
                };
                if (field.isAnnotationPresent(JsonSecret.class)) {
                    JsonSecret secret = field.getAnnotation(JsonSecret.class);
                    fieldValue = Utils.decrypt(fieldValue.toString(), secret.key());
                    if (!Utils.isValidApiKey(fieldValue.toString())) {
                        Utils.printlnError("The field " + field.getName() + " is not a valid API key.");
                        throw new JsonSecretException("Field value: " + fieldValue);
                    }
                }
                if (field.isAnnotationPresent(JsonRangeCheck.class)) {
                    if (field.getType().equals(int.class)) {
                        int fieldInt = (int) fieldValue;
                        if (fieldInt < field.getAnnotation(JsonRangeCheck.class).minInt()
                                || fieldInt > field.getAnnotation(JsonRangeCheck.class).maxInt()) {
                            Utils.printlnError("The field " + field.getName() + " is out of range.");
                            throw new JsonRangeCheckException("Field value: " + fieldInt);
                        }
                    } else if (field.getType().equals(long.class)) {
                        long fieldLong = (long) fieldValue;
                        if (fieldLong < field.getAnnotation(JsonRangeCheck.class).minLong()
                                || fieldLong > field.getAnnotation(JsonRangeCheck.class).maxLong()) {
                            Utils.printlnError("The field " + field.getName() + " is out of range.");
                            throw new JsonRangeCheckException("Field value: " + fieldLong);
                        }
                    } else if (field.getType().equals(double.class)) {
                        double fieldDouble = (double) fieldValue;
                        if (fieldDouble < field.getAnnotation(JsonRangeCheck.class).minDouble()
                                || fieldDouble > field.getAnnotation(JsonRangeCheck.class).maxDouble()) {
                            Utils.printlnError("The field " + field.getName() + " is out of range.");
                            throw new JsonRangeCheckException("Field value: " + fieldDouble);
                        }
                    } else {
                        Utils.printlnError("Failed to deserialize the field: " + field.getName());
                    }
                }
                if (field.isAnnotationPresent(JsonCheck.class)) {
                    if (!fieldValue.equals(field.get(this))) {
                        Utils.printlnError("The field " + field.getName() + " is not allowed to be changed.");
                        throw new JsonCheckException(fieldName);
                    }
                }
                if (field.isAnnotationPresent(JsonFilter.class)) {
                    if (fieldValue instanceof String fieldString) {
                        for (String kw : field.getAnnotation(JsonFilter.class).kwList()) {
                            if (fieldString.contains(kw)) {
                                Utils.printlnError("The field " + field.getName() + " contains prohibited information.");
                                throw new JsonFilterException(fieldName);
                            }
                        }
                    } else if (fieldValue instanceof JSONArray fieldArray) {
                        for (String kw : field.getAnnotation(JsonFilter.class).kwList()) {
                            if (fieldArray.toList().contains(kw)) {
                                Utils.printlnError("The field " + field.getName() + " contains prohibited information.");
                                throw new JsonFilterException(fieldName);
                            }
                        }
                    }
                }

                // start deserializing back to field
                if (Modifier.isFinal(field.getModifiers())) {
                    // ignore final fields
                    continue;
                } else if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
                    // if the field is of org.json supported type: int, long, double, String, Collection, etc
                    field.set(this, fieldValue);
                } else if (fieldValue instanceof JSONArray fieldJsonArray) {
                    // if the field is of Collection type, which usually deserialized from JSONArray
                    if (field.get(this) instanceof HashSet<?>) {
                        field.set(this, new HashSet<>(fieldJsonArray.toList()));
                    } else {
                        Utils.printlnError("Failed to deserialize the field: " + field.getName());
                    }
                } else if (fieldValue instanceof JSONObject fieldJsonObject && field.get(this) instanceof Serializable fieldSerializable) {
                    // if the field is self-defined class, it must implement Serializable, which usually deserialized
                    // from JSONObject
                    fieldSerializable.fromJSON(fieldJsonObject);
                } else {
                    Utils.printlnError("Failed to deserialize the field: " + field.getName());
                }
            } catch (IllegalAccessException e) {
                Utils.printlnError("Failed to deserialize the field: " + field.getName());
            }
        }
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

    @Override
    public int hashCode() {
        return Objects.hash(systemPrompt, replPrompt, apiKey, timeCreated, timeLastOpen, timeLastExit,
                totalPromptTokens, totalCompletionTokens, temperature, messages, tags, description, sessionUID);
    }
}
