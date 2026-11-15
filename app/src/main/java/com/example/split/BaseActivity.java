package com.example.split;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Base class for all Split app screens.
 * - Enables edge-to-edge display and applies padding to prevent the status bar
 *   and keyboard from covering the content.
 * - Sets the title, subtitle, and back button (‹) in view_top_bar.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }

    /** Shared data used throughout the app. */
    protected SplitStore store() {
        return SplitStore.get();
    }

    /**
     * Call immediately after setContentView().
     * title / subtitle = null if the screen does not have a top bar.
     */
    protected void setupScreen(String title, String subtitle) {
        // android.R.id.content = the container holding the screen's layout;
        // its first child is the outermost view of the layout.
        ViewGroup content = findViewById(android.R.id.content);
        View root = content.getChildAt(0);
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            // systemBars = status bar + navigation bar, ime = keyboard
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });

        TextView textTitle = findViewById(R.id.text_title);
        if (textTitle != null) textTitle.setText(title);

        TextView textSubtitle = findViewById(R.id.text_subtitle);
        if (textSubtitle != null) {
            textSubtitle.setText(subtitle);
            textSubtitle.setVisibility(subtitle == null ? View.GONE : View.VISIBLE);
        }

        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());
    }

    /**
     * If the data is missing (e.g., Android terminates the app while it is in the background),
     * navigate back to Screen 1.
     *
     * @return true if navigation to Screen 1 has been performed.
     *         The current screen should not perform any further actions in this case.
     */
    protected boolean goHomeIf(boolean dataMissing) {
        if (!dataMissing) return false;
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        return true;
    }
}