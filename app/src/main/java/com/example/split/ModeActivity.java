package com.example.split;

import android.content.Intent;
import android.os.Bundle;

/** Màn 2: chọn 1 trong 3 cách chia tiền. */
public class ModeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
        setupScreen("정산 방식", store().mealName + " · " + store().people.size() + "명");

        findViewById(R.id.card_equal).setOnClickListener(v -> {
            store().startEqual();
            startActivity(new Intent(this, EqualActivity.class));
        });
        findViewById(R.id.card_items).setOnClickListener(v -> {
            store().startItems();
            startActivity(new Intent(this, RoundsActivity.class));
        });
        findViewById(R.id.card_hoesik).setOnClickListener(v -> {
            store().startHoesik();
            startActivity(new Intent(this, RoundsActivity.class));
        });
    }
}