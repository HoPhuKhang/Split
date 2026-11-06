package com.example.split.model;

import java.util.ArrayList;
import java.util.List;

/** An item in a bill for one dining session. */
public class MenuItem {
    public final String id;      // Unique ID of the item (m1, m2, ...)
    public String name;          // 삼겹살
    public int price;            // Price per serving, in KRW
    public int qty;              // Quantity
    public final List<String> who = new ArrayList<>(); //  IDs of people who ate this item

    public MenuItem(String id, String name, int price, int qty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.qty = qty;
    }

    /** Total amount = price × quantity */
    public int total() {
        return price * qty;
    }
}