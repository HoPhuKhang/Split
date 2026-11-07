package com.example.split.logic;

import java.util.Locale;

/** Formats money: 58000 → "58,000원" */
public final class Money {
    private Money() {}

    public static String won(int amount) {
        return String.format(Locale.KOREA, "%,d원", amount);
    }
}