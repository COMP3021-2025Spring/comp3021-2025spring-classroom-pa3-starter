/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GPT4oMiniClientTest {
    @Test
    void getClientName() {
        assertEquals("GPT-4o-mini", new GPT4oMiniClient().getClientName());
    }

    @Test
    void query() {
        GPT4oMiniClient client = new GPT4oMiniClient();
        String response = client.query("Hello");
        System.out.println(response);
        assertEquals("Hello! How can I assist you today?", response);
    }

}