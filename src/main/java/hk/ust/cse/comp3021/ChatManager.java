/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import hk.ust.cse.comp3021.exception.InvalidClientNameException;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.json.JSONObject;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * ChatManager class
 */
public class ChatManager {
    /**
     * The shell prompt for the ChatManager repl
     */
    static final String replPrompt = Utils.toInfo("ChatManager> ");

    /**
     * The shell prompt for the ChatManager admin repl
     */
    static final String adminPromt = Utils.toInfo("Admin> ");

    /**
     * The banner, generated using <a href="https://patorjk.com/software/taag/#p=display&f=Ogre&t=LLM%20ChatManager">ascii art</a>
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
            put("desc", "set a description to the specified session");
            put("show", "show available chat clients");
            put("list", "list previous sessions");
            put("load", "load from a previous session");
            put("whoami", "show the current user");
            put("profile", "generate a profile for the current user");
            put("help", "show this help message");
            put("exit", "exit the program");
        }
    };

    /**
     * The admin menu, a map of command and description
     */
    static final Map<String, String> adminMenus = new LinkedHashMap<>() {
        {
            put("profile", "show the system profile of the database");
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

    /**
     * The completer for the ChatManager repl
     */
    static final Completer completer = new StringsCompleter(menus.keySet());

    /**
     * Initialize the line reader for jline
     */
    static final LineReader lineReader = LineReaderBuilder.builder().completer(completer).terminal(terminal).build();

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
     * The current active ChatClient
     */
    static ChatClient chatClient;

    /**
     * Print the help message
     */
    private static void printHelp(Map<String, String> menus) {
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
     * Get names of all available chat clients
     *
     * @return the names of all available chat clients
     */
    public static String getChatClientNames() {
        List<String> clientNames = new ArrayList<>();
        try {
            for (Class<? extends ChatClient> subType : getSubClasses()) {
                String clientName = subType.getField("clientName").get(null).toString();
                if (clientName.contains("_"))
                    Utils.printlnError("Invalid client name: " + clientName + ", ignored");
                else
                    clientNames.add(clientName);
            }
        } catch (ReflectiveOperationException e) {
            Utils.printlnError(e.getMessage());
        }
        return String.join(" | ", clientNames);
    }

    /**
     * Create a chat client by the client name
     *
     * @param clientName the client name
     * @return the chat client instance
     */
    public static ChatClient getChatClient(String clientName) {
        try {
            for (Class<? extends ChatClient> subType : getSubClasses()) {
                String modelName = subType.getField("clientName").get(null).toString();
                if (modelName.equals(clientName)) {
                    System.out.println("Creating " + clientName + " client...");
                    return subType.getDeclaredConstructor().newInstance();
                }
            }
            throw new InvalidClientNameException("Invalid client name: " + clientName);
        } catch (ReflectiveOperationException | InvalidClientNameException e) {
            Utils.printlnError(e.getMessage());
            return null;
        }
    }

    /**
     * Restore a chat client by user and sessionUID
     *
     * @param user       the user
     * @param sessionUID the session UID
     * @return the chat client instance
     */
    public static ChatClient getChatClient(String user, String sessionUID) {
        JSONObject session = SessionManager.getSession(user, sessionUID);
        if (session == null) {
            return null;
        }
        String clientName = session.getString("clientName");
        try {
            for (Class<? extends ChatClient> subType : getSubClasses()) {
                String modelName = subType.getField("clientName").get(null).toString();
                if (modelName.equals(clientName)) {
                    System.out.println("Loading " + clientName + " client...");
                    ChatClient chatClient = subType.getDeclaredConstructor(JSONObject.class).newInstance(session);
                    chatClient.sessionUID = sessionUID;
                    return chatClient;
                }
            }
            throw new InvalidClientNameException("Invalid client name: " + clientName);
        } catch (InvocationTargetException e) {
            Utils.printlnError(e.getClass().getName() + " " + e.getCause().getMessage());
        } catch (ReflectiveOperationException | InvalidClientNameException e) {
            Utils.printlnError(e.getMessage());
        }
        return null;
    }

    /**
     * Add tags to the session
     *
     * @param user       the user
     * @param sessionUID the session UID
     * @param tags       the tags to add
     */
    static void addTags(String user, String sessionUID, String[] tags) {
        JSONObject session = SessionManager.getSession(user, sessionUID);
        if (session == null) {
            return;
        }
        for (String tag : tags) {
            session.getJSONArray("tags").put(tag.trim());
        }
        SessionManager.setSession(user, sessionUID, session);
    }

    /**
     * Remove a tag from the session
     *
     * @param user       the user
     * @param sessionUID the session UID
     * @param tag        the tag to remove
     */
    static void removeTag(String user, String sessionUID, String tag) {
        JSONObject session = SessionManager.getSession(user, sessionUID);
        if (session == null) {
            return;
        }
        for (int i = 0; i < session.getJSONArray("tags").length(); i++) {
            if (session.getJSONArray("tags").getString(i).equals(tag)) {
                session.getJSONArray("tags").remove(i);
                break;
            }
        }
        SessionManager.setSession(user, sessionUID, session);
    }

    /**
     * Add a description to the session
     *
     * @param user       the user
     * @param sessionUID  the session UID
     * @param description the description to add
     */
    static void setDescription(String user, String sessionUID, String description) {
        JSONObject session = SessionManager.getSession(user, sessionUID);
        if (session == null) {
            return;
        }
        session.put("description", description.trim());
        SessionManager.setSession(user, sessionUID, session);
    }

    /**
     * Top-level Read-Eval-Print Loop
     *
     * @param user the user
     */
    public static void repl(String user) {
        Utils.printlnInfo(banner + String.format("Welcome %s to LLM ChatManager!", user));
        SessionManager.loadDatabase();
        SessionManager.initSessions(user);
        printHelp(menus);

        while (true) {
            try {
                Utils.printInfo(replPrompt);
                String[] tokens = lineReader.readLine().split("\\s+");
                String command = tokens[0];
                String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
                switch (command) {
                    case "show":
                        System.out.println("Available LLM Chat Clients: " + getChatClientNames());
                        break;
                    case "chat":
                        // default client name is GPT-4o for testing
                        if (args.length > 1) {
                            Utils.printlnError("Usage: chat [clientName]");
                            break;
                        }
                        String clientName = args.length == 0 ? "GPT-4o" : args[0];
                        chatClient = getChatClient(clientName);
                        if (chatClient == null) {
                            break;
                        }
                        chatClient.repl();
                        System.out.println("Session " + chatClient.sessionUID + " ended");
                        chatClient.saveClient(user);
                        break;
                    case "tag":
                        if (args.length < 2) {
                            Utils.printlnError("Usage: tag [session] [tag1] [tag2] ...");
                            break;
                        }
                        addTags(user, args[0], Arrays.copyOfRange(args, 1, args.length));
                        SessionManager.listSessions(user);
                        break;
                    case "untag":
                        if (args.length != 2) {
                            Utils.printlnError("Usage: untag [session] [tag]");
                            break;
                        }
                        removeTag(user, args[0], args[1]);
                        SessionManager.listSessions(user);
                        break;
                    case "desc":
                        if (args.length < 1) {
                            Utils.printlnError("Usage: description [session] [description]");
                            break;
                        }
                        String description = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1,
                                args.length)) : "";
                        setDescription(user, args[0], description);
                        SessionManager.listSessions(user);
                        break;
                    case "list":
                        SessionManager.listSessions(user);
                        break;
                    case "load":
                        if (args.length < 1) {
                            Utils.printlnError("Usage: load [clientUID]");
                            break;
                        }
                        String sessionUID = args[0];
                        chatClient = getChatClient(user, sessionUID);
                        if (chatClient == null) {
                            break;
                        }
                        chatClient.repl();
                        System.out.println("Session " + sessionUID + " ended");
                        chatClient.saveClient(user);
                        break;
                    case "whoami":
                        System.out.println(user);
                        break;
                    case "profile":
                        SessionManager.profile(user);
                        break;
                    case "help":
                        printHelp(menus);
                        break;
                    case "exit":
                        throw new EndOfFileException();
                    case "":
                        break;
                    default:
                        Utils.printlnError("Invalid command");
                }
            } catch (UserInterruptException | EndOfFileException e) {
                SessionManager.saveDatabase();
                return;
            }
        }
    }

    /**
     * Administer Read-Eval-Print Loop
     */
    public static void adminRepl() {
        Utils.printlnInfo(banner + "Welcome to Administrator portal!");
        SessionManager.loadDatabase();
        printHelp(adminMenus);

        while (true) {
            try {
                Utils.printInfo(adminPromt);
                String[] tokens = lineReader.readLine().split("\\s+");
                String command = tokens[0];
                switch (command) {
                    case "profile":
                        SessionManager.profile("admin");
                        break;
                    case "help":
                        printHelp(adminMenus);
                        break;
                    case "exit":
                        throw new EndOfFileException();
                    case "":
                        break;
                    default:
                        Utils.printlnError("Invalid command");
                }
            } catch (UserInterruptException | EndOfFileException e) {
                SessionManager.saveDatabase();
                return;
            }
        }
    }
}
