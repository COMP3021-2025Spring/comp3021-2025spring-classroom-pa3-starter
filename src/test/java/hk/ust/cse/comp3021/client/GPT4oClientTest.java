/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GPT4oClientTest {

    @Test
    void getModelName() {
        assertEquals("GPT-4o", new GPT4oClient().getClientName());
    }

    @Test
    void query() {
        GPT4oClient client = new GPT4oClient();
        String response = client.query("Hello");
        System.out.println(response);
        assertEquals("Hello! How can I assist you today?", response);
    }
}