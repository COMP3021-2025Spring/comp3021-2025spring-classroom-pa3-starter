/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.client;

import hk.ust.cse.comp3021.ChatClient;
import hk.ust.cse.comp3021.Utils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * GPT4oClient class: <a href="https://itso.hkust.edu.hk/services/it-infrastructure/azure-openai-api-service">API reference</a>
 */
public class GPT4oClient extends ChatClient {

    /**
     * The model name, accessed at top-level repl statically
     */
    public static final String clientName = "GPT-4o";

    /**
     * The maximum tokens
     */
    static final int maxTokens = 8192;

    /**
     * The API URL
     */
    static final String apiURL = "https://hkust.azure-api.net/openai/deployments/gpt-4o/chat/completions?api-version=2024-06-01";

    @Override
    protected String getClientName() {
        return clientName;
    }

    @Override
    protected int getClientMaxTokens() {
        return maxTokens;
    }

    @Override
    public String query(String prompt) {
        try {
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setDoOutput(true);

            messages.addMessage("user", prompt);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = messages.toJSON().toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                responseBuilder.append(responseLine.trim());
            }

            JSONObject responseJSON = new JSONObject(responseBuilder.toString());
            String response = responseJSON.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            messages.addMessage("assistant", response);

            return response;
        } catch (Exception e) {
            Utils.printlnError("Query error: " + e.getMessage());
            return "";
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("maxTokens", maxTokens);
        json.put("apiURL", apiURL);
        return json;
    }

    @Override
    public ChatClient fromJSON(JSONObject json) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
