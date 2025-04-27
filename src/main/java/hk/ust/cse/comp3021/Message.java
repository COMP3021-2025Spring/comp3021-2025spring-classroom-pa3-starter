/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.json.JSONObject;
import java.util.Objects;

/**
 * Message class, consisting of role and content
 */
public class Message implements Serializable {
    String role;
    String content;
    int tokens;

    /**
     * Default constructor of Message
     */
    public Message() {}

    /**
     * Constructor of Message
     *
     * @param role    role in LLM: system, user, assistant
     * @param content content of the message
     */
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * Constructor of Message
     *
     * @param role    role in LLM: system, user, assistant
     * @param content content of the message
     * @param tokens  tokens of the message
     */
    public Message(String role, String content, int tokens) {
        this.role = role;
        this.content = content;
        this.tokens = tokens;
    }

    /**
     * Set the token of message, used for modifying the user token
     *
     * @param tokens tokens of the message
     */
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    /**
     * Convert the object to JSON
     *
     * @return the JSON object
     */
    @Override
    public JSONObject toJSON() {
        JSONObject messageJson = new JSONObject();
        messageJson.put("role", role);
        messageJson.put("content", content);
        messageJson.put("tokens", tokens);
        return messageJson;
    }

    /**
     * Convert the object from JSON
     *
     * @param jsonObject the JSON object
     */
    @Override
    public void fromJSON(JSONObject jsonObject) {
        role = jsonObject.getString("role");
        content = jsonObject.getString("content");
        tokens = jsonObject.getInt("tokens");
    }

    @Override
    public String toString() {
        String prompt = role.equals("user") ? " --> " : " <-- ";
        return Utils.toInfo(role + prompt) + content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return tokens == message.tokens && role.equals(message.role) && content.equals(message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content, tokens);
    }
}
