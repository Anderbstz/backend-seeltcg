package com.pikacards.chatbot.dto;
public class ChatResponse {
    private String reply;
    public ChatResponse(String reply) { this.reply = reply; }
    public String getReply() { return reply; } public void setReply(String r) { reply = r; }
}
