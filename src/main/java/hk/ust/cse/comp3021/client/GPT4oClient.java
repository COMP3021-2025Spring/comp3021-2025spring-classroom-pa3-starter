/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021.client;

import hk.ust.cse.comp3021.ChatClient;
import hk.ust.cse.comp3021.Utils;
import hk.ust.cse.comp3021.annotation.JsonCheck;
import hk.ust.cse.comp3021.exception.PersistenceException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * GPT4oClient class:
 * <a href="https://itso.hkust.edu.hk/services/it-infrastructure/azure-openai-api-service">API reference</a>
 */
public class GPT4oClient extends ChatClient {

    /**
     * The model name, accessed at top-level repl statically
     */
    @JsonCheck
    public static final String clientName = "GPT-4o";

    /**
     * The maximum tokens
     */
    @JsonCheck
    static final int maxTokens = 8192;

    /**
     * The API URL
     */
    @JsonCheck
    static final String apiURL = "https://hkust.azure-api.net/openai/deployments/gpt-4o/chat/completions?api-version" +
            "=2024-06-01";

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

            // the returned prompt token is actually the total prompt tokens
            int promptTokens = responseJSON.getJSONObject("usage").getInt("prompt_tokens");
            int currPromptTokens = promptTokens - totalPromptTokens - totalCompletionTokens;
            messages.getLastMessage().setTokens(currPromptTokens);
            totalPromptTokens += currPromptTokens;

            String response =
                    responseJSON.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
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

    /**
     * Default constructor
     */
    public GPT4oClient() {
        super();
    }

    /**
     * Constructor of ChatClient when deserializing from JSON
     *
     * @param session the JSON object
     * @throws PersistenceException if the JSON object is invalid when checking the annotations
     */
    public GPT4oClient(JSONObject session) throws PersistenceException {
        super(session);
    }
}
