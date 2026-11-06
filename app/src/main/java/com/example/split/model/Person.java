package com.example.split.model;

/** A person participating in a meal. */
public class Person {
    public final String id;    // Unique, immutable ID (p1, p2, ...)
    public String name;        // Display name: 지훈
    public final int color;    // Personal color in 0xAARRGGBB format

    public Person(String id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    /** First character of the name, used for the circular avatar: 지훈 → 지 */
    public String initial() {
        if (name == null || name.isEmpty()) return "?";
        return name.substring(0, 1);
    }
}