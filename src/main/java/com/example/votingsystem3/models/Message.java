package com.example.votingsystem3.models;

import java.sql.Timestamp;

public class Message {
    private final int id;
    private final int senderId;
    private final Integer receiverId;
    private final Integer pollingStationId;
    private final String message;
    private final Timestamp timestamp;
    private final String senderEmail;
    private final String receiverEmail;
    private final Integer replyToMessageId;

    public Message(int id, int senderId, Integer receiverId, Integer pollingStationId,
                   String message, Timestamp timestamp, String senderEmail,
                   String receiverEmail, Integer replyToMessageId) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.pollingStationId = pollingStationId;
        this.message = message;
        this.timestamp = timestamp;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.replyToMessageId = replyToMessageId;
    }

    // Getters

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public Integer getReceiverId() { return receiverId; }
    public Integer getPollingStationId() { return pollingStationId; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getSenderEmail() { return senderEmail; }
    public String getReceiverEmail() { return receiverEmail; }
    public Integer getReplyToMessageId() { return replyToMessageId; }

    @Override
    public String toString() {
        return (replyToMessageId != null ? "↳ " : "") + senderEmail + ": " + message;
    }
}
