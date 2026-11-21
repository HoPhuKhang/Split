package com.example.split;

import static org.junit.Assert.assertTrue;

import com.example.split.logic.Nudge;

import org.junit.Test;

/** Tests money reminder messages for the correct name, amount, and tone. */
public class NudgeTest {

    @Test
    public void everyToneHasNameAndAmount() {
        for (Nudge.Tone tone : Nudge.Tone.values()) {
            String text = Nudge.body(tone, "서연", 16000);
            assertTrue(text.contains("서연"));
            assertTrue(text.contains("16,000원"));
        }
    }

    @Test
    public void politeUsesNim() {
        assertTrue(Nudge.body(Nudge.Tone.POLITE, "서연", 16000).startsWith("서연님"));
    }

    @Test
    public void messageIsSentInSplitsName() {
        String msg = Nudge.message(Nudge.Tone.FRIENDLY, "민지", 3500, "지훈", "금요일 동아리 회식");
        assertTrue(msg.contains("받는 사람: 지훈"));
        assertTrue(msg.contains("— Split · 금요일 동아리 회식"));
    }
}