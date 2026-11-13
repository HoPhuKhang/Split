package com.example.split;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.split.model.Person;
import com.google.android.material.chip.Chip;

/** Creates reusable small UI components: avatars, person chips, and payer chips. */
public final class Ui {
    private Ui() {}

    /** Converts dp to pixels. */
    public static int dp(Context c, float value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    /** A circular avatar with the person's unique color and the first letter of their name in the center. */
    public static TextView avatar(Context c, Person p, int sizeDp) {
        TextView v = new TextView(c);
        int size = dp(c, sizeDp);
        v.setLayoutParams(new ViewGroup.MarginLayoutParams(size, size));
        v.setGravity(Gravity.CENTER);
        v.setText(p.initial());
        v.setTextColor(Color.WHITE);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeDp * 0.42f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(p.color);
        v.setBackground(circle);
        return v;
    }

    /** Basic chip: rounded corners and no automatic state changes when clicked (the state is updated manually). */
    public static Chip chip(Context c, CharSequence text) {
        Chip chip = new Chip(c);
        chip.setText(text);
        chip.setCheckable(false);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        chip.setChipStrokeWidth(dp(c, 1));
        setColors(chip, ContextCompat.getColor(c, R.color.card),
                ContextCompat.getColor(c, R.color.line),
                ContextCompat.getColor(c, R.color.ink));
        return chip;
    }

    /** Participant chip: selected = person's unique background color + ✓; unselected = white background with gray text. */
    public static Chip personChip(Context c, Person p, boolean selected) {
        Chip chip = chip(c, selected ? "✓ " + p.name : p.name);
        if (selected) {
            setColors(chip, p.color, p.color, Color.WHITE);
        } else {
            setColors(chip, ContextCompat.getColor(c, R.color.card),
                    ContextCompat.getColor(c, R.color.line),
                    ContextCompat.getColor(c, R.color.ink_muted));
        }
        chip.setContentDescription(p.name + (selected ? " 선택됨" : " 선택 안 됨"));
        return chip;
    }

    /** Payer chip ("Who paid?"): colored dot + name; selected = dark background (ink) with white text. */
    public static Chip payerChip(Context c, Person p, boolean selected) {
        SpannableString text = new SpannableString("● " + p.name);
        text.setSpan(new ForegroundColorSpan(p.color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Chip chip = chip(c, text);
        int ink = ContextCompat.getColor(c, R.color.ink);
        if (selected) {
            setColors(chip, ink, ink, Color.WHITE);
        }
        chip.setContentDescription(p.name + (selected ? " 결제함" : ""));
        return chip;
    }

    /** General choice chip (1st/2nd round tabs, menu or call options): selected = dark background. */
    public static Chip choiceChip(Context c, CharSequence text, boolean selected) {
        Chip chip = chip(c, text);
        if (selected) {
            int ink = ContextCompat.getColor(c, R.color.ink);
            setColors(chip, ink, ink, Color.WHITE);
        }
        return chip;
    }

    private static void setColors(Chip chip, int background, int stroke, int text) {
        chip.setChipBackgroundColor(ColorStateList.valueOf(background));
        chip.setChipStrokeColor(ColorStateList.valueOf(stroke));
        chip.setTextColor(text);
    }
}