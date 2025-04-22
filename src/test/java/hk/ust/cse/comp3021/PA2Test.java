package hk.ust.cse.comp3021;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PA2Test {
    /**
     * Generate profile once in a test and store here
     */
    static Map<String, JSONObject> userProfiles = new HashMap<>();

    /**
     * Parse profile and return the JSONObject
     *
     * @param user the username
     * @return the JSONObject parsed from the JSON file
     */
    static JSONObject getTestProfile(String user) throws IOException {
        Path filePath = Paths.get("src/test/resources/" + user + "-profile-test.json");
        return new JSONObject(Files.readString(filePath));
    }

    /**
     * Compare the profile by key
     *
     * @param profile     the original profile
     * @param testProfile the test profile
     * @param key         the key to compare
     */
    static void compareProfileByKey(JSONObject profile, JSONObject testProfile, String key) {
        if (key.startsWith("top"))
            assertEquals(profile.get(key).toString(), testProfile.get(key).toString());
        else
            assertEquals(profile.get(key), testProfile.get(key));
    }

    /**
     * Compare the profile by key with delta
     *
     * @param profile     the original profile
     * @param testProfile the test profile
     * @param key         the key to compare
     * @param delta       the delta for compare
     */
    static void compareProfileByKey(JSONObject profile, JSONObject testProfile, String key, double delta) {
        assertEquals(profile.getDouble(key), testProfile.getDouble(key), delta);
    }

    @BeforeAll
    public static void setUp() {
        SessionManager.loadDatabase();
        Utils.printlnInfo("Start computing and caching user profiles");
        for (String user : SessionManager.getUsers()) {
            userProfiles.put(user, SessionManager.generateProfile(user));
        }
        userProfiles.put("admin", SessionManager.generateProfile("admin"));
        Utils.printlnInfo("End computing and caching user profiles");
    }

    // Only used by TA, do not use it!
    @Test
    void setTestProfiles() throws IOException {
        for (String user : userProfiles.keySet()) {
            JSONObject testProfile = userProfiles.get(user);
            Path filePath = Paths.get("src/test/resources/" + user + "-profile-test.json");
            Files.writeString(filePath, testProfile.toString(2));
        }
    }

    @Test
    void testSumStatistics() throws IOException {
        for (String user : userProfiles.keySet()) {
            JSONObject profile = userProfiles.get(user);
            JSONObject testProfile = getTestProfile(user);
            compareProfileByKey(profile, testProfile, "sumPromptTokens");
            compareProfileByKey(profile, testProfile, "sumCompletionTokens");
        }
    }

    @Test
    void testMaxStatistics() throws IOException {
        for (String user : userProfiles.keySet()) {
            JSONObject profile = userProfiles.get(user);
            JSONObject testProfile = getTestProfile(user);
            compareProfileByKey(profile, testProfile, "maxPromptTokens");
            compareProfileByKey(profile, testProfile, "maxCompletionTokens");
            compareProfileByKey(profile, testProfile, "maxTimeLastOpen");
            compareProfileByKey(profile, testProfile, "maxTimeLastExit");
            compareProfileByKey(profile, testProfile, "maxTimeCreated");
        }
    }

    @Test
    void testMinStatistics() throws IOException {
        for (String user : userProfiles.keySet()) {
            JSONObject profile = userProfiles.get(user);
            JSONObject testProfile = getTestProfile(user);
            compareProfileByKey(profile, testProfile, "minPromptTokens");
            compareProfileByKey(profile, testProfile, "minCompletionTokens");
            compareProfileByKey(profile, testProfile, "minTimeLastOpen");
            compareProfileByKey(profile, testProfile, "minTimeLastExit");
            compareProfileByKey(profile, testProfile, "minTimeCreated");
        }
    }

    @Test
    void testAvgStatistics() throws IOException {
        for (String user : userProfiles.keySet()) {
            JSONObject profile = userProfiles.get(user);
            JSONObject testProfile = getTestProfile(user);
            compareProfileByKey(profile, testProfile, "avgPromptTokens");
            compareProfileByKey(profile, testProfile, "avgCompletionTokens");
            compareProfileByKey(profile, testProfile, "avgTimeCreated");
            compareProfileByKey(profile, testProfile, "avgTimeLastOpen");
            compareProfileByKey(profile, testProfile, "avgTimeLastExit");
            compareProfileByKey(profile, testProfile, "avgLastSessionDuration");
            compareProfileByKey(profile, testProfile, "avgTemperature", 0.01);
        }
    }

    @Test
    void testTopStringStatistics() throws IOException {
        for (String user : userProfiles.keySet()) {
            JSONObject profile = userProfiles.get(user);
            JSONObject testProfile = getTestProfile(user);
            compareProfileByKey(profile, testProfile, "topTags");
            compareProfileByKey(profile, testProfile, "topModels");
            compareProfileByKey(profile, testProfile, "topWords");
        }
    }

    @Test
    void testGeneralStatistics() throws IOException {
        for (String user : userProfiles.keySet()) {
            JSONObject profile = userProfiles.get(user);
            JSONObject testProfile = getTestProfile(user);
            compareProfileByKey(profile, testProfile, "numSessions");
        }
        JSONObject profile = userProfiles.get("admin");
        JSONObject testProfile = getTestProfile("admin");
        compareProfileByKey(profile, testProfile, "numUsers");
        compareProfileByKey(profile, testProfile, "avgSessions");
    }
}