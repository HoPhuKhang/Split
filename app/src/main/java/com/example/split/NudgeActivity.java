package com.example.split;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.split.logic.Money;
import com.example.split.logic.Nudge;
import com.example.split.model.Person;
import com.example.split.model.Transfer;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn 6: nhắc tiền nhẹ nhàng. Chọn người phải gửi → chọn giọng điệu (xem trước câu thật)
 * → "보내기" mở bảng chia sẻ của Android (KakaoTalk, tin nhắn…).
 * Tuần 4: người đã được nhắc có dấu ✓, quay lại màn này thì tự chọn người kế tiếp.
 */
public class NudgeActivity extends BaseActivity {

    private final List<Transfer> targets = new ArrayList<>();
    private int selected = 0;
    private Transfer sending = null;   // người vừa mở bảng chia sẻ
    private Nudge.Tone tone = Nudge.Tone.FRIENDLY;

    private ChipGroup chipsTarget;
    private RadioGroup groupTone;

    /** Đóng bảng chia sẻ xong thì ghi lại người đã nhắc và sang màn "보냈어요". Phải khai báo trước onStart(). */
    private final ActivityResultLauncher<Intent> shareLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            r -> {
                if (sending != null) store().markNudged(sending);
                sending = null;
                startActivity(new Intent(this, SentActivity.class));
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nudge);
        if (goHomeIf(store().result == null || store().result.transfers.isEmpty())) return;
        setupScreen("부드럽게 알리기", "Split이 대신 보내서 서로 민망하지 않아요");

        targets.addAll(store().result.transfers);
        chipsTarget = findViewById(R.id.chips_target);
        groupTone = findViewById(R.id.group_tone);
        findViewById(R.id.btn_send).setOnClickListener(v -> send());
    }

    /** Chạy cả lúc mở màn lần đầu và lúc quay lại từ màn "보냈어요". */
    @Override
    protected void onResume() {
        super.onResume();
        if (targets.isEmpty()) return;
        selected = firstNotNudged();
        render();
    }

    /** Người đầu tiên chưa được nhắc; ai cũng nhắc rồi thì giữ người đang chọn. */
    private int firstNotNudged() {
        for (int i = 0; i < targets.size(); i++) {
            if (!store().isNudged(targets.get(i))) return i;
        }
        return Math.min(selected, targets.size() - 1);
    }

    private void render() {
        renderTargets();
        renderTones();
    }

    /** "서연 · 16,000원" — mỗi người phải gửi là một chip. Đã nhắc rồi: "✓ 서연 · 16,000원". */
    private void renderTargets() {
        chipsTarget.removeAllViews();
        for (int i = 0; i < targets.size(); i++) {
            final int index = i;
            Transfer t = targets.get(i);
            Person from = store().person(t.fromId);
            if (from == null) continue;
            String label = from.name + " · " + Money.won(t.amount);
            if (store().isNudged(t)) label = "✓ " + label;
            Chip chip = Ui.choiceChip(this, label, i == selected);
            chip.setOnClickListener(v -> {
                selected = index;
                render();
            });
            chipsTarget.addView(chip);
        }
    }

    /** 3 lựa chọn giọng điệu, mỗi lựa chọn kèm câu xem trước với tên và số tiền thật. */
    private void renderTones() {
        groupTone.setOnCheckedChangeListener(null);   // tránh gọi lại khi đang vẽ
        groupTone.removeAllViews();
        Transfer t = targets.get(selected);
        Person from = store().person(t.fromId);
        String name = from != null ? from.name : "";

        for (Nudge.Tone option : Nudge.Tone.values()) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setTag(option);
            rb.setText(Nudge.label(option) + "\n\"" + Nudge.body(option, name, t.amount) + "\"");
            rb.setTextColor(ContextCompat.getColor(this, R.color.ink));
            rb.setTextSize(14);
            rb.setBackgroundResource(R.drawable.bg_card);
            int pad = Ui.dp(this, 14);
            rb.setPadding(pad, pad, pad, pad);
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, Ui.dp(this, 10));
            groupTone.addView(rb, lp);
            if (option == tone) rb.setChecked(true);
        }

        groupTone.setOnCheckedChangeListener((group, checkedId) -> {
            View checked = group.findViewById(checkedId);
            if (checked != null) tone = (Nudge.Tone) checked.getTag();
        });
    }

    /** Mở bảng chia sẻ của Android với câu đã chọn. */
    private void send() {
        Transfer t = targets.get(selected);
        Person from = store().person(t.fromId);
        Person to = store().person(t.toId);
        if (from == null || to == null) return;

        String text = Nudge.message(tone, from.name, t.amount, to.name, store().mealName);
        sending = t;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        shareLauncher.launch(Intent.createChooser(send, "알림 보내기"));
    }
}