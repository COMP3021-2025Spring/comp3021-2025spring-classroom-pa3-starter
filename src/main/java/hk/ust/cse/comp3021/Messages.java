/*
 * Copyright (c) 2025.
 * Xiang Chen xchenht@connect.ust.hk
 * This project is developed only for HKUST COMP3021 Programming Assignment
 */

package hk.ust.cse.comp3021;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Messages consists of a list of {@link Message}
 */
public class Messages implements Serializable {
    /**
     * List of {@link Message}
     */
    List<Message> messageList = new ArrayList<>();

    /**
     * Add a {@link Message} to the list
     *
     * @param role    role in LLM: system, user, assistant
     * @param content content of the message
     */
    public void addMessage(String role, String content) {
        messageList.add(new Message(role, content));
    }

    /**
     * Add a {@link Message} to the list
     * @param role role in LLM: system, user, assistant
     * @param content content of the message
     * @param tokens number of tokens
     */
    public void addMessage(String role, String content, int tokens) {
        messageList.add(new Message(role, content, tokens));
    }

    /**
     * Get the last message
     * @return the last message
     */
    public Message getLastMessage() {
        return messageList.get(messageList.size() - 1);
    }

    /**
     * Convert the messages to JSON format used in POST request
     *
     * @return the JSON string
     */
    public JSONArray toPOSTData() {
        JSONArray messageList = new JSONArray();
        for (Message message : this.messageList) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("role", message.role);
            messageJson.put("content", message.content);
            messageList.put(messageJson);
        }
        return messageList;
    }

    /**
     * Convert the messages to JSON format used in persistence
     *
     * @return the JSON string
     */
    @Override
    public JSONObject toJSON() {
        JSONObject messagesJson = new JSONObject();
        JSONArray messageList = new JSONArray();
        for (Message message : this.messageList) {
            messageList.put(message.toJSON());
        }
        messagesJson.put("contents", messageList);
        return messagesJson;
    }

    @Override
    public void fromJSON(JSONObject jsonObject) {
        JSONArray messagesJson = jsonObject.getJSONArray("contents");
        for (int i = 0; i < messagesJson.length(); i++) {
            JSONObject messageJson = messagesJson.getJSONObject(i);
            Message message = new Message();
            message.fromJSON(messageJson);
            messageList.add(message);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Message message : messageList) {
            sb.append(message).append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Messages messages = (Messages) o;
        return messageList.equals(messages.messageList);
    }

    @Override
    public int hashCode() {
        return messageList.hashCode();
    }
}
