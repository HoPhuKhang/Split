package com.example.split.model;

import java.util.ArrayList;
import java.util.List;

/** A dining round (1st round, 2nd round, etc.): who attended, who paid, and what was ordered. */
public class Round {
    public String name;                                   // "1st round"
    public final List<String> attendees = new ArrayList<>(); // IDs of attendees
    public String payerId = null;                         // ID of the person who paid upfront — null = not selected
    public final List<MenuItem> items = new ArrayList<>();

    public Round(String name) {
        this.name = name;
    }

    /** Total bill for this round */
    public int total() {
        int sum = 0;
        for (MenuItem it : items) sum += it.total();
        return sum;
    }
}