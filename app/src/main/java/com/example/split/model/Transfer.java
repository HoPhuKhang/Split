package com.example.split.model;

/** A money transfer: fromId sends the amount to toId. */
public class Transfer {
    public final String fromId;
    public final String toId;
    public final int amount;

    public Transfer(String fromId, String toId, int amount) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
    }
}