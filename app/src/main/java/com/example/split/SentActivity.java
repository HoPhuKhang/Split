package com.example.split;

import android.content.Intent;
import android.os.Bundle;

/** Màn 7: đã gửi. "새 모임 만들기" xóa dữ liệu cũ và quay về màn 1, đóng hết các màn cũ. */
public class SentActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent);
        setupScreen(null, null);

        findViewById(R.id.btn_new).setOnClickListener(v -> {
            store().reset();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}