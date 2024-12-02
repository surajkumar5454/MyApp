package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public class PostingActivity extends AppCompatActivity {

    private TextInputEditText uidInput;
    private MaterialButton searchButton;
    private LinearLayout resultsLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posting_activity);

        // Initialize views
        uidInput = findViewById(R.id.uidInput);
        searchButton = findViewById(R.id.searchButton);
        resultsLayout = findViewById(R.id.resultsLayout);

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Get UID from intent if passed
        String uid = getIntent().getStringExtra("uidno");
        if (uid != null) {
            uidInput.setText(uid);
            executeSearch(uid);
        }

        // Set click listener for search button
        searchButton.setOnClickListener(v -> {
            String uidNo = uidInput.getText().toString().trim();
            if (uidNo.isEmpty()) {
                Toast.makeText(PostingActivity.this, "Please enter a valid UID", Toast.LENGTH_SHORT).show();
                return;
            }
            executeSearch(uidNo);
        });

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void executeSearch(String uid) {
        resultsLayout.removeAllViews();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT DISTINCT j.uidno, j.name, u.unit_nm as unit, r.rnk_nm as rank, " +
                "j.dateofjoin, j.dateofrelv " +
                "FROM joininfo j " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.uidno = ? " +
                "ORDER BY j.dateofjoin DESC",
                new String[]{uid}
            );

            if (cursor.moveToFirst()) {
                // Add header card with name and UID
                String name = cursor.getString(cursor.getColumnIndex("name"));
                MaterialCardView headerCard = createHeaderCard(uid, name);
                resultsLayout.addView(headerCard);

                // Add posting history cards
                do {
                    addPostingCard(cursor);
                } while (cursor.moveToNext());
            } else {
                showNoResultsMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private MaterialCardView createHeaderCard(String uid, String name) {
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

        MaterialTextView nameText = new MaterialTextView(this);
        nameText.setText(name);
        nameText.setTextSize(20);
        nameText.setTextColor(getColor(R.color.card_header_text));
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setGravity(Gravity.CENTER);

        MaterialTextView uidText = new MaterialTextView(this);
        uidText.setText(uid);
        uidText.setTextSize(16);
        uidText.setTextColor(getColor(R.color.card_header_text));
        uidText.setGravity(Gravity.CENTER);
        uidText.setPadding(0, 8, 0, 0);

        contentLayout.addView(nameText);
        contentLayout.addView(uidText);
        headerCard.addView(contentLayout);
        return headerCard;
    }

    private void addPostingCard(Cursor cursor) {
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

        String unit = cursor.getString(cursor.getColumnIndex("unit"));
        String rank = cursor.getString(cursor.getColumnIndex("rank"));
        String dateJoin = cursor.getString(cursor.getColumnIndex("dateofjoin"));
        String dateRelieve = cursor.getString(cursor.getColumnIndex("dateofrelv"));

        addDetailRow(contentLayout, "Unit", unit);
        addDetailRow(contentLayout, "Rank", rank);
        addDetailRow(contentLayout, "Date of Joining", dateJoin);
        if (dateRelieve != null && !dateRelieve.isEmpty()) {
            addDetailRow(contentLayout, "Date of Relieving", dateRelieve);
        }

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
        valueView.setText(value != null ? value : "Not Available");
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
        messageView.setText("No posting records found");
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        messageView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
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
