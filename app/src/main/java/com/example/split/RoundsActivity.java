package com.example.split;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.split.logic.Money;
import com.example.split.model.MenuItem;
import com.example.split.model.Person;
import com.example.split.model.Round;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn 3b: nhập từng 차 (chế độ 회식) hoặc một hóa đơn (chế độ theo món).
 * Thứ tự trên màn hình = thứ tự suy nghĩ: ai có mặt → ai trả tiền → gọi món gì.
 */
public class RoundsActivity extends BaseActivity {

    /** Menu hay gọi: tên, giá */
    private static final String[][] QUICK_MENUS = {
            {"삼겹살", "15000"}, {"소주", "5000"}, {"맥주", "5000"}, {"공깃밥", "1000"},
            {"김치찌개", "8000"}, {"치킨", "20000"}, {"콜라", "2000"}, {"떡볶이", "12000"}
    };

    private int current = 0;   // đang xem 차 thứ mấy

    private LinearLayout listTabs, listItems;
    private ChipGroup chipsAttendees, chipsPayer;
    private TextView textNeed, textRoundTotal;
    private Button btnAddItem, btnAssign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rounds);
        SplitStore s = store();
        if (goHomeIf(s.mode == null || s.mode == SplitStore.Mode.EQUAL || s.rounds.isEmpty())) return;

        boolean hoesik = s.mode == SplitStore.Mode.HOESIK;
        setupScreen(s.mealName, hoesik ? "차수마다 참석자를 따로 정해요" : "영수증 메뉴를 입력해요");

        listTabs = findViewById(R.id.list_tabs);
        listItems = findViewById(R.id.list_items);
        chipsAttendees = findViewById(R.id.chips_attendees);
        chipsPayer = findViewById(R.id.chips_payer);
        textNeed = findViewById(R.id.text_need);
        textRoundTotal = findViewById(R.id.text_round_total);
        btnAddItem = findViewById(R.id.btn_add_item);
        btnAssign = findViewById(R.id.btn_assign);

        btnAddItem.setOnClickListener(v -> showAddSheet(round()));
        btnAssign.setOnClickListener(v -> startActivity(new Intent(this, AssignActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!store().rounds.isEmpty()) render();
    }

    private Round round() {
        return store().rounds.get(current);
    }

    private void render() {
        renderTabs();
        renderAttendees();
        renderPayer();
        renderItems();
        btnAssign.setEnabled(canAssign());
    }

    /* ---------- Tab 1차 / 2차 / + 차 (chỉ ở chế độ 회식) ---------- */

    private void renderTabs() {
        listTabs.removeAllViews();
        if (store().mode != SplitStore.Mode.HOESIK) {
            listTabs.setVisibility(View.GONE);
            return;
        }
        listTabs.setVisibility(View.VISIBLE);
        for (int i = 0; i < store().rounds.size(); i++) {
            final int index = i;
            Chip tab = Ui.choiceChip(this, store().rounds.get(i).name, i == current);
            tab.setOnClickListener(v -> {
                current = index;
                render();
            });
            addTab(tab);
        }
        Chip add = Ui.chip(this, "+ 차");
        add.setTextColor(ContextCompat.getColor(this, R.color.mint_strong));
        add.setOnClickListener(v -> {
            store().addRound();
            current = store().rounds.size() - 1;
            render();
        });
        addTab(add);
    }

    private void addTab(Chip tab) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(Ui.dp(this, 8));
        listTabs.addView(tab, lp);
    }

    /* ---------- Ai có mặt ở 차 này ---------- */

    private void renderAttendees() {
        chipsAttendees.removeAllViews();
        Round r = round();
        for (Person p : store().people) {
            boolean here = r.attendees.contains(p.id);
            Chip chip = Ui.personChip(this, p, here);
            chip.setOnClickListener(v -> toggleAttendee(r, p));
            chipsAttendees.addView(chip);
        }
    }

    /** Bật / tắt một người. Tắt thì gỡ người đó khỏi các món, và bỏ chọn nếu đang là người trả tiền. */
    private void toggleAttendee(Round r, Person p) {
        if (r.attendees.contains(p.id)) {
            if (r.attendees.size() <= 1) {
                Toast.makeText(this, "한 명 이상 있어야 해요.", Toast.LENGTH_SHORT).show();
                return;
            }
            r.attendees.remove(p.id);
            if (p.id.equals(r.payerId)) r.payerId = null;   // bắt chọn lại người trả
            for (MenuItem it : r.items) it.who.remove(p.id);
        } else {
            // Thêm lại theo đúng thứ tự trong danh sách người
            List<String> ordered = new ArrayList<>();
            for (Person q : store().people) {
                if (q.id.equals(p.id) || r.attendees.contains(q.id)) ordered.add(q.id);
            }
            r.attendees.clear();
            r.attendees.addAll(ordered);
        }
        render();
    }

    /* ---------- Ai đã trả tiền (bắt buộc, không có giá trị mặc định) ---------- */

    private void renderPayer() {
        chipsPayer.removeAllViews();
        Round r = round();
        for (Person p : store().people) {
            if (!r.attendees.contains(p.id)) continue;   // chỉ người có mặt mới trả được
            Chip chip = Ui.payerChip(this, p, p.id.equals(r.payerId));
            chip.setOnClickListener(v -> {
                r.payerId = p.id;
                render();
            });
            chipsPayer.addView(chip);
        }
        boolean noPayer = r.payerId == null;
        textNeed.setVisibility(noPayer ? View.VISIBLE : View.GONE);
        btnAddItem.setEnabled(!noPayer);
    }

    /* ---------- Danh sách món ---------- */

    private void renderItems() {
        listItems.removeAllViews();
        Round r = round();
        if (r.items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("아직 메뉴가 없어요.\n'+ 메뉴 추가'를 눌러 입력해요.");
            empty.setTextColor(ContextCompat.getColor(this, R.color.ink_muted));
            empty.setGravity(android.view.Gravity.CENTER);
            int pad = Ui.dp(this, 24);
            empty.setPadding(0, pad, 0, pad);
            listItems.addView(empty);
        }
        for (MenuItem it : r.items) {
            View row = getLayoutInflater().inflate(R.layout.item_menu_row, listItems, false);
            ((TextView) row.findViewById(R.id.text_name)).setText(it.name);
            TextView qty = row.findViewById(R.id.text_qty);
            qty.setText("x" + it.qty);
            qty.setVisibility(it.qty > 1 ? View.VISIBLE : View.GONE);
            ((TextView) row.findViewById(R.id.text_price)).setText(Money.won(it.total()));
            row.findViewById(R.id.btn_delete).setOnClickListener(v -> {
                r.items.remove(it);
                render();
            });
            listItems.addView(row);
        }
        textRoundTotal.setText(r.name + " 합계  " + Money.won(r.total()));
    }

    /** Sang màn gán món khi: có ít nhất 1 món, và mọi 차 có món đều đã chọn người trả. */
    private boolean canAssign() {
        int itemCount = 0;
        for (Round r : store().rounds) {
            if (r.items.isEmpty()) continue;
            if (r.payerId == null) return false;
            itemCount += r.items.size();
        }
        return itemCount > 0;
    }

    /* ---------- Bottom sheet thêm món ---------- */

    private void showAddSheet(Round r) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.sheet_add_item, null);
        sheet.setContentView(v);

        EditText editName = v.findViewById(R.id.edit_item_name);
        TextView textPrice = v.findViewById(R.id.text_price);
        TextView textQty = v.findViewById(R.id.text_qty);
        ChipGroup chipsQuick = v.findViewById(R.id.chips_quick);

        // Java không cho lambda gán lại biến bên ngoài, nên dùng mảng 1 phần tử
        final int[] price = {0};
        final int[] qty = {1};
        Runnable refresh = () -> {
            textPrice.setText(Money.won(price[0]));
            textQty.setText("x" + qty[0]);
        };

        for (String[] menu : QUICK_MENUS) {
            int menuPrice = Integer.parseInt(menu[1]);
            Chip chip = Ui.chip(this, menu[0] + " " + (menuPrice / 1000) + "천");
            chip.setOnClickListener(c -> {
                editName.setText(menu[0]);
                price[0] = menuPrice;
                refresh.run();
            });
            chipsQuick.addView(chip);
        }

        Numpad.build(v.findViewById(R.id.numpad), key -> {
            price[0] = Numpad.apply(price[0], key);
            refresh.run();
        });
        v.findViewById(R.id.btn_minus).setOnClickListener(c -> {
            if (qty[0] > 1) qty[0]--;
            refresh.run();
        });
        v.findViewById(R.id.btn_plus).setOnClickListener(c -> {
            if (qty[0] < 99) qty[0]++;
            refresh.run();
        });

        v.findViewById(R.id.btn_add).setOnClickListener(c -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                editName.setError("메뉴 이름을 입력해 주세요.");
                return;
            }
            if (price[0] <= 0) {
                Toast.makeText(this, "가격을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            store().addItem(r, name, price[0], qty[0]);
            sheet.dismiss();
            render();
        });

        refresh.run();
        sheet.show();
    }
}