package com.example.split.logic;

/**
 * Money reminder messages in three different tones.
 * This class does not depend on Android, so it can be tested independently.
 * Every message ends with "Split" so the recipient knows the message
 * was sent by the app, not a personal request for money from you.
 */
public final class Nudge {
    private Nudge() {}

    public enum Tone { FRIENDLY, SIMPLE, POLITE }

    /** Display name of the tone shown on the screen. */
    public static String label(Tone tone) {
        switch (tone) {
            case FRIENDLY: return "친근하게";
            case SIMPLE:   return "간단하게";
            default:       return "정중하게";
        }
    }

    /** Main message content (also used for the preview). */
    public static String body(Tone tone, String name, int amount) {
        String won = Money.won(amount);
        switch (tone) {
            case FRIENDLY:
                return name + ", 오늘 즐거웠어! 편할 때 " + won + " 보내줘. 고마워!";
            case SIMPLE:
                return name + ", 오늘 네 몫은 " + won + "이야. 편할 때 보내줘.";
            default:
                return name + "님, 오늘 정산 금액은 " + won + "입니다. 함께해 주셔서 감사해요. 편하실 때 보내 주세요!";
        }
    }

    /** Complete message to send: message body + recipient + meal name. */
    public static String message(Tone tone, String name, int amount, String toName, String mealName) {
        return body(tone, name, amount)
                + "\n\n받는 사람: " + toName
                + "\n— Split · " + mealName;
    }
}