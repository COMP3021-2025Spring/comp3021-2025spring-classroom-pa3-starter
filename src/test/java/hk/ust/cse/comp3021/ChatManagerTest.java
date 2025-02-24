/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatManagerTest {

    @Test
    void showChatClients() {
        assertEquals("GPT-4o", ChatManager.getChatClients().trim());
    }

    @Test
    void getChatClient() throws ChatManager.InvalidClientNameException {
        ChatClient client = ChatManager.getChatClient("GPT-4o");
        assertEquals("GPT-4o", client.getClientName());
        assertThrows(ChatManager.InvalidClientNameException.class, () -> ChatManager.getChatClient("a fake name"));
    }
}