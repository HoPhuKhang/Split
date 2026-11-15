package com.example.split;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

import com.example.split.model.Person;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Screen 1: Enter the meal name and add or remove participants. */
public class MainActivity extends BaseActivity {

    private static final int MIN_PEOPLE = 2;
    private static final int MAX_PEOPLE = 8;

    private EditText editMeal;
    private ChipGroup chipsPeople;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupScreen(null, null);   // Screen 1 does not have a top bar.

        editMeal = findViewById(R.id.edit_meal);
        chipsPeople = findViewById(R.id.chips_people);
        btnNext = findViewById(R.id.btn_next);

        editMeal.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateNext();
            }
        });

        btnNext.setOnClickListener(v -> {
            store().mealName = editMeal.getText().toString().trim();
            startActivity(new Intent(this, ModeActivity.class));
        });
    }

    /** Re-render the screen every time the user returns to it (e.g., after tapping "Create a New Group"). */
    @Override
    protected void onResume() {
        super.onResume();
        editMeal.setText(store().mealName);
        renderPeople();
    }

    /** Remove all existing chips and recreate them from store().people. */
    private void renderPeople() {
        chipsPeople.removeAllViews();
        boolean canRemove = store().people.size() > MIN_PEOPLE;

        for (Person p : store().people) {
            Chip chip = Ui.personChip(this, p, true);
            if (canRemove) {                 // Allow removal only when there are more than 2 participants.
                chip.setCloseIconVisible(true);
                chip.setCloseIconContentDescription(p.name + " 삭제");
                chip.setOnCloseIconClickListener(v -> {
                    store().removePerson(p.id);
                    renderPeople();
                });
            }
            chipsPeople.addView(chip);
        }

        if (store().people.size() < MAX_PEOPLE) {
            Chip add = Ui.chip(this, "+ 사람 추가");
            add.setOnClickListener(v -> showAddDialog());
            chipsPeople.addView(add);
        }
        updateNext();
    }

    /** Enable the "Next" button only when a meal name is entered and there are at least 2 participants. */
    private void updateNext() {
        boolean hasName = editMeal.getText().toString().trim().length() > 0;
        btnNext.setEnabled(hasName && store().people.size() >= MIN_PEOPLE);
    }

    /** Display a dialog for entering a new participant's name.
     * If the name is empty or duplicated, show an error without closing the dialog.
     */
    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint("이름");
        input.setSingleLine(true);
        FrameLayout box = new FrameLayout(this);
        int pad = Ui.dp(this, 20);
        box.setPadding(pad, Ui.dp(this, 8), pad, 0);
        box.addView(input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("사람 추가")
                .setView(box)
                .setNegativeButton("취소", null)
                .setPositiveButton("추가", null)   // Attach the listener afterward to control whether the dialog should close.
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                input.setError("이름을 입력해 주세요.");
            } else if (!store().addPerson(name)) {
                input.setError("이미 있는 이름이에요.");
            } else {
                dialog.dismiss();
                renderPeople();
            }
        }));
        dialog.show();
    }
}