package com.example.split;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Màn 7: đã gửi.
 * Tuần 4: nếu còn người chưa được nhắc thì hiện "다음 사람에게 보내기" để quay lại màn 6.
 * "새 모임 만들기" xóa dữ liệu cũ và quay về màn 1, đóng hết các màn cũ.
 */
public class SentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent);
        setupScreen(null, null);

        int remaining = store().remainingNudges();
        TextView textRemaining = findViewById(R.id.text_remaining);
        View btnNextPerson = findViewById(R.id.btn_next_person);
        if (remaining > 0) {
            textRemaining.setText("아직 " + remaining + "명에게 보내지 않았어요.");
            btnNextPerson.setVisibility(View.VISIBLE);
        } else {
            textRemaining.setText("모두에게 알림을 보냈어요.");
            btnNextPerson.setVisibility(View.GONE);
        }

        // Quay lại màn 6; màn 6 tự chọn người kế tiếp chưa được nhắc
        btnNextPerson.setOnClickListener(v -> finish());

        findViewById(R.id.btn_new).setOnClickListener(v -> {
            store().reset();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}