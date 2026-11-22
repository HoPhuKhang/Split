package com.example.split;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.split.logic.Money;
import com.example.split.logic.Settlement;
import com.example.split.model.MenuItem;
import com.example.split.model.Person;
import com.example.split.model.Round;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Màn 4: ai ăn món gì. Hai cách gán:
 *  - Cọ (chạm): chọn một người ở trên → chạm món nào thì bật/tắt người đó cho món đó.
 *  - Kéo thả: nhấn giữ thẻ món → kéo lên chip của một người → thả.
 * Món của 차 nào chỉ gán được cho người có mặt ở 차 đó (오류 예방).
 */
public class AssignActivity extends BaseActivity {

    private static final String HINT = "사람을 고르고, 그 사람이 먹은 메뉴를 눌러요. 메뉴를 사람에게 끌어다 놓아도 돼요.";

    private String brushId = null;          // người đang được chọn làm "cọ"
    private final Handler handler = new Handler(Looper.getMainLooper());

    private ChipGroup chipsBrush;
    private TextView textHint;
    private LinearLayout listRounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign);
        SplitStore s = store();
        if (goHomeIf(s.mode == null || s.mode == SplitStore.Mode.EQUAL || s.rounds.isEmpty())) return;
        setupScreen("누가 뭘 먹었나요?", s.mealName);

        chipsBrush = findViewById(R.id.chips_brush);
        textHint = findViewById(R.id.text_hint);
        listRounds = findViewById(R.id.list_rounds);

        findViewById(R.id.btn_result).setOnClickListener(v -> showResult());
        render();
    }

    private void render() {
        renderBrush();
        renderRounds();
    }

    /* ---------- Thanh chọn người (cũng là chỗ thả món) ---------- */

    private void renderBrush() {
        chipsBrush.removeAllViews();
        for (Person p : store().people) {
            Chip chip = Ui.personChip(this, p, p.id.equals(brushId));
            chip.setOnClickListener(v -> {
                brushId = p.id.equals(brushId) ? null : p.id;   // chạm lần nữa để bỏ chọn
                render();
            });
            chip.setOnDragListener((v, event) -> dropOnPerson(v, event, p));
            chipsBrush.addView(chip);
        }
    }

    /** Nhận món được kéo tới. Dữ liệu kéo theo có dạng "chỉ_số_차:id_món". */
    private boolean dropOnPerson(View chip, DragEvent event, Person p) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;                                   // đăng ký: chip này nhận được món
            case DragEvent.ACTION_DRAG_ENTERED:
                chip.setScaleX(1.1f);
                chip.setScaleY(1.1f);
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
            case DragEvent.ACTION_DRAG_ENDED:
                chip.setScaleX(1f);
                chip.setScaleY(1f);
                return true;
            case DragEvent.ACTION_DROP:
                String[] parts = event.getClipData().getItemAt(0).getText().toString().split(":");
                Round r = store().rounds.get(Integer.parseInt(parts[0]));
                for (MenuItem it : r.items) {
                    if (it.id.equals(parts[1])) toggle(r, it, p);
                }
                return true;
            default:
                return true;
        }
    }

    /* ---------- Danh sách món theo từng 차 ---------- */

    private void renderRounds() {
        listRounds.removeAllViews();
        for (int i = 0; i < store().rounds.size(); i++) {
            Round r = store().rounds.get(i);
            if (r.items.isEmpty()) continue;
            listRounds.addView(roundHeader(r));
            for (MenuItem it : r.items) {
                listRounds.addView(itemCard(i, r, it));
            }
        }
    }

    /** Tiêu đề 차: "1차 · 지훈 결제 · 지훈, 민지, 서연" */
    private TextView roundHeader(Round r) {
        StringBuilder names = new StringBuilder();
        for (String id : r.attendees) {
            Person p = store().person(id);
            if (p == null) continue;
            if (names.length() > 0) names.append(", ");
            names.append(p.name);
        }
        Person payer = store().person(r.payerId);
        TextView header = new TextView(this);
        header.setText(r.name + " · " + (payer != null ? payer.name : "?") + " 결제 · " + names);
        header.setTextColor(ContextCompat.getColor(this, R.color.ink_muted));
        header.setTextSize(13);
        header.setPadding(0, Ui.dp(this, 16), 0, Ui.dp(this, 6));
        return header;
    }

    private View itemCard(int roundIndex, Round r, MenuItem it) {
        View card = getLayoutInflater().inflate(R.layout.item_assign, listRounds, false);
        ((TextView) card.findViewById(R.id.text_name)).setText(it.qty > 1 ? it.name + " x" + it.qty : it.name);
        ((TextView) card.findViewById(R.id.text_price)).setText(Money.won(it.total()));

        // Ai đã ăn món này: hàng avatar nhỏ
        LinearLayout listWho = card.findViewById(R.id.list_who);
        listWho.removeAllViews();
        if (it.who.isEmpty()) {
            TextView none = new TextView(this);
            none.setText("아직 아무도 안 골랐어요");
            none.setTextColor(ContextCompat.getColor(this, R.color.amber_strong));
            none.setTextSize(13);
            listWho.addView(none);
        }
        for (String id : it.who) {
            Person p = store().person(id);
            if (p == null) continue;
            TextView avatar = Ui.avatar(this, p, 28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 28), Ui.dp(this, 28));
            lp.setMarginEnd(Ui.dp(this, 4));
            listWho.addView(avatar, lp);
        }

        // "모두" = mọi người có mặt ở 차 này cùng ăn; bấm lần nữa để xóa hết
        card.findViewById(R.id.btn_all).setOnClickListener(v -> {
            boolean allIn = it.who.size() == r.attendees.size();
            it.who.clear();
            if (!allIn) it.who.addAll(r.attendees);
            render();
        });

        // Cách 1: chạm (cọ)
        card.setOnClickListener(v -> {
            if (brushId == null) {
                warn("먼저 위에서 사람을 골라 주세요.");
                return;
            }
            toggle(r, it, store().person(brushId));
        });

        // Cách 2: nhấn giữ để kéo
        card.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("menu", roundIndex + ":" + it.id);
            v.startDragAndDrop(data, new View.DragShadowBuilder(v), null, 0);
            return true;
        });
        return card;
    }

    /** Bật / tắt một người cho một món. Người không có mặt ở 차 đó thì báo lỗi. */
    private void toggle(Round r, MenuItem it, Person p) {
        if (p == null) return;
        if (!r.attendees.contains(p.id)) {
            warn(p.name + "님은 " + r.name + "에 없었어요.");
            return;
        }
        if (it.who.contains(p.id)) {
            it.who.remove(p.id);
        } else {
            // Giữ đúng thứ tự trong danh sách người
            it.who.add(p.id);
            it.who.sort((a, b) -> store().peopleIds().indexOf(a) - store().peopleIds().indexOf(b));
        }
        render();
    }

    /** Hiện cảnh báo màu vàng 2 giây rồi trở về dòng gợi ý. */
    private void warn(String message) {
        textHint.setText("⚠ " + message);
        textHint.setTextColor(ContextCompat.getColor(this, R.color.amber_strong));
        textHint.setBackgroundResource(R.drawable.bg_warn);
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            textHint.setText(HINT);
            textHint.setTextColor(ContextCompat.getColor(this, R.color.mint_strong));
            textHint.setBackgroundResource(R.drawable.bg_hint);
        }, 2000);
    }

    /** Còn món chưa gán thì không tính, báo số món còn thiếu. */
    private void showResult() {
        int missing = Settlement.countUnassigned(store().rounds);
        if (missing > 0) {
            warn("배정하지 않은 메뉴가 " + missing + "개 있어요.");
            return;
        }
        store().calculate();
        startActivity(new Intent(this, ResultActivity.class));
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}