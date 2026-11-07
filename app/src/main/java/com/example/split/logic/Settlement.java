package com.example.split.logic;

import com.example.split.model.MenuItem;
import com.example.split.model.PaidLine;
import com.example.split.model.Round;
import com.example.split.model.Transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all bill-splitting calculations. This class does NOT depend on Android,
 * so it can be tested independently using unit tests.
 *
 * Concept:
 *   net (balance) = amount paid − personal share of the bill
 *   net > 0  → money to receive
 *   net < 0  → money to send
 */
public final class Settlement {
    private Settlement() {}

    /** Bill-splitting result, used by the results screen. */
    public static class Result {
        public final Map<String, Integer> paid = new LinkedHashMap<>(); // Amount paid by each person
        public final Map<String, Integer> owe = new LinkedHashMap<>();  // Total amount owed by each person
        public final Map<String, Integer> net = new LinkedHashMap<>();  // paid − owe
        public final List<Transfer> transfers = new ArrayList<>();
        public final List<PaidLine> paidBy = new ArrayList<>();
        public int total;
        public int maxTransfers; // Maximum number of transfers if everyone pays each other directly: n(n−1)/2
    }

    /* ---------------- Mode 1: Equal split ---------------- */

    public static Result equal(List<String> peopleIds, int total, String payerId) {
        if (payerId == null) {
            throw new IllegalStateException("결제한 사람을 먼저 골라야 해요.");
        }
        Result res = new Result();
        int n = peopleIds.size();
        int each = total / n;            // The person who paid upfront covers the remainder
        for (String id : peopleIds) {
            res.paid.put(id, 0);
            res.owe.put(id, each);
            res.net.put(id, -each);
        }
        res.paid.put(payerId, total);
        res.owe.put(payerId, total - each * (n - 1));
        res.net.put(payerId, each * (n - 1));

        for (String id : peopleIds) {
            if (!id.equals(payerId) && each > 0) {
                res.transfers.add(new Transfer(id, payerId, each));
            }
        }
        res.paidBy.add(new PaidLine(payerId, "전체 결제", total));
        res.total = total;
        res.maxTransfers = n * (n - 1) / 2;
        return res;
    }

    /* ---------------- Modes 2 & 3: By item / Multiple dining rounds ---------------- */

    /** Counts the number of menu items that have not been assigned to anyone. */
    public static int countUnassigned(List<Round> rounds) {
        int count = 0;
        for (Round r : rounds) {
            for (MenuItem it : r.items) {
                if (it.who.isEmpty()) count++;
            }
        }
        return count;
    }

    /**
     * @param showRoundName true in the group dining mode, so the label displays
     *                      "1st round payment" instead of just "Payment"
     */
    public static Result items(List<String> peopleIds, List<Round> rounds, boolean showRoundName) {
        Result res = new Result();
        Map<String, Double> oweExact = new LinkedHashMap<>();
        for (String id : peopleIds) {
            res.paid.put(id, 0);
            oweExact.put(id, 0.0);
        }

        for (Round r : rounds) {
            if (r.items.isEmpty()) continue;
            if (r.payerId == null) {
                throw new IllegalStateException(r.name + ": 결제한 사람을 먼저 골라야 해요.");
            }
            for (MenuItem it : r.items) {
                if (it.who.isEmpty()) {
                    throw new IllegalStateException("배정하지 않은 메뉴가 있어요: " + it.name);
                }
                double share = (double) it.total() / it.who.size();
                for (String pid : it.who) {
                    oweExact.put(pid, oweExact.get(pid) + share);
                }
            }
            int roundTotal = r.total();
            res.paid.put(r.payerId, res.paid.get(r.payerId) + roundTotal);
            res.paidBy.add(new PaidLine(r.payerId,
                    (showRoundName ? r.name + " " : "") + "결제", roundTotal));
            res.total += roundTotal;
        }

        // Round to the nearest 1 KRW
        int sum = 0;
        String topId = null;
        for (String id : peopleIds) {
            int owe = (int) Math.round(oweExact.get(id));
            int net = (int) Math.round(res.paid.get(id) - oweExact.get(id));
            res.owe.put(id, owe);
            res.net.put(id, net);
            sum += net;
            if (topId == null || net > res.net.get(topId)) topId = id;
        }
        // After rounding, the total net balance must equal zero.
        // Assign any rounding difference to the person who should receive the most.
        if (sum != 0 && topId != null) {
            res.net.put(topId, res.net.get(topId) - sum);
        }

        res.transfers.addAll(minimizeTransfers(res.net));
        int n = peopleIds.size();
        res.maxTransfers = n * (n - 1) / 2;
        return res;
    }

    /**
     * Minimizes the number of money transfers by matching the largest debtors
     * with the largest creditors.
     * (A greedy algorithm — sufficiently effective for groups of 2–8 people.)
     */
    static List<Transfer> minimizeTransfers(Map<String, Integer> net) {
        List<String> debtors = new ArrayList<>();   // People who need to send money
        List<String> creditors = new ArrayList<>(); // People who need to receive money
        List<Integer> debtAmt = new ArrayList<>();
        List<Integer> credAmt = new ArrayList<>();

        // Sort by absolute amount in descending order
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(net.entrySet());
        Collections.sort(entries, (a, b) -> Math.abs(b.getValue()) - Math.abs(a.getValue()));
        for (Map.Entry<String, Integer> e : entries) {
            if (e.getValue() < 0) {
                debtors.add(e.getKey());
                debtAmt.add(-e.getValue());
            } else if (e.getValue() > 0) {
                creditors.add(e.getKey());
                credAmt.add(e.getValue());
            }
        }

        List<Transfer> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            int amount = Math.min(debtAmt.get(i), credAmt.get(j));
            if (amount > 0) {
                result.add(new Transfer(debtors.get(i), creditors.get(j), amount));
            }
            debtAmt.set(i, debtAmt.get(i) - amount);
            credAmt.set(j, credAmt.get(j) - amount);
            if (debtAmt.get(i) == 0) i++;
            if (credAmt.get(j) == 0) j++;
        }
        return result;
    }
}