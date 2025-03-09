/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import hk.ust.cse.comp3021.client.GPT4oClient;
import hk.ust.cse.comp3021.exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hk.ust.cse.comp3021.ChatManager.getChatClient;
import static org.junit.jupiter.api.Assertions.*;

public class Task2Test {
    static ChatClient chatClient;
    static String clientUID;
    static File resourceDir;

    /**
     * Parse a JSON file and return the JSONObject
     *
     * @param resourceName the name of the JSON file
     * @return the JSONObject parsed from the JSON file
     */
    static JSONObject getJsonResource(String resourceName) {
        return Utils.parseJSON(new File(resourceDir, resourceName).getPath());
    }

    @BeforeAll
    static void setUp() {
        try {
            Path sessions = Path.of("sessions");
            if (!Files.exists(sessions)) {
                Files.createDirectory(sessions);
            }
        } catch (IOException e) {
            Utils.printlnError("Failed to create the session directory: " + e.getMessage());
        }
        resourceDir = new File("../src/test/resources");
        assertTrue(Files.exists(Path.of("keys/GPT-4o.txt")));
    }

    // The first three tests share the same chatClient and clientUID

    @Test
    @Order(1)
    void basicSerialization() {
        chatClient = new GPT4oClient();
        assertNotNull(chatClient);
        // some simple queries
        chatClient.query("Do you know the person Professor Charles Chuan Zhang at HKUST?");
        chatClient.query("Do you know the course COMP 3021 of HKUST?");
        clientUID = chatClient.getClientUID();
        chatClient.saveClient();
        JSONObject session = Utils.parseJSON(clientUID);
        assertNotNull(session);
    }

    @Test
    @Order(2)
    void basicDeserialization() {
        JSONObject session = Utils.parseJSON(clientUID);
        assertNotNull(session);
        ChatClient newChatClient = getChatClient(session);
        assertNotNull(newChatClient);
        // test if the ChatClient is identically deserialized
        assertEquals(chatClient, newChatClient);
        // test if the conversation is continued
        String response = chatClient.query("Who did I asked about?");
        assertTrue(response.toLowerCase().contains("charles"));
    }

    @Test()
    @Order(3)
    void basicPersistenceRoundTrip() {
        for (int i = 0; i < 10; i++) {
            chatClient.saveClient();
            JSONObject session = Utils.parseJSON(clientUID);
            assertNotNull(session);
            chatClient = getChatClient(session);
            assertNotNull(chatClient);
        }
    }

    // From here, the following tests are independent, testing against the PersistenceException

    @Test
    void jsonCheckDeserialization() {
        for (int i = 1; i <= 3; i++) {
            JSONObject session = getJsonResource("JsonCheck" + i);
            assertNotNull(session);
            assertThrows(JsonCheckException.class, () -> {
                ChatClient chatClient = new GPT4oClient(session);
            });
        }
    }

    @Test
    void jsonFilterSerialization() {
        ChatClient chatClient = new GPT4oClient();
        String clientUID = chatClient.getClientUID();
        assertNotNull(chatClient);
        // add some prohibited tags
        chatClient.addTags(new String[]{"todo", "todo", "finished", "fuck"});
        chatClient.saveClient();
        JSONObject session = Utils.parseJSON(clientUID);
        assertNotNull(session);
        assertInstanceOf(JSONArray.class, session.get("tags"));
        JSONArray tags = (JSONArray) session.get("tags");
        List<Object> tagsList = tags.toList();
        assertEquals(tagsList, new ArrayList<>(Arrays.asList("todo", "finished")));
        // add prohibited description
        chatClient.setDescription("This is a damn good session");
        chatClient.saveClient();
        session = Utils.parseJSON(clientUID);
        assertNotNull(session);
        assertInstanceOf(String.class, session.get("description"));
        String description = session.getString("description");
        assertEquals("This is a **** good session", description);
    }

    @Test
    void jsonFilterDeserialization() {
        for (int i = 1; i <= 2; i++) {
            JSONObject session = getJsonResource("JsonFilter" + i);
            assertNotNull(session);
            assertThrows(JsonFilterException.class, () -> {
                ChatClient chatClient = new GPT4oClient(session);
            });
        }
    }

    @Test
    void jsonIgnoreSerialization() {
        ChatClient chatClient = new GPT4oClient();
        String clientUID = chatClient.getClientUID();
        assertNotNull(chatClient);
        chatClient.saveClient();
        JSONObject session = Utils.parseJSON(clientUID);
        assertNotNull(session);
        // check if the ignored fields are not saved
        assertFalse(session.has("systemPrompt"));
        assertFalse(session.has("replPrompt"));
        assertFalse(session.has("menus"));
    }

    @Test
    void jsonIgnoreDeserialization() {
        JSONObject rawSession = getJsonResource("JsonIgnore");
        for (int i = 1; i <= 4; i++) {
            JSONObject session = getJsonResource("JsonIgnore" + i);
            try {
                ChatClient chatClient = new GPT4oClient(session);
                ChatClient rawChatClient = new GPT4oClient(rawSession);
                // check if the ignored fields are not loaded
                assertEquals(rawChatClient, chatClient);
            } catch (PersistenceException e) {
                assert false;
            }
        }
    }

    @Test
    void jsonRangeCheckDeserialization() {
        for (int i = 1; i <= 10; i++) {
            JSONObject session = getJsonResource("JsonRangeCheck" + i);
            assertNotNull(session);
            assertThrows(JsonRangeCheckException.class, () -> {
                ChatClient chatClient = new GPT4oClient(session);
            });
        }
    }

    @Test
    void jsonSecretDeserialization() {
        for (int i = 1; i <= 2; i++) {
            JSONObject session = getJsonResource("JsonSecret" + i);
            assertNotNull(session);
            assertThrows(JsonSecretException.class, () -> {
                ChatClient chatClient = new GPT4oClient(session);
            });
        }
    }
}
