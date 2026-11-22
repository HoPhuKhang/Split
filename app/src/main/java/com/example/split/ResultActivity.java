package com.example.split;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.split.logic.Money;
import com.example.split.logic.Settlement;
import com.example.split.model.PaidLine;
import com.example.split.model.Person;
import com.example.split.model.Transfer;

/**
 * Màn 5: kết quả. Chỉ ĐỌC store().result và vẽ 3 phần:
 *  1. 누가 결제했나요?  2. 누가 누구에게 보내나요?  3. 사람별 정리 (결제 − 먹은 몫)
 */
public class ResultActivity extends BaseActivity {

    private Settlement.Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        result = store().result;
        if (goHomeIf(result == null)) return;
        setupScreen("정산 결과", store().mealName + " · 총 " + Money.won(result.total));

        renderSummary();
        renderPaid();
        renderTransfers();
        renderPeople();

        View btnNudge = findViewById(R.id.btn_nudge);
        btnNudge.setEnabled(!result.transfers.isEmpty());
        btnNudge.setOnClickListener(v -> startActivity(new Intent(this, NudgeActivity.class)));
    }

    /** "서로 주고받으면 최대 3번이지만, 2번만 보내면 끝나요." */
    private void renderSummary() {
        TextView summary = findViewById(R.id.text_summary);
        int n = result.transfers.size();
        if (n == 0) {
            summary.setText("보낼 돈이 없어요. 모두 먹은 만큼 냈어요.");
        } else {
            summary.setText("송금 횟수 최소화 · 서로 주고받으면 최대 " + result.maxTransfers
                    + "번이지만, " + n + "번만 보내면 끝나요.");
        }
    }

    /* ---------- 1. 누가 결제했나요? ---------- */

    private void renderPaid() {
        LinearLayout list = findViewById(R.id.list_paid);
        list.removeAllViews();
        for (PaidLine line : result.paidBy) {
            Person p = store().person(line.personId);
            if (p == null) continue;
            LinearLayout row = row();
            row.addView(avatar(p));
            row.addView(nameWithSub(p.name, line.label), weight1());
            row.addView(text(Money.won(line.amount), R.color.ink, true));
            list.addView(row);
        }
    }

    /* ---------- 2. 누가 누구에게 보내나요? ---------- */

    private void renderTransfers() {
        LinearLayout list = findViewById(R.id.list_transfers);
        list.removeAllViews();
        if (result.transfers.isEmpty()) {
            list.addView(text("보낼 돈이 없어요.", R.color.ink_muted, false));
        }
        for (Transfer t : result.transfers) {
            Person from = store().person(t.fromId);
            Person to = store().person(t.toId);
            if (from == null || to == null) continue;
            LinearLayout row = row();
            row.addView(avatar(from));
            TextView amount = text(Money.won(t.amount) + " →", R.color.mint_strong, true);
            amount.setGravity(Gravity.CENTER);
            row.addView(amount, weight1());
            row.addView(avatar(to));
            row.setContentDescription(from.name + "이(가) " + to.name + "에게 " + Money.won(t.amount));
            list.addView(row);
        }
    }

    /* ---------- 3. 사람별 정리 ---------- */

    private void renderPeople() {
        LinearLayout list = findViewById(R.id.list_people);
        list.removeAllViews();
        for (Person p : store().people) {
            Integer net = result.net.get(p.id);
            if (net == null) continue;
            int paid = result.paid.get(p.id);
            int owe = result.owe.get(p.id);

            LinearLayout row = row();
            row.addView(nameWithSub(p.name,
                    "결제 " + Money.won(paid) + " − 먹은 몫 " + Money.won(owe)), weight1());

            // Luôn có chữ "받을 돈 / 보낼 돈" bên cạnh màu, để người khó phân biệt màu vẫn hiểu
            TextView status;
            if (net > 0) {
                status = text("받을 돈 " + Money.won(net), R.color.mint_strong, true);
            } else if (net < 0) {
                status = text("보낼 돈 " + Money.won(-net), R.color.berry_strong, true);
            } else {
                status = text("정산 완료", R.color.ink_muted, true);
            }
            row.addView(status);
            list.addView(row);
        }
    }

    /* ---------- Hàm nhỏ tạo view ---------- */

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(Ui.dp(this, 48));
        int pad = Ui.dp(this, 6);
        row.setPadding(0, pad, 0, pad);
        return row;
    }

    private LinearLayout.LayoutParams weight1() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginStart(Ui.dp(this, 10));
        lp.setMarginEnd(Ui.dp(this, 10));
        return lp;
    }

    private TextView avatar(Person p) {
        TextView a = Ui.avatar(this, p, 36);
        a.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 36), Ui.dp(this, 36)));
        return a;
    }

    private TextView text(String s, int colorRes, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(ContextCompat.getColor(this, colorRes));
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    /** Tên (đậm) + một dòng nhỏ màu xám bên dưới. */
    private LinearLayout nameWithSub(String name, String sub) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(text(name, R.color.ink, true));
        TextView small = text(sub, R.color.ink_muted, false);
        small.setTextSize(12);
        box.addView(small);
        return box;
    }
}