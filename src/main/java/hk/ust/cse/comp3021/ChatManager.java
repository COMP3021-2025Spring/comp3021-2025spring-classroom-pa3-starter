/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.json.JSONObject;
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
     * The banner, generated from:
     * <a href="https://patorjk.com/software/taag/#p=display&f=Ogre&t=LLM%20ChatManager">...</a>
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
            put("tag", "tag the specified session");
            put("untag", "untag the specified session");
            put("description", "set a description to the specified session");
            put("list", "list available chat clients");
            put("history", "show previous sessions");
            put("load", "load from a previous session");
            put("help", "show this help message");
            put("exit", "exit the program");
        }
    };

    /**
     * Initialize the terminal for jline
     */
    static final Terminal terminal;

    // disable the logger and initialize the terminal
    static {
        Logger.getLogger("org.jline").setLevel(Level.OFF);
        try {
            terminal = TerminalBuilder.builder().jansi(true).system(true).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // create the session directory if not exists
    static {
        try {
            Path sessions = Path.of("sessions");
            if (!Files.exists(sessions)) {
                Files.createDirectory(sessions);
            }
        } catch (IOException e) {
            Utils.printlnError("Failed to create the session directory: " + e.getMessage());
        }
    }

    /**
     * The completer for the ChatManager repl
     */
    static final Completer completer = new StringsCompleter(menus.keySet());

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
     * Print the session information
     *
     * @param filePath the file path of the session
     */
    static void printSession(Path filePath) {
        try {
            JSONObject session = new JSONObject(Files.readString(filePath));
            String clientUID = filePath.getFileName().getFileName().toString().replace(".json", "");
            String tags = session.getJSONArray("tags").join(", ");
            String description = session.getString("description");
            System.out.printf("Session: %-40s Tags: %-40s Description: %s %n", clientUID, tags, description);
        } catch (IOException e) {
            Utils.printlnError("Error reading session: " + e.getMessage());
        }
    }

    /**
     * Add tags to the session
     *
     * @param clientUID the client UID
     * @param tags       the tags to add
     */
    static void addTags(String clientUID, String[] tags) {
        JSONObject session = Utils.parseJSON(clientUID);
        for (String tag : tags) {
            session.getJSONArray("tags").put(tag.trim());
        }
        Utils.writeJSON(session, clientUID);
    }

    /**
     * Remove a tag from the session
     * @param clientUID the client UID
     * @param tag the tag to remove
     */
    static void removeTag(String clientUID, String tag) {
        JSONObject session = Utils.parseJSON(clientUID);
        for (int i = 0; i < session.getJSONArray("tags").length(); i++) {
            if (session.getJSONArray("tags").getString(i).equals(tag)) {
                session.getJSONArray("tags").remove(i);
                break;
            }
        }
        Utils.writeJSON(session, clientUID);
    }

    /**
     * Add a description to the session
     * @param clientUID the client UID
     * @param description the description to add
     */
    static void setDescription(String clientUID, String description) {
        Path filePath = Paths.get("sessions", clientUID + ".json");
        JSONObject session = Utils.parseJSON(filePath.toString());
        session.put("description", description.trim());
        Utils.writeJSON(session, filePath.toString());
    }

    /**
     * List all the previously stored sessions
     */
    public static void listSessions() {
        try (Stream<Path> paths = Files.walk(Paths.get("sessions"))) {
            paths.filter(Files::isRegularFile).forEach(ChatManager::printSession);
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
        LineReader lineReader = LineReaderBuilder.builder().completer(completer).terminal(terminal).build();

        while (true) {
            try {
                Utils.printInfo(replPrompt);
                String[] tokens = lineReader.readLine().split(" ");
                String command = tokens[0];
                String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
                switch (command) {
                    case "list":
                        System.out.println("Available LLM Chat Clients: " + getChatClientNames());
                        break;
                    case "chat":
                        // default client name is GPT-4o for testing
                        String clientName = args.length == 0 ? "GPT-4o" : args[0];
                        try {
                            ChatClient chatClient = getChatClient(clientName);
                            chatClient.repl();
                            System.out.println("Session " + chatClient.getClientUID() + " ended");
                            chatClient.saveClient();
                        } catch (InvalidClientNameException e) {
                            Utils.printlnError(e.getMessage());
                        }
                        break;
                    case "tag":
                        if (args.length < 2) {
                            Utils.printlnError("Usage: tag [session] [tag1] [tag2] ...");
                            break;
                        }
                        addTags(args[0], Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "untag":
                        if (args.length != 2) {
                            Utils.printlnError("Usage: untag [session] [tag]");
                            break;
                        }
                        removeTag(args[0], args[1]);
                        break;
                    case "description":
                        if (args.length < 2) {
                            Utils.printlnError("Usage: description [session] [description]");
                            break;
                        }
                        setDescription(args[0], String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                        break;
                    case "history":
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
