package com.example.split;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/** A 3×4 numeric keypad for entering amounts without using the phone's keyboard. */
public final class Numpad {
    private Numpad() {}

    /** Notifies the screen which key has just been pressed. */
    public interface OnKey {
        void onKey(String key);
    }

    public static final String BACK = "⌫";
    public static final String[] KEYS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "000", "0", BACK};

    /** Maximum limit: 9,999,999 KRW to prevent the number from overflowing the screen. */
    public static final int MAX = 9_999_999;

    /**
     * Calculates the new number after a key is pressed.
     * Does not depend on any Android components, so it can be tested independently.
     * Numpad.apply(15, "000") → 15000, Numpad.apply(1234, "⌫") → 123
     */
    public static int apply(int value, String key) {
        long next;
        if (BACK.equals(key)) {
            next = value / 10;
        } else if ("000".equals(key)) {
            next = (long) value * 1000;
        } else {
            next = (long) value * 10 + Integer.parseInt(key);
        }
        return next > MAX ? value : (int) next;
    }

    /** Creates 12 keys inside an existing GridLayout from the layout file. */
    public static void build(GridLayout grid, OnKey listener) {
        Context c = grid.getContext();
        grid.removeAllViews();
        grid.setColumnCount(3);
        int gap = Ui.dp(c, 4);
        for (String key : KEYS) {
            TextView button = new TextView(c);
            button.setText(key);
            button.setGravity(Gravity.CENTER);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            button.setTextColor(ContextCompat.getColor(c, R.color.ink));
            button.setBackgroundResource(R.drawable.bg_card);
            button.setContentDescription(BACK.equals(key) ? "지우기" : key);
            button.setOnClickListener(v -> listener.onKey(key));

            // Each column has a weight of 1, making all three columns equally wide.
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.height = Ui.dp(c, 52);
            lp.setMargins(gap, gap, gap, gap);
            grid.addView(button, lp);
        }
    }
}