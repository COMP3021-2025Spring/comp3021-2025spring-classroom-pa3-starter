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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * GPT4oMiniClient class: <a href="https://itso.hkust.edu.hk/services/it-infrastructure/azure-openai-api-service">API reference</a>
 */
public class GPT4oMiniClient extends ChatClient {

    /**
     * The model name, accessed at top-level repl statically
     */
    public static final String clientName = "GPT-4o-mini";

    /**
     * The maximum tokens
     */
    static final int maxTokens = 8192;

    /**
     * The API URL
     */
    static final String apiURL = "https://hkust.azure-api.net/openai/deployments/gpt-4o-mini/chat/completions?api-version=2024-06-01";

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
            HttpURLConnection conn = getHttpURLConnection();

            messages.addMessage("user", prompt);
            JSONObject responseJSON = sendPOSTRequest(conn);
            int promptTokens = responseJSON.getJSONObject("usage").getInt("prompt_tokens");
            messages.getLastMessage().setTokens(promptTokens - totalPromptTokens - totalCompletionTokens);
            totalPromptTokens += promptTokens;

            String response = responseJSON.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            int completionTokens = responseJSON.getJSONObject("usage").getInt("completion_tokens");
            messages.addMessage("assistant", response, completionTokens);
            totalCompletionTokens += completionTokens;

            return response;
        } catch (Exception e) {
            Utils.printlnError("Query error: " + e.getMessage());
            return "";
        }
    }

    HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL(apiURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("api-key", apiKey);
        conn.setDoOutput(true);
        return conn;
    }

    JSONObject sendPOSTRequest(HttpURLConnection conn) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = getPOSTData().toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder responseBuilder = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            responseBuilder.append(responseLine.trim());
        }
        return new JSONObject(responseBuilder.toString());
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
