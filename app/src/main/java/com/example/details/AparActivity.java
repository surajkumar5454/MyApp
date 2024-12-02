package com.example.details;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class AparActivity extends AppCompatActivity {

    private TextInputEditText uidInput;
    private MaterialButton searchButton;
    private LinearLayout resultsLayout;
    private CircularProgressIndicator progressBar;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apar_activity);

        // Initialize views
        uidInput = findViewById(R.id.uidInput);
        searchButton = findViewById(R.id.searchButton);
        resultsLayout = findViewById(R.id.detailsLayout);
        progressBar = findViewById(R.id.progressBar);

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
            new QueryDatabaseTask().execute(uid);
        }

        // Set click listener for search button
        searchButton.setOnClickListener(v -> {
            String uidNo = uidInput.getText().toString().trim();
            if (uidNo.isEmpty()) {
                Toast.makeText(AparActivity.this, "Please enter a valid UID", Toast.LENGTH_SHORT).show();
                return;
            }
            new QueryDatabaseTask().execute(uidNo);
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class QueryDatabaseTask extends AsyncTask<String, Void, List<String[]>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            resultsLayout.removeAllViews();
        }

        @Override
        protected List<String[]> doInBackground(String... params) {
            List<String[]> results = new ArrayList<>();
            String values = params[0];
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(
                    "SELECT DISTINCT p.uidno, p.name, u.unit_nm, r.rnk_nm, r.brn_nm, " +
                    "dateFrom, dateTo, grading, numericGrad, adverse, remark, integrity " +
                    "FROM parmanentinfo p, tbl_apar t, joininfo j, unitdep u, rnk_brn_mas r " +
                    "WHERE p.uidno=t.uidno AND p.uidno=j.uidno AND j.rank=r.rnk_cd " +
                    "AND j.branch=r.brn_cd AND u.unit_cd=j.unit AND j.dateofrelv IS NULL " +
                    "AND p.uidno LIKE ? ORDER BY dateFrom DESC",
                    new String[]{"%" + values + "%"}
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String[] row = new String[12];
                        for (int i = 0; i < row.length; i++) {
                            row[i] = cursor.getString(i);
                        }
                        results.add(row);
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) cursor.close();
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<String[]> results) {
            progressBar.setVisibility(View.GONE);

            if (results.isEmpty()) {
                showNoResultsMessage();
                return;
            }

            // Add header card with name and basic info
            String[] firstRow = results.get(0);
            addHeaderCard(firstRow[1], firstRow[0], firstRow[2], firstRow[3] + " (" + firstRow[4] + ")");

            // Add APAR cards
            for (String[] row : results) {
                addAparCard(row);
            }
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

    private void addAparCard(String[] row) {
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

        addDetailRow(contentLayout, "Period", row[5] + " to " + row[6]);
        addDetailRow(contentLayout, "Grading", row[7]);
        addDetailRow(contentLayout, "Numerical Grade", row[8]);
        addDetailRow(contentLayout, "Adverse Remarks", row[9]);
        addDetailRow(contentLayout, "Remarks", row[10]);
        addDetailRow(contentLayout, "Integrity", row[11]);

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
        messageView.setText("No APAR records found");
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
