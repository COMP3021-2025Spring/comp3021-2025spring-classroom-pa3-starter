package hk.ust.cse.comp3021;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class PA2Test {

    /**
     * Parse profile and return the JSONObject
     *
     * @param user the username
     * @return the JSONObject parsed from the JSON file
     */
    static JSONObject getUserProfile(String user) throws IOException {
        Path filePath = Paths.get("src/test/resources/" + user + "-profile.json");
        return new JSONObject(Files.readString(filePath));
    }

    @BeforeAll
    public static void setUp() {
        SessionManager.loadDatabase();
    }

    @Test
    void generateProfileForUser() throws IOException {
        // 50 users in total
        for (String user : SessionManager.getUsers()) {
            JSONObject profile = SessionManager.generateProfile(user);
            JSONObject testProfile = getUserProfile(user);
            assertEquals(profile.toString(), testProfile.toString());
        }
    }

    @Test
    void generateProfileForAdmin() throws IOException {
        JSONObject profile = SessionManager.generateProfile("admin");
        JSONObject testProfile = getUserProfile("admin");
        assertEquals(profile.toString(), testProfile.toString());
    }

    @Test
    void generateProfileForNewUser() throws IOException {
        JSONObject profile = SessionManager.generateProfile("non-existent");
        JSONObject testProfile = getUserProfile("non-existent");
        assertEquals(profile.toString(), testProfile.toString());
    }
}