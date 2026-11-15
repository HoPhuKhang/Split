package com.example.split;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.example.split.logic.Money;
import com.example.split.model.Person;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/** Screen 3a: Split the bill equally. Enter the total amount and MUST select the person who paid. */
public class EqualActivity extends BaseActivity {

    private TextView textAmount, textEach, textNeed;
    private ChipGroup chipsPayer;
    private Button btnResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equal);
        if (goHomeIf(store().mode != SplitStore.Mode.EQUAL)) return;
        setupScreen("빠른 N분의 1", store().mealName);

        textAmount = findViewById(R.id.text_amount);
        textEach = findViewById(R.id.text_each);
        textNeed = findViewById(R.id.text_need);
        chipsPayer = findViewById(R.id.chips_payer);
        btnResult = findViewById(R.id.btn_result);

        Numpad.build(findViewById(R.id.numpad), key -> {
            store().equalTotal = Numpad.apply(store().equalTotal, key);
            render();
        });

        btnResult.setOnClickListener(v -> {
            store().calculate();
            startActivity(new Intent(this, ResultActivity.class));
        });
        render();
    }

    private void render() {
        SplitStore s = store();
        int n = s.people.size();
        textAmount.setText(Money.won(s.equalTotal));
        textEach.setText(n + "명 · 1인당 " + Money.won(s.equalTotal / n));

        chipsPayer.removeAllViews();
        for (Person p : s.people) {
            Chip chip = Ui.payerChip(this, p, p.id.equals(s.equalPayerId));
            chip.setOnClickListener(v -> {
                s.equalPayerId = p.id;
                render();
            });
            chipsPayer.addView(chip);
        }

        // If an amount has been entered but no payer has been selected,
        // display a yellow warning and disable the result button.
        boolean hasTotal = s.equalTotal > 0;
        textNeed.setVisibility(hasTotal && s.equalPayerId == null ? View.VISIBLE : View.GONE);
        btnResult.setEnabled(hasTotal && s.equalPayerId != null);
    }
}