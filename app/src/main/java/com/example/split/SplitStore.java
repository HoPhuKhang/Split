package com.example.split;

import com.example.split.logic.Settlement;
import com.example.split.model.MenuItem;
import com.example.split.model.Person;
import com.example.split.model.Round;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores shared data used by all screens (singleton).
 * Every screen accesses it through SplitStore.get(), so there is no need
 * to pass data through Intents.
 */
public final class SplitStore {

    /** Three bill-splitting modes */
    public enum Mode { EQUAL, ITEMS, HOESIK }

    /** A unique color for each person, assigned in order (Split Design System) */
    public static final int[] PERSON_COLORS = {
            0xFF0A7A57, // mint
            0xFFC2255C, // berry
            0xFF3B5BDB, // blue
            0xFF9C5700, // amber
            0xFF6741D9, // violet
            0xFF0B7285  // teal
    };

    private static SplitStore instance;

    public static SplitStore get() {
        if (instance == null) instance = new SplitStore();
        return instance;
    }

    // ----- Data -----
    public String mealName = "금요일 동아리 회식";
    public final List<Person> people = new ArrayList<>();
    public Mode mode = null;

    // Equal-splitting mode
    public int equalTotal = 0;
    public String equalPayerId = null;     // null = no payer selected yet

    // Item-based / group-dining mode
    public final List<Round> rounds = new ArrayList<>();

    // Calculated result; the result screen only reads this value
    public Settlement.Result result = null;

    private int nextPerson = 1;
    private int nextItem = 1;

    private SplitStore() {
        reset();
    }

    /** Resets everything to the initial state: three sample people, nothing selected. */
    public void reset() {
        mealName = "금요일 동아리 회식";
        people.clear();
        rounds.clear();
        mode = null;
        equalTotal = 0;
        equalPayerId = null;
        result = null;
        nextPerson = 1;
        nextItem = 1;
        addPerson("지훈");
        addPerson("민지");
        addPerson("서연");
    }

    // ----- People -----

    /** Adds a person. Returns false if the name is empty or already exists. */
    public boolean addPerson(String name) {
        if (name == null) return false;
        name = name.trim();
        if (name.isEmpty() || findByName(name) != null) return false;
        int color = PERSON_COLORS[(nextPerson - 1) % PERSON_COLORS.length];
        people.add(new Person("p" + nextPerson, name, color));
        nextPerson++;
        return true;
    }

    /** Removes a person and also removes them from all rounds and menu items. */
    public void removePerson(String id) {
        Person p = person(id);
        if (p == null) return;
        people.remove(p);
        if (id.equals(equalPayerId)) equalPayerId = null;
        for (Round r : rounds) {
            r.attendees.remove(id);
            if (id.equals(r.payerId)) r.payerId = null;
            for (MenuItem it : r.items) it.who.remove(id);
        }
    }

    public Person person(String id) {
        for (Person p : people) if (p.id.equals(id)) return p;
        return null;
    }

    public Person findByName(String name) {
        for (Person p : people) if (p.name.equals(name)) return p;
        return null;
    }

    public List<String> peopleIds() {
        List<String> ids = new ArrayList<>();
        for (Person p : people) ids.add(p.id);
        return ids;
    }

    // ----- Start a splitting mode -----

    public void startEqual() {
        mode = Mode.EQUAL;
        equalTotal = 0;
        equalPayerId = null;
        result = null;
    }

    public void startItems() {
        mode = Mode.ITEMS;
        rounds.clear();
        rounds.add(newRound("영수증"));
        result = null;
    }

    public void startHoesik() {
        mode = Mode.HOESIK;
        rounds.clear();
        rounds.add(newRound("1차"));
        result = null;
    }

    /** Creates a new round: everyone is included by default, but NO payer is selected automatically. */
    public Round newRound(String name) {
        Round r = new Round(name);
        r.attendees.addAll(peopleIds());
        r.payerId = null;
        return r;
    }

    /** Adds the next round (2nd, 3rd, etc.) in group-dining mode. */
    public Round addRound() {
        Round r = newRound((rounds.size() + 1) + "차");
        rounds.add(r);
        return r;
    }

    // ----- Menu items -----

    public MenuItem addItem(Round r, String name, int price, int qty) {
        MenuItem it = new MenuItem("m" + nextItem, name, price, qty);
        nextItem++;
        r.items.add(it);
        return it;
    }

    // ----- Calculate the result -----

    /** Calculates and saves the result. Throws IllegalStateException if no payer is selected or any item has no assigned eaters. */
    public Settlement.Result calculate() {
        if (mode == Mode.EQUAL) {
            result = Settlement.equal(peopleIds(), equalTotal, equalPayerId);
        } else {
            result = Settlement.items(peopleIds(), rounds, mode == Mode.HOESIK);
        }
        return result;
    }
}