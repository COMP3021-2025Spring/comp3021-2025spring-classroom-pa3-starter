/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SessionManager class provides read/write interface for session database
 */
public class SessionManager {
    /**
     * The session database in memory as a JSONObject
     */
    static JSONObject db;

    /**
     * The path to the session database file
     */
    static final String dbPath = "db.json";

    /**
     * Load session database from file into memory, create an empty database if not exist
     */
    static void loadDatabase() {
        try {
            Path filePath = Paths.get(dbPath);
            db = new JSONObject(Files.readString(filePath));
            Utils.printlnInfo("Sessions database loaded");
        } catch (IOException | JSONException e) {
            Utils.printlnError("Failed to load the session: " + e.getMessage());
            db = new JSONObject();
            Utils.printlnInfo("Empty sessions database created");
        }
    }

    /**
     * Save session database from memory to file
     */
    static void saveDatabase() {
        try {
            Path filePath = Paths.get(dbPath);
            Files.writeString(filePath, db.toString(4));
            Utils.printlnInfo("Sessions database saved");
        } catch (IOException e) {
            Utils.printlnError("Fail to save sessions database");
        }
    }

    /**
     * Initialize sessions for current user
     *
     * @param user the user
     */
    static void initSessions(String user) {
        if (!db.has(user)) {
            db.put(user, new JSONObject());
        }
    }

    /**
     * Get the session given the user and sessionUID
     *
     * @param user       the user to get session for
     * @param sessionUID the sessionUID to get
     * @return the session
     */
    static JSONObject getSession(String user, String sessionUID) {
        try {
            return db.getJSONObject(user).getJSONObject(sessionUID);
        } catch (JSONException e) {
            Utils.printlnError("Failed to get the session: " + e.getMessage());
        }
        return null;
    }

    /**
     * Save a session to the session database
     *
     * @param user    the user to save session for
     * @param session the session to save
     */
    static void setSession(String user, String sessionUID, JSONObject session) {
        try {
            db.getJSONObject(user).put(sessionUID, session);
        } catch (JSONException e) {
            Utils.printlnError("Failed to save the session: " + e.getMessage());
        }
    }

    /**
     * Get all the sessions of the user as stream
     *
     * @param user the user to get sessions for
     * @return the sessions
     */
    static Stream<JSONObject> getSessionsStream(String user) {
        if (Objects.equals(user, "admin"))
            return db.toMap()
                    .entrySet().stream()
                    .flatMap(e -> getSessionsStream(e.getKey()));
        else
            return db.getJSONObject(user).toMap()
                    .keySet().stream()
                    .map(uid -> Objects.requireNonNull(getSession(user, uid)));
    }

    /**
     * Print the session information
     *
     * @param session the session to print
     */
    static void printSession(String sessionUID, JSONObject session) {
        try {
            String clientName = session.getString("clientName");
            String tags = session.getJSONArray("tags").join(", ");
            String description = session.getString("description");
            String timeCreated = Utils.timeToString(session.getLong("timeCreated"));
            String timeLastExit = Utils.timeToString(session.getLong("timeLastExit"));
            System.out.printf("UID: %s Client: %-20s Created: %s Last Exit: %s Tags: %-30s Description: %s %n",
                    Utils.toInfo(sessionUID), Utils.toInfo(clientName), Utils.toInfo(timeCreated),
                    Utils.toInfo(timeLastExit), Utils.toInfo(tags), Utils.toInfo(description));
        } catch (JSONException e) {
            Utils.printlnError("Error reading session: " + e.getMessage());
        }
    }

    /**
     * List all the previously stored sessions
     *
     * @param user the user to list sessions for
     */
    public static void listSessions(String user) {
        db.getJSONObject(user).toMap()
                .forEach((key, value) ->
                        printSession(key, Objects.requireNonNull(getSession(user, key))));
    }

    /**
     * Get the number of users
     *
     * @return the number of users
     */
    public static int getNumUsers() {
        return db.toMap().size();
    }

    /**
     * Get the number of sessions of the user
     *
     * @param user the user to get number of sessions for
     * @return the number of sessions
     */
    public static long getNumSessions(String user) {
        return getSessionsStream(user).count();
    }

    /**
     * Get the statistics of the user using map-reduce
     *
     * @param user the user to get statistics for, use "admin" to get statistics for all users
     * @param m    the map function
     * @param i    the identity value
     * @param r    the reduce function
     * @return the statistics
     */
    public static int getStat(String user, ToIntFunction<JSONObject> m, int i, IntBinaryOperator r) {
        return getSessionsStream(user)
                .mapToInt(m)
                .reduce(i, r);
    }

    /**
     * The list of ignored words when counting top words
     */
    static final List<String> ignoredWords = List.of("i", "it",
            "She", "The", "This", "You", "a", "about", "all", "also", "am", "an", "and", "any", "are", "as", "at",
            "be", "been", "but", "by", "can", "could", "data", "de", "def", "do", "each", "following", "for", "from",
            "had", "has", "have", "he", "help", "her", "his", "how", "if", "important", "in", "information", "into",
            "is", "it", "its", "key", "like", "make", "may", "me", "more", "my", "need",
            "no", "not", "of", "on", "one", "or", "other", "provide", "return", "she", "so", "some", "specific",
            "such", "that", "the", "their", "there", "they", "this", "to", "use", "used", "using", "was", "we", "what"
            , "which", "who", "will", "with", "would", "you", "your", "here", "were", "does", "our", "a", "if", "he");


    /**
     * Counts the number of occurrences of the top k string in the stream using reduce
     *
     * @return the hash map of the string and its count
     */
    static HashMap<String, Integer> topString(Stream<String> stream, int k) {
        HashMap<String, Integer> map = new HashMap<>();
        stream.forEach(str -> map.put(str, map.getOrDefault(str, 0) + 1));
        return map.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(k)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }

    /**
     * Generate a profile for the user and save as json file
     * Casting the epoch time from Long to Integer is safe because we will not encounter the
     * <a href="https://en.wikipedia.org/wiki/Year_2038_problem">Year 2038 problem</a>.
     *
     * @param user the user to generate profile for
     */
    public static void generateProfile(String user) {
        JSONObject profile = new JSONObject();

        if (user.equals("admin"))
            profile.put("numUsers", getNumUsers());
        profile.put("numSessions", getNumSessions(user));
        // get sum statistics
        profile.put("sumPromptTokens", getStat(user,
                s -> s.getInt("totalPromptTokens"),
                0,
                Integer::sum));
        profile.put("sumCompletionTokens", getStat(user,
                s -> s.getInt("totalCompletionTokens"),
                0,
                Integer::sum));
        // get max statistics
        profile.put("maxPromptTokens", getStat(user,
                s -> s.getInt("totalPromptTokens"),
                Integer.MIN_VALUE,
                Math::max));
        profile.put("maxCompletionTokens", getStat(user,
                s -> s.getInt("totalCompletionTokens"),
                Integer.MIN_VALUE,
                Math::max));
        profile.put("maxTimeCreated", Utils.timeToString(getStat(user,
                s -> s.getInt("timeCreated"),
                Integer.MIN_VALUE,
                Math::max)));
        profile.put("maxTimeLastExit", Utils.timeToString(getStat(user,
                s -> s.getInt("timeLastExit"),
                Integer.MIN_VALUE,
                Math::max)));
        // get min statistics
        profile.put("minPromptTokens", getStat(user,
                s -> s.getInt("totalPromptTokens"),
                Integer.MAX_VALUE,
                Math::min));
        profile.put("minCompletionTokens", getStat(user,
                s -> s.getInt("totalCompletionTokens"),
                Integer.MAX_VALUE,
                Math::min));
        profile.put("minTimeCreated", Utils.timeToString(getStat(user,
                s -> s.getInt("timeCreated"),
                Integer.MAX_VALUE,
                Math::min)));
        // get average statistics
        profile.put("averagePromptTokens", (double) getStat(user,
                s -> s.getInt("totalPromptTokens"),
                0,
                Integer::sum) / getNumSessions(user));
        profile.put("averageCompletionTokens", (double) getStat(user,
                s -> s.getInt("totalCompletionTokens"),
                0,
                Integer::sum) / getNumSessions(user));
        profile.put("averageTemperature", (double) getStat(user,
                s -> (int) s.getDouble("temperature") * 10,
                0,
                Integer::sum) / (10 * getNumSessions(user)));
        profile.put("averageTimeCreated", Utils.timeToString(getStat(user,
                s -> s.getInt("timeCreated") % 86400,
                0,
                Integer::sum) / getNumSessions(user)));
        profile.put("averageTimeLastOpen", Utils.timeToString(getStat(user,
                s -> s.getInt("timeLastOpen") % 86400,
                0,
                Integer::sum) / getNumSessions(user)));
        profile.put("averageTimeLastExit", Utils.timeToString(getStat(user,
                s -> s.getInt("timeLastExit") % 86400,
                0,
                Integer::sum) / getNumSessions(user)));
        profile.put("averageLastSessionDuration", (getStat(user,
                s -> Utils.getDuration(s.getInt("timeLastOpen"), s.getInt("timeLastExit")),
                0,
                Integer::sum) / getNumSessions(user)));
        // get top String statistics
        profile.put("topTags", topString(getSessionsStream(user)
                        .flatMap(s -> s.getJSONArray("tags")
                                .toList()
                                .stream()
                                .map(String::valueOf))
                , 3));
        profile.put("topWords", topString(getSessionsStream(user)
                        .flatMap(s -> Arrays.stream(
                                s.getJSONObject("messages").getJSONArray("contents")
                                        .join(" ")
                                        .split("\\s+")))
                        .filter(s -> !ignoredWords.contains(s.toLowerCase()))
                        .filter(s -> s.matches("[a-zA-Z]+"))
                , 20));
        profile.put("topClients", topString(getSessionsStream(user)
                        .map(s -> s.getString("clientName"))
                , 3));

        System.out.printf("----- %s CHAT CLIENT PROFILE ----- %n", user.toUpperCase());
        System.out.println(profile.toString(2));

        try {
            Path filePath = Paths.get(user + "-profile.json");
            Files.writeString(filePath, profile.toString(2));
            System.out.println("Profile generated at " + Utils.toInfo(filePath.toString()));
        } catch (IOException e) {
            Utils.printlnError("Fail to generate profile for " + user);
        }
    }
}
