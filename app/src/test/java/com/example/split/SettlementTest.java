package com.example.split;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.example.split.logic.Settlement;
import com.example.split.model.MenuItem;
import com.example.split.model.Round;
import com.example.split.model.Transfer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Tests bill-splitting calculations. Run: Right-click the file → Run 'SettlementTest'. */
public class SettlementTest {

    private static final String JIHUN = "p1", MINJI = "p2", SEOYEON = "p3";
    private static final List<String> ALL = Arrays.asList(JIHUN, MINJI, SEOYEON);

    /** Creates a menu item and assigns it to the people who ate it. */
    private static MenuItem item(String id, String name, int price, int qty, String... who) {
        MenuItem it = new MenuItem(id, name, price, qty);
        it.who.addAll(Arrays.asList(who));
        return it;
    }

    /** Scenario from the presentation slides: Jihun pays 58,000 KRW in the 1st round,
     * Minji pays 35,000 KRW in the 2nd round, and Seoyeon leaves early. */
    private static List<Round> slideScenario() {
        Round r1 = new Round("1차");
        r1.attendees.addAll(ALL);
        r1.payerId = JIHUN;
        r1.items.add(item("m1", "삼겹살", 15000, 3, JIHUN, MINJI, SEOYEON));
        r1.items.add(item("m2", "소주", 5000, 2, JIHUN, MINJI));
        r1.items.add(item("m3", "공깃밥", 1000, 3, JIHUN, MINJI, SEOYEON));

        Round r2 = new Round("2차");
        r2.attendees.addAll(Arrays.asList(JIHUN, MINJI));
        r2.payerId = MINJI;
        r2.items.add(item("m4", "치킨", 20000, 1, JIHUN, MINJI));
        r2.items.add(item("m5", "맥주", 5000, 3, JIHUN, MINJI));

        return new ArrayList<>(Arrays.asList(r1, r2));
    }

    @Test
    public void slideScenario_twoTransfersToJihun() {
        Settlement.Result res = Settlement.items(ALL, slideScenario(), true);

        assertEquals(93000, res.total);
        assertEquals(19500, (int) res.net.get(JIHUN));    // Amount to receive
        assertEquals(-3500, (int) res.net.get(MINJI));    // Amount to send
        assertEquals(-16000, (int) res.net.get(SEOYEON)); // Amount to send
        assertEquals(38500, (int) res.owe.get(JIHUN));    // Jihoon's share of the bill

        assertEquals(2, res.transfers.size());
        Transfer first = res.transfers.get(0);
        assertEquals(SEOYEON, first.fromId);
        assertEquals(JIHUN, first.toId);
        assertEquals(16000, first.amount);
        Transfer second = res.transfers.get(1);
        assertEquals(MINJI, second.fromId);
        assertEquals(3500, second.amount);

        assertEquals("1차 결제", res.paidBy.get(0).label);
    }

    @Test
    public void noPayer_throws() {
        List<Round> rounds = slideScenario();
        rounds.get(1).payerId = null;   // No payer selected for the 2nd round
        assertThrows(IllegalStateException.class, () -> Settlement.items(ALL, rounds, true));
    }

    @Test
    public void unassignedItem_isCountedAndBlocked() {
        List<Round> rounds = slideScenario();
        rounds.get(0).items.get(2).who.clear();   // No one assigned to the steamed rice (공깃밥)
        assertEquals(1, Settlement.countUnassigned(rounds));
        assertThrows(IllegalStateException.class, () -> Settlement.items(ALL, rounds, true));
    }

    @Test
    public void equalSplit_eachPays29000() {
        Settlement.Result res = Settlement.equal(ALL, 87000, JIHUN);
        assertEquals(2, res.transfers.size());
        for (Transfer t : res.transfers) {
            assertEquals(JIHUN, t.toId);
            assertEquals(29000, t.amount);
        }
        assertEquals(58000, (int) res.net.get(JIHUN));
    }

    @Test
    public void equalSplit_payerTakesRemainder() {
        Settlement.Result res = Settlement.equal(ALL, 10000, MINJI);
        assertEquals(3333, (int) res.owe.get(JIHUN));
        assertEquals(3334, (int) res.owe.get(MINJI));   // The payer covers the remaining 1 KRW
        int sum = 0;
        for (int v : res.net.values()) sum += v;
        assertEquals(0, sum);
    }

    @Test
    public void rounding_netAlwaysSumsToZero() {
        Round r = new Round("Receipt");
        r.attendees.addAll(ALL);
        r.payerId = JIHUN;
        r.items.add(item("m1", "Pizza", 10000, 1, JIHUN, MINJI, SEOYEON)); // 10000 cannot be divided evenly by 3

        Settlement.Result res = Settlement.items(ALL, Arrays.asList(r), false);
        int sum = 0;
        for (int v : res.net.values()) sum += v;
        assertEquals(0, sum);

        int sent = 0;
        for (Transfer t : res.transfers) sent += t.amount;
        assertEquals((int) res.net.get(JIHUN), sent);   // Jihoon receives exactly the total amount sent
        assertEquals("결제", res.paidBy.get(0).label);
    }
    /* ---------- Week 4: extra cases ---------- */

    /** Jihun pays for both rounds alone: the other two each send him their share. */
    @Test
    public void onePersonPaysAllRounds_everyoneSendsToHim() {
        List<Round> rounds = slideScenario();
        rounds.get(1).payerId = JIHUN;   // Jihun also pays the 2nd round

        Settlement.Result res = Settlement.items(ALL, rounds, true);

        assertEquals(93000, (int) res.paid.get(JIHUN));
        assertEquals(54500, (int) res.net.get(JIHUN));   // 93,000 − 38,500
        assertEquals(2, res.paidBy.size());              // "1차 결제" and "2차 결제"
        assertEquals(2, res.transfers.size());
        for (Transfer t : res.transfers) assertEquals(JIHUN, t.toId);
        assertEquals(MINJI, res.transfers.get(0).fromId);     // Largest debt first
        assertEquals(38500, res.transfers.get(0).amount);
        assertEquals(16000, res.transfers.get(1).amount);
    }

    /** 8 people split 100,003 KRW equally: 12,500 each, the payer covers the extra 3 KRW. */
    @Test
    public void equalSplit_eightPeople_oddAmount() {
        List<String> eight = eightPeople();
        Settlement.Result res = Settlement.equal(eight, 100003, "p1");

        assertEquals(7, res.transfers.size());
        for (Transfer t : res.transfers) assertEquals(12500, t.amount);
        assertEquals(12503, (int) res.owe.get("p1"));
        assertEquals(28, res.maxTransfers);              // 8 × 7 / 2
        int sum = 0;
        for (int v : res.net.values()) sum += v;
        assertEquals(0, sum);
    }

    /** 8 people, 2 payers: never more than 7 transfers (n − 1), and the money adds up. */
    @Test
    public void eightPeople_twoPayers_atMostSevenTransfers() {
        List<String> eight = eightPeople();
        Round r = new Round("1차");
        r.attendees.addAll(eight);
        r.payerId = "p1";
        r.items.add(item("m1", "피자", 10000, 8, eight.toArray(new String[0])));   // 80,000
        Round r2 = new Round("2차");
        r2.attendees.addAll(eight);
        r2.payerId = "p2";
        r2.items.add(item("m2", "음료", 2000, 8, eight.toArray(new String[0])));   // 16,000

        Settlement.Result res = Settlement.items(eight, Arrays.asList(r, r2), true);

        assertEquals(96000, res.total);
        assertEquals(68000, (int) res.net.get("p1"));    // 80,000 − 12,000
        assertEquals(4000, (int) res.net.get("p2"));     // 16,000 − 12,000
        assertEquals(7, res.transfers.size());
        int toP1 = 0, toP2 = 0;
        for (Transfer t : res.transfers) {
            if (t.toId.equals("p1")) toP1 += t.amount;
            if (t.toId.equals("p2")) toP2 += t.amount;
        }
        assertEquals(68000, toP1);
        assertEquals(4000, toP2);
    }

    private static List<String> eightPeople() {
        return Arrays.asList("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8");
    }
}