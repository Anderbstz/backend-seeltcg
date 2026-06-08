package com.pikacards.chatbot.dto;
public class ChatRequest {
    private String message;
    private boolean useFullDb = true;
    public String getMessage() { return message; } public void setMessage(String m) { message = m; }
    public boolean isUseFullDb() { return useFullDb; } public void setUseFullDb(boolean u) { useFullDb = u; }
}
