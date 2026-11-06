package com.example.split.model;

/** A row in the "Who paid?" section: Jihoon · 1st round payment · 58,000 KRW */
public class PaidLine {
    public final String personId;
    public final String label;   // "1st round payment" or "Full payment"
    public final int amount;

    public PaidLine(String personId, String label, int amount) {
        this.personId = personId;
        this.label = label;
        this.amount = amount;
    }
}