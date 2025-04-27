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
import java.util.stream.Collector;
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
                    .stream()
                    .flatMap(SessionManager::getSessionsStream);
        else
            return db.getJSONObject(user).toMap()
                    .keySet()
                    .stream()
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
     * Supplier in the collect method
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
     * Accumulate the session to the profile
     *
     * @param profile the profile to accumulate to
     * @param session the session to accumulate
     */
    static void accumulateSessionToProfile(JSONObject profile, JSONObject session) {
        profile.increment("numSessions")
                .put("sumPromptTokens", profile.getInt("sumPromptTokens") + session.getInt("totalPromptTokens"))
                .put("sumCompletionTokens", profile.getInt("sumCompletionTokens") + session.getInt("totalCompletionTokens"))
                .put("sumTemperature", profile.getDouble("sumTemperature") + session.getDouble("temperature"))
                .put("sumTimeCreated", profile.getInt("sumTimeCreated") + session.getInt("timeCreated") % Utils.SoD)
                .put("sumTimeLastOpen", profile.getInt("sumTimeLastOpen") + session.getInt("timeLastOpen") % Utils.SoD)
                .put("sumTimeLastExit", profile.getInt("sumTimeLastExit") + session.getInt("timeLastExit") % Utils.SoD)
                .put("sumLastSessionDuration", profile.getInt("sumLastSessionDuration")
                        + Utils.getDuration(session.getInt("timeLastOpen"), session.getInt("timeLastExit")))
                .put("sumPrice", profile.getDouble("sumPrice")
                        + (session.getInt("totalPromptTokens") * unitPromptPrice
                        + session.getInt("totalCompletionTokens") * unitCompletionPrice)
                        * Integer.parseInt(session.getString("clientName").split("[-b]")[1]))
                .put("maxPromptTokens", Math.max(profile.getInt("maxPromptTokens"), session.getInt("totalPromptTokens")))
                .put("maxCompletionTokens", Math.max(profile.getInt("maxCompletionTokens"), session.getInt("totalCompletionTokens")))
                .put("maxTimeCreated", Math.max(profile.getInt("maxTimeCreated"), session.getInt("timeCreated")))
                .put("maxTimeLastOpen", Math.max(profile.getInt("maxTimeLastOpen"), session.getInt("timeLastOpen")))
                .put("maxTimeLastExit", Math.max(profile.getInt("maxTimeLastExit"), session.getInt("timeLastExit")))
                .put("minPromptTokens", Math.min(profile.getInt("minPromptTokens"), session.getInt("totalPromptTokens")))
                .put("minCompletionTokens", Math.min(profile.getInt("minCompletionTokens"), session.getInt("totalCompletionTokens")))
                .put("minTimeCreated", Math.min(profile.getInt("minTimeCreated"), session.getInt("timeCreated")))
                .put("minTimeLastOpen", Math.min(profile.getInt("minTimeLastOpen"), session.getInt("timeLastOpen")))
                .put("minTimeLastExit", Math.min(profile.getInt("minTimeLastExit"), session.getInt("timeLastExit")))
                .put("topTags", updateTopString(profile.getJSONObject("topTags"),
                        session.getJSONArray("tags").toList().stream().map(String::valueOf)))
                .put("topWords", updateTopString(profile.getJSONObject("topWords"),
                        tokenizeMessages(session).filter(word -> !ignoredWords.contains(word))))
                .put("topModels", updateTopString(profile.getJSONObject("topModels"),
                        Stream.of(session.getString("clientName").split("-")[0])));
    }

    /**
     * Combine two profiles into one
     *
     * @param profile1 the accumulated profile from one stream group
     * @param profile2 the accumulated profile from another stream group
     */
    static JSONObject combineTwoProfiles(JSONObject profile1, JSONObject profile2) {
        return profile1.put("numSessions", profile1.getInt("numSessions") + profile2.getInt("numSessions"))
                .put("sumPromptTokens", profile1.getInt("sumPromptTokens") + profile2.getInt("sumPromptTokens"))
                .put("sumCompletionTokens", profile1.getInt("sumCompletionTokens") + profile2.getInt("sumCompletionTokens"))
                .put("sumTemperature", profile1.getDouble("sumTemperature") + profile2.getDouble("sumTemperature"))
                .put("sumTimeCreated", profile1.getInt("sumTimeCreated") + profile2.getInt("sumTimeCreated"))
                .put("sumTimeLastOpen", profile1.getInt("sumTimeLastOpen") + profile2.getInt("sumTimeLastOpen"))
                .put("sumTimeLastExit", profile1.getInt("sumTimeLastExit") + profile2.getInt("sumTimeLastExit"))
                .put("sumLastSessionDuration", profile1.getInt("sumLastSessionDuration") + profile2.getInt("sumLastSessionDuration"))
                .put("sumPrice", profile1.getDouble("sumPrice") + profile2.getDouble("sumPrice"))
                .put("maxPromptTokens", Math.max(profile1.getInt("maxPromptTokens"), profile2.getInt("maxPromptTokens")))
                .put("maxCompletionTokens", Math.max(profile1.getInt("maxCompletionTokens"), profile2.getInt("maxCompletionTokens")))
                .put("maxTimeCreated", Math.max(profile1.getInt("maxTimeCreated"), profile2.getInt("maxTimeCreated")))
                .put("maxTimeLastOpen", Math.max(profile1.getInt("maxTimeLastOpen"), profile2.getInt("maxTimeLastOpen")))
                .put("maxTimeLastExit", Math.max(profile1.getInt("maxTimeLastExit"), profile2.getInt("maxTimeLastExit")))
                .put("minPromptTokens", Math.min(profile1.getInt("minPromptTokens"), profile2.getInt("minPromptTokens")))
                .put("minCompletionTokens", Math.min(profile1.getInt("minCompletionTokens"), profile2.getInt("minCompletionTokens")))
                .put("minTimeCreated", Math.min(profile1.getInt("minTimeCreated"), profile2.getInt("minTimeCreated")))
                .put("minTimeLastOpen", Math.min(profile1.getInt("minTimeLastOpen"), profile2.getInt("minTimeLastOpen")))
                .put("minTimeLastExit", Math.min(profile1.getInt("minTimeLastExit"), profile2.getInt("minTimeLastExit")))
                .put("topTags", mergeTopString(profile1.getJSONObject("topTags"), profile2.getJSONObject("topTags")))
                .put("topWords", mergeTopString(profile1.getJSONObject("topWords"), profile2.getJSONObject("topWords")))
                .put("topModels", mergeTopString(profile1.getJSONObject("topModels"), profile2.getJSONObject("topModels")));
    }

    /**
     * Post process the profile, compute admin, average, top N statistics and remove useless statistics
     *
     * @param profile the profile to post process
     * @return the post processed profile
     */
    static JSONObject postProcess(JSONObject profile) {
        int numSessions = profile.getInt("numSessions");
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
     * The collector to collect the profile
     * The accumulator is accumulateSessionToProfile
     * The combiner is combineTwoProfiles
     * The finisher is postProcess
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collector.html">...</a>
     */
    static Collector<JSONObject, JSONObject, JSONObject> profileCollector = Collector.of(
            SessionManager::createEmptyProfile,
            SessionManager::accumulateSessionToProfile,
            SessionManager::combineTwoProfiles,
            SessionManager::postProcess
    );

    /**
     * Generate the profile using collect + stream
     *
     * @param user the user to generate profile for
     * @return the profile
     */
    static JSONObject generateProfileBase(String user) {
        return getSessionsStream(user).collect(profileCollector);
    }

    /**
     * Generate the profile using collect + parallel stream
     *
     * @param user the user to generate profile for
     * @return the profile
     */
    static JSONObject generateProfileParallel(String user) {
        return getSessionsStream(user).parallel().collect(profileCollector);
    }

    /**
     * Generate the profile using Fork & Join Pool
     * The goal is to implement a better parallel profiling
     *
     * @param user the user to generate profile for
     * @return the profile
     */
    static JSONObject generateProfileForkJoin(String user) {
        return getSessionsStream(user).parallel().collect(profileCollector);
    }

    /**
     * Common interface for generating profile, by default using the parallel version
     * Casting the epoch time from Long to Integer is safe because we will not encounter the
     * <a href="https://en.wikipedia.org/wiki/Year_2038_problem">Year 2038 problem</a>.
     *
     * @param user the user to generate profile for
     * @return the profile
     */
    static JSONObject generateProfile(String user) {
        JSONObject profile = generateProfileParallel(user);
        // admin only statistics
        if (user.equals("admin")) {
            profile.put("numUsers", getNumUsers());
            profile.put("avgSessions", profile.getInt("numSessions") / getNumUsers());
        }
        return profile;
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
