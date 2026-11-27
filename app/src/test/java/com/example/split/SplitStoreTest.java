package com.example.split;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.split.model.Round;
import com.example.split.model.Transfer;

import org.junit.Test;

/** Kiểm tra phần ghi nhớ "đã nhắc ai rồi" trong SplitStore (tuần 4). */
public class SplitStoreTest {

    /** 3 người mẫu, 지훈 trả 90,000원 cho 1 món cả 3 cùng ăn → 2 người phải gửi. */
    private SplitStore storeWithTwoTransfers() {
        SplitStore s = SplitStore.get();
        s.reset();
        s.startItems();
        Round r = s.rounds.get(0);
        r.payerId = "p1";
        s.addItem(r, "삼겹살", 30000, 3).who.addAll(s.peopleIds());
        s.calculate();
        return s;
    }

    @Test
    public void remainingGoesDownAfterEachReminder() {
        SplitStore s = storeWithTwoTransfers();
        assertEquals(2, s.remainingNudges());

        Transfer first = s.result.transfers.get(0);
        s.markNudged(first);
        assertTrue(s.isNudged(first));
        assertEquals(1, s.remainingNudges());

        s.markNudged(s.result.transfers.get(1));
        assertEquals(0, s.remainingNudges());
    }

    @Test
    public void newResultOrResetClearsReminders() {
        SplitStore s = storeWithTwoTransfers();
        Transfer first = s.result.transfers.get(0);
        s.markNudged(first);

        s.calculate();                    // tính lại → chưa nhắc ai
        assertEquals(2, s.remainingNudges());

        s.markNudged(s.result.transfers.get(0));
        s.reset();                        // 새 모임 만들기
        assertEquals(0, s.remainingNudges());
        assertFalse(s.nudged.contains(SplitStore.nudgeKey(first)));
    }
}