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
            Utils.printlnError("Failed to load the database: " + e.getMessage());
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
                    .keySet()
                    .parallelStream()
                    .flatMap(SessionManager::getSessionsStream);
        else
            return db.getJSONObject(user).toMap()
                    .keySet()
                    .parallelStream()
                    .map(uid -> getSession(user, uid));
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
        db.getJSONObject(user).keySet()
                .forEach((key) -> printSession(key, Objects.requireNonNull(getSession(user, key))));
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
     * Get the set of users
     *
     * @return the set of users
     */
    public static Set<String> getUsers() {
        return db.toMap().keySet();
    }

    /**
     * The list of ignored words when counting top words
     */
    static List<String> ignoredWords;

    // Load the stopwords from the file stopwords.txt, remember to filter out comments
    static {
        try {
            ignoredWords = Files.readAllLines(Paths.get("stopwords.txt"));
        } catch (IOException e) {
            Utils.printlnError("Failed to load stopwords: " + e.getMessage());
            ignoredWords = new ArrayList<>();
        }
    }

    /**
     * The unit USD price of a prompt token using one billion parameters
     */
    static final Double unitPromptPrice = 8.82e-09;

    /**
     * The unit USD price of a response token using one billion parameters
     */
    static final Double unitCompletionPrice = 3.53e-08;

    /**
     * Initialize an empty profile
     *
     * @return the empty profile
     */
    static JSONObject createEmptyProfile() {
        return new JSONObject()
                .put("numSessions", 0)
                .put("sumPromptTokens", 0)
                .put("sumCompletionTokens", 0)
                .put("sumTemperature", 0.0)
                .put("sumTimeCreated", 0)
                .put("sumTimeLastExit", 0)
                .put("sumTimeLastOpen", 0)
                .put("sumLastSessionDuration", 0)
                .put("sumPrice", 0.0)
                .put("maxPromptTokens", Integer.MIN_VALUE)
                .put("maxCompletionTokens", Integer.MIN_VALUE)
                .put("maxTimeCreated", Integer.MIN_VALUE)
                .put("maxTimeLastOpen", Integer.MIN_VALUE)
                .put("maxTimeLastExit", Integer.MIN_VALUE)
                .put("minPromptTokens", Integer.MAX_VALUE)
                .put("minCompletionTokens", Integer.MAX_VALUE)
                .put("minTimeCreated", Integer.MAX_VALUE)
                .put("minTimeLastOpen", Integer.MAX_VALUE)
                .put("minTimeLastExit", Integer.MAX_VALUE)
                .put("topTags", new JSONObject())
                .put("topWords", new JSONObject())
                .put("topModels", new JSONObject());
    }

    /**
     * Update the top string map with new strings
     *
     * @param topStringMap the top string map, from its
     * @param newStrings   the new strings to add
     * @return the updated top string map
     */
    private static JSONObject updateTopString(JSONObject topStringMap, Stream<String> newStrings) {
        newStrings.forEach(topStringMap::increment);
        return topStringMap;
    }

    /**
     * Merge two top string maps
     *
     * @param topStringMap1 the first top string map
     * @param topStringMap2 the second top string map
     * @return the merged top string map
     */
    private static JSONObject mergeTopString(JSONObject topStringMap1, JSONObject topStringMap2) {
        JSONObject mergedTopStringMap = new JSONObject();
        topStringMap1.toMap().forEach(mergedTopStringMap::put);
        topStringMap2.toMap().forEach((key, value) -> {
            if (mergedTopStringMap.has(key)) {
                mergedTopStringMap.put(key, mergedTopStringMap.getInt(key) + (Integer) value);
            } else {
                mergedTopStringMap.put(key, value);
            }
        });
        return mergedTopStringMap;
    }

    /**
     * Keep only the top N strings in the map, first sorted by the value, then by the key
     *
     * @param topNStringMap the top N string map
     * @param topN          the number of top strings to keep
     * @return the updated top N string map
     */
    private static JSONObject limitTopNString(JSONObject topNStringMap, int topN) {
        return topNStringMap.toMap().entrySet().stream()
                .sorted((e1, e2) -> {
                    Integer i1 = (Integer) e1.getValue();
                    Integer i2 = (Integer) e2.getValue();
                    if (i1.equals(i2)) {
                        return e2.getKey().compareTo(e1.getKey());
                    } else {
                        return i2.compareTo(i1);
                    }
                })
                .limit(topN)
                .collect(
                        JSONObject::new,
                        (json, entry) -> json.put(entry.getKey(), entry.getValue()),
                        SessionManager::mergeTopString
                );
    }

    /**
     * Tokenize the message into stream of words
     *
     * @param session the session contains messages
     * @return the stream of tokens
     */
    static Stream<String> tokenizeMessages(JSONObject session) {
        return session.getJSONObject("messages")
                .getJSONArray("contents")
                .toList()
                .stream()
                .map(m -> ((Map<String, String>) m).get("content")
                        .replaceAll("[^a-zA-Z0-9]", " ")
                        .toLowerCase())
                .flatMap(m -> Arrays.stream(m.split(" ")))
                .filter(str -> str.matches("[a-zA-Z]+"));
    }

    /**
     * Generate a profile for the user and save as json file using reduce
     * Casting the epoch time from Long to Integer is safe because we will not encounter the
     * <a href="https://en.wikipedia.org/wiki/Year_2038_problem">Year 2038 problem</a>.
     *
     * @param user the user to generate profile for
     */
    static JSONObject generateProfile(String user) {
        // iterate through all sessions and collect statistics
        JSONObject profile = getSessionsStream(user).collect(
                // Supplier: create an empty profile
                SessionManager::createEmptyProfile,
                (p, s) -> {
                    // Accumulator: update the profile with session data
                    p.increment("numSessions")
                            .put("sumPromptTokens", p.getInt("sumPromptTokens") + s.getInt("totalPromptTokens"))
                            .put("sumCompletionTokens", p.getInt("sumCompletionTokens") + s.getInt("totalCompletionTokens"))
                            .put("sumTemperature", p.getDouble("sumTemperature") + s.getDouble("temperature"))
                            .put("sumTimeCreated", p.getInt("sumTimeCreated") + s.getInt("timeCreated") % Utils.SoD)
                            .put("sumTimeLastOpen", p.getInt("sumTimeLastOpen") + s.getInt("timeLastOpen") % Utils.SoD)
                            .put("sumTimeLastExit", p.getInt("sumTimeLastExit") + s.getInt("timeLastExit") % Utils.SoD)
                            .put("sumLastSessionDuration", p.getInt("sumLastSessionDuration")
                                    + Utils.getDuration(s.getInt("timeLastOpen"), s.getInt("timeLastExit")))
                            .put("sumPrice", p.getDouble("sumPrice")
                                    + (s.getInt("totalPromptTokens") * unitPromptPrice
                                    + s.getInt("totalCompletionTokens") * unitCompletionPrice)
                                    * Integer.parseInt(s.getString("clientName").split("[-b]")[1]))
                            .put("maxPromptTokens", Math.max(p.getInt("maxPromptTokens"), s.getInt("totalPromptTokens")))
                            .put("maxCompletionTokens", Math.max(p.getInt("maxCompletionTokens"), s.getInt("totalCompletionTokens")))
                            .put("maxTimeCreated", Math.max(p.getInt("maxTimeCreated"), s.getInt("timeCreated")))
                            .put("maxTimeLastOpen", Math.max(p.getInt("maxTimeLastOpen"), s.getInt("timeLastOpen")))
                            .put("maxTimeLastExit", Math.max(p.getInt("maxTimeLastExit"), s.getInt("timeLastExit")))
                            .put("minPromptTokens", Math.min(p.getInt("minPromptTokens"), s.getInt("totalPromptTokens")))
                            .put("minCompletionTokens", Math.min(p.getInt("minCompletionTokens"), s.getInt("totalCompletionTokens")))
                            .put("minTimeCreated", Math.min(p.getInt("minTimeCreated"), s.getInt("timeCreated")))
                            .put("minTimeLastOpen", Math.min(p.getInt("minTimeLastOpen"), s.getInt("timeLastOpen")))
                            .put("minTimeLastExit", Math.min(p.getInt("minTimeLastExit"), s.getInt("timeLastExit")))
                            .put("topTags", updateTopString(p.getJSONObject("topTags"),
                                    s.getJSONArray("tags").toList().stream().map(String::valueOf)))
                            .put("topWords", updateTopString(p.getJSONObject("topWords"),
                                    tokenizeMessages(s).filter(word -> !ignoredWords.contains(word))))
                            .put("topModels", updateTopString(p.getJSONObject("topModels"),
                                    Stream.of(s.getString("clientName").split("-")[0])));
                },
                (p1, p2) -> {
                    // Combiner: merge two profiles (used in parallel streams)
                    p1.put("numSessions", p1.getInt("numSessions") + p2.getInt("numSessions"))
                            .put("sumPromptTokens", p1.getInt("sumPromptTokens") + p2.getInt("sumPromptTokens"))
                            .put("sumCompletionTokens", p1.getInt("sumCompletionTokens") + p2.getInt("sumCompletionTokens"))
                            .put("sumTemperature", p1.getDouble("sumTemperature") + p2.getDouble("sumTemperature"))
                            .put("sumTimeCreated", p1.getInt("sumTimeCreated") + p2.getInt("sumTimeCreated"))
                            .put("sumTimeLastOpen", p1.getInt("sumTimeLastOpen") + p2.getInt("sumTimeLastOpen"))
                            .put("sumTimeLastExit", p1.getInt("sumTimeLastExit") + p2.getInt("sumTimeLastExit"))
                            .put("sumLastSessionDuration", p1.getInt("sumLastSessionDuration") + p2.getInt("sumLastSessionDuration"))
                            .put("sumPrice", p1.getDouble("sumPrice") + p2.getDouble("sumPrice"))
                            .put("maxPromptTokens", Math.max(p1.getInt("maxPromptTokens"), p2.getInt("maxPromptTokens")))
                            .put("maxCompletionTokens", Math.max(p1.getInt("maxCompletionTokens"), p2.getInt("maxCompletionTokens")))
                            .put("maxTimeCreated", Math.max(p1.getInt("maxTimeCreated"), p2.getInt("maxTimeCreated")))
                            .put("maxTimeLastOpen", Math.max(p1.getInt("maxTimeLastOpen"), p2.getInt("maxTimeLastOpen")))
                            .put("maxTimeLastExit", Math.max(p1.getInt("maxTimeLastExit"), p2.getInt("maxTimeLastExit")))
                            .put("minPromptTokens", Math.min(p1.getInt("minPromptTokens"), p2.getInt("minPromptTokens")))
                            .put("minCompletionTokens", Math.min(p1.getInt("minCompletionTokens"), p2.getInt("minCompletionTokens")))
                            .put("minTimeCreated", Math.min(p1.getInt("minTimeCreated"), p2.getInt("minTimeCreated")))
                            .put("minTimeLastOpen", Math.min(p1.getInt("minTimeLastOpen"), p2.getInt("minTimeLastOpen")))
                            .put("minTimeLastExit", Math.min(p1.getInt("minTimeLastExit"), p2.getInt("minTimeLastExit")))
                            .put("topTags", mergeTopString(p1.getJSONObject("topTags"), p2.getJSONObject("topTags")))
                            .put("topWords", mergeTopString(p1.getJSONObject("topWords"), p2.getJSONObject("topWords")))
                            .put("topModels", mergeTopString(p1.getJSONObject("topModels"), p2.getJSONObject("topModels")));
                }
        );
        // post process
        int numSessions = profile.getInt("numSessions");
        // admin only statistics
        if (user.equals("admin")) {
            profile.put("numUsers", getNumUsers());
            profile.put("avgSessions", numSessions / getNumUsers());
        }
        return profile
                // get average statistics
                .put("avgTemperature", numSessions == 0 ? 0.0 : profile.getDouble("sumTemperature") / numSessions)
                .put("avgTimeLastOpen", numSessions == 0 ? 0 : profile.getInt("sumTimeLastOpen") / numSessions)
                .put("avgTimeCreated", numSessions == 0 ? 0 : profile.getInt("sumTimeCreated") / numSessions)
                .put("avgTimeLastExit", numSessions == 0 ? 0 : profile.getInt("sumTimeLastExit") / numSessions)
                .put("avgLastSessionDuration", numSessions == 0 ? 0 : profile.getInt("sumLastSessionDuration") / numSessions)
                .put("avgPromptTokens", numSessions == 0 ? 0 : profile.getInt("sumPromptTokens") / numSessions)
                .put("avgCompletionTokens", numSessions == 0 ? 0 : profile.getInt("sumCompletionTokens") / numSessions)
                // remove useless statistics
                .put("sumTemperature", (Object) null)
                .put("sumTimeCreated", (Object) null)
                .put("sumTimeLastOpen", (Object) null)
                .put("sumTimeLastExit", (Object) null)
                .put("sumLastSessionDuration", (Object) null)
                // filter the topN strings
                .put("topTags", limitTopNString(profile.getJSONObject("topTags"), 3))
                .put("topWords", limitTopNString(profile.getJSONObject("topWords"), 20))
                .put("topModels", limitTopNString(profile.getJSONObject("topModels"), 5));
    }

    /**
     * Print the profile in human-readable form
     *
     * @param profile the JSON format profile to print
     */
    private static void printProfile(JSONObject profile) {
        for (String key : profile.keySet()) {
            if (profile.get(key) instanceof JSONObject) {
                System.out.printf("%s: %s %n", Utils.toInfo(key), profile.getJSONObject(key).toString(2));
            } else if (profile.get(key) instanceof Double) {
                System.out.printf("%s: %.4f %n", Utils.toInfo(key), profile.getDouble(key));
            } else if (key.contains("Time")) {
                System.out.printf("%s: %s %n", Utils.toInfo(key), Utils.timeToString(profile.getInt(key)));
            } else {
                System.out.printf("%s: %s %n", Utils.toInfo(key), profile.get(key));
            }
        }
    }

    /**
     * Generate a profile for the user, print and save as json file
     *
     * @param user the user to generate profile for
     */
    public static void profile(String user) {
        // print profile to stdout
        System.out.printf("----- %s CHAT CLIENT PROFILE ----- %n", user.toUpperCase());
        JSONObject profile = generateProfile(user);
        printProfile(profile);
        // save profile to file
        try {
            Path filePath = Paths.get(user + "-profile.json");
            Files.writeString(filePath, profile.toString(2));
            System.out.println("Profile generated at " + Utils.toInfo(filePath.toString()));
        } catch (IOException e) {
            Utils.printlnError("Fail to generate profile for " + user);
        }
    }
}
