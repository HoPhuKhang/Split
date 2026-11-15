package com.example.split;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Kiểm tra cách bàn phím số tính ra con số. */
public class NumpadTest {

    @Test
    public void digitsAndThousands() {
        int v = 0;
        v = Numpad.apply(v, "1");
        v = Numpad.apply(v, "5");
        v = Numpad.apply(v, "000");
        assertEquals(15000, v);
    }

    @Test
    public void backspaceRemovesLastDigit() {
        assertEquals(123, Numpad.apply(1234, Numpad.BACK));
        assertEquals(0, Numpad.apply(0, Numpad.BACK));
    }

    @Test
    public void neverGoesOverMax() {
        assertEquals(9_999_999, Numpad.apply(9_999_999, "9"));
        assertEquals(5_000_000, Numpad.apply(5_000, "000"));  // 5,000,000 vẫn trong giới hạn
        assertEquals(50_000, Numpad.apply(50_000, "000"));    // 50,000,000 vượt giới hạn → giữ nguyên
    }
}