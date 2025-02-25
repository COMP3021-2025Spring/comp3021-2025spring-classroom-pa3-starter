/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class Task1Test {
    ChatClient chatClient;

    @Test
    void showChatClients() {
        // you should implement at least the GPT-4o ChatClient
        assertTrue(ChatManager.getChatClientNames().contains("GPT-4o"));
    }

    static boolean isValidFormat(String str) {
        String[] parts = str.split("_");
        if (parts.length != 3) {
            return false;
        }
        String dateTimePart = parts[1] + "_" + parts[2];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        try {
            LocalDateTime.parse(dateTimePart, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Mocking the default key
     */
    static void createDefaultKey() {
        Path directoryPath = Paths.get("keys");
        Path filePath = directoryPath.resolve("GPT-4o" + ".txt");

        try {
            // Create the directory if it does not exist
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Create the file if it does not exist
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                System.out.println("File created: " + filePath);
            } else {
                System.out.println("File already exists: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() throws ChatManager.InvalidClientNameException {
        createDefaultKey();
        // you should implement at least the GPT-4o ChatClient
        chatClient = ChatManager.getChatClient("GPT-4o");
    }

    @Test
    void getChatClient() {
        // invalid client name
        assertThrows(ChatManager.InvalidClientNameException.class, () -> ChatManager.getChatClient("a fake name"));
        assertEquals("GPT-4o", chatClient.getClientName());
        assertTrue(isValidFormat(chatClient.getClientUID()));
        assertTrue(chatClient.getClientMaxTokens() > 0);
    }

    @Test
    void replChatClient() {
        String response = chatClient.query("Hello!");
        // you may change this to your observed response, provided with a screenshot
        assertEquals("Hello! How can I assist you today?", response);
    }

    @Test
    void replChatClientConversation1() {
        String response = chatClient.query("Hello!");
        // you may change this to your observed response, provided with a screenshot
        assertEquals("Hello! How can I assist you today?", response);

        // the client should preserve the conversation
        response = chatClient.query("Did I greet with you?");
        // you may change this to your observed response, provided with a screenshot
        assertTrue(response.contains("Yes"));
        // the client should preserve the conversation
        response = chatClient.query("Did I ask you about your name?");
        // you may change this to your observed response, provided with a screenshot
        assertTrue(response.contains("No"));
    }

    @Test
    void replChatClientConversation2() {
        String response = chatClient.query("Who are you?");
        // you may change this to your observed response, provided with a screenshot
        assertTrue(response.contains("AI assistant"));

        // the client should preserve the conversation
        response = chatClient.query("Did you said you are an AI assistant?");
        // you may change this to your observed response, provided with a screenshot
        assertTrue(response.contains("Yes"));
        // the client should preserve the conversation
        response = chatClient.query("Did you said you are a cat?");
        // you may change this to your observed response, provided with a screenshot
        assertTrue(response.contains("No"));
    }
}
