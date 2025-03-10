package com.example.details;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class LeaveActivity extends AppCompatActivity {

    private static final String TAG = "LeaveActivity";
    private TextInputEditText uidInput;
    private MaterialButton searchButton;
    private LinearLayout resultsLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leave_activity);

        // Initialize views
        uidInput = findViewById(R.id.uidInput);
        searchButton = findViewById(R.id.searchButton);
        resultsLayout = findViewById(R.id.detailsLayout);

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get UID from intent if passed
        String uid = getIntent().getStringExtra("uidno");
        if (uid != null) {
            uidInput.setText(uid);
            executeQuery(uid);
        }

        // Set click listener for search button
        searchButton.setOnClickListener(v -> {
            String uidNo = uidInput.getText().toString().trim();
            if (uidNo.isEmpty()) {
                Toast.makeText(LeaveActivity.this, "Please enter a valid UID", Toast.LENGTH_SHORT).show();
                return;
            }
            executeQuery(uidNo);
        });
    }

    private void executeQuery(String uid) {
        resultsLayout.removeAllViews();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT DISTINCT p.uidno, p.name, u.unit_nm, r.rnk_nm, r.brn_nm, " +
                "l.LeaveType, l.NoOfDays, l.LeaveFrom, l.LeaveTo, l.Ground " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN tblLeave l ON p.uidno = l.uidno " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL AND p.uidno = ? " +
                "ORDER BY l.LeaveFrom DESC",
                new String[]{uid}
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Add header card with name and basic info
                addHeaderCard(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("uidno")),
                    cursor.getString(cursor.getColumnIndex("unit_nm")),
                    cursor.getString(cursor.getColumnIndex("rnk_nm")) + 
                    " (" + cursor.getString(cursor.getColumnIndex("brn_nm")) + ")"
                );

                // Add leave cards
                do {
                    addLeaveCard(cursor);
                } while (cursor.moveToNext());
            } else {
                showNoResultsMessage();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing search", e);
            Toast.makeText(this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void addHeaderCard(String name, String uid, String unit, String rank) {
        MaterialCardView headerCard = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        headerCard.setLayoutParams(cardParams);
        headerCard.setCardElevation(4f);
        headerCard.setRadius(12f);
        headerCard.setContentPadding(24, 24, 24, 24);
        headerCard.setCardBackgroundColor(getColor(R.color.card_header_background));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);

        addHeaderText(contentLayout, name, 20, true);
        addHeaderText(contentLayout, "UID: " + uid, 16, false);
        addHeaderText(contentLayout, unit, 16, false);
        addHeaderText(contentLayout, rank, 16, false);

        headerCard.addView(contentLayout);
        resultsLayout.addView(headerCard);
    }

    private void addHeaderText(LinearLayout parent, String text, int textSize, boolean isBold) {
        MaterialTextView textView = new MaterialTextView(this);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(getColor(R.color.card_header_text));
        if (isBold) textView.setTypeface(null, Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 4, 0, 4);
        parent.addView(textView);
    }

    private void addLeaveCard(Cursor cursor) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setCardElevation(4f);
        card.setRadius(12f);
        card.setContentPadding(24, 24, 24, 24);
        card.setCardBackgroundColor(getColor(R.color.card_content_background));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        addDetailRow(contentLayout, "Leave Type", cursor.getString(cursor.getColumnIndex("LeaveType")));
        addDetailRow(contentLayout, "Number of Days", cursor.getString(cursor.getColumnIndex("NoOfDays")));
        addDetailRow(contentLayout, "From", cursor.getString(cursor.getColumnIndex("LeaveFrom")));
        addDetailRow(contentLayout, "To", cursor.getString(cursor.getColumnIndex("LeaveTo")));
        addDetailRow(contentLayout, "Ground", cursor.getString(cursor.getColumnIndex("Ground")));

        card.addView(contentLayout);
        resultsLayout.addView(card);
    }

    private void addDetailRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        
        MaterialTextView labelView = new MaterialTextView(this);
        labelView.setText(label + ": ");
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        labelView.setTextColor(getColor(R.color.card_label_text));
        labelView.setTextSize(16);

        MaterialTextView valueView = new MaterialTextView(this);
        valueView.setText(value != null && !value.isEmpty() ? value : "Not Available");
        valueView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        valueView.setTextColor(getColor(R.color.card_value_text));
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setTextSize(16);
        valueView.setPadding(16, 0, 0, 0);

        row.addView(labelView);
        row.addView(valueView);
        parent.addView(row);

        // Add a subtle divider
        View divider = new View(this);
        divider.setBackgroundColor(getColor(R.color.divider_color));
        divider.setAlpha(0.1f);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        );
        dividerParams.setMargins(0, 8, 0, 8);
        divider.setLayoutParams(dividerParams);
        parent.addView(divider);
    }

    private void showNoResultsMessage() {
        MaterialTextView messageView = new MaterialTextView(this);
        messageView.setText("No leave records found");
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        messageView.setTextColor(getColor(R.color.card_value_text));
        messageView.setTextSize(16);
        resultsLayout.addView(messageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}
