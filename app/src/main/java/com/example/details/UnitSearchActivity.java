package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;

public class UnitSearchActivity extends AppCompatActivity {

    private static final String TAG = "UnitSearchActivity";
    private MaterialAutoCompleteTextView unitAutoCompleteTextView;
    private MaterialAutoCompleteTextView rankSpinner;
    private TextInputLayout rankSpinnerLayout;
    private LinearLayout resultsLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unitsearch_activity);

        // Initialize views
        unitAutoCompleteTextView = findViewById(R.id.unitAutoCompleteTextView);
        rankSpinner = findViewById(R.id.rankSpinner);
        rankSpinnerLayout = findViewById(R.id.rankSpinnerLayout);
        resultsLayout = findViewById(R.id.resultsLayout);

        // Setup toolbar
        findViewById(R.id.topAppBar).setOnClickListener(v -> finish());

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Load unit suggestions
        loadUnitSuggestions();

        // Set unit selection listener
        unitAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUnit = (String) parent.getItemAtPosition(position);
            loadRankValues(selectedUnit);
        });

        // Set rank selection listener
        rankSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRank = (String) parent.getItemAtPosition(position);
            String selectedUnit = unitAutoCompleteTextView.getText().toString();
            queryAndDisplayResults(selectedUnit, selectedRank);
        });
    }

    // Method to load AutoCompleteTextView values (units)
    private void loadUnitSuggestions() {
        List<String> unitNames = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT DISTINCT unit_nm FROM unitdep", null);
            if (cursor.moveToFirst()) {
                do {
                    unitNames.add(cursor.getString(0)); // Add unit names to the list
                } while (cursor.moveToNext());
            }

            // Set adapter for AutoCompleteTextView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, unitNames);
            unitAutoCompleteTextView.setAdapter(adapter);
            unitAutoCompleteTextView.setThreshold(1); // Start showing suggestions after 1 character

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void loadRankValues(String selectedUnit) {
        List<String> ranks = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT DISTINCT r.rnk_nm FROM rnk_brn_mas r " +
                "JOIN joininfo j ON j.rank = r.rnk_cd " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "WHERE u.unit_nm = ? " +
                "ORDER BY r.rnk_nm",
                new String[]{selectedUnit}
            );

            if (cursor.moveToFirst()) {
                do {
                    ranks.add(cursor.getString(0));
                } while (cursor.moveToNext());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    ranks
                );
                rankSpinner.setAdapter(adapter);
                rankSpinnerLayout.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "No ranks found for selected unit", Toast.LENGTH_SHORT).show();
                rankSpinnerLayout.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading ranks", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void queryAndDisplayResults(String selectedUnit, String selectedRank) {
        resultsLayout.removeAllViews();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT DISTINCT p.uidno, p.name, r.rnk_nm, r.brn_nm " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL AND u.unit_nm = ? AND r.rnk_nm = ? " +
                "ORDER BY p.name",
                new String[]{selectedUnit, selectedRank}
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    addPersonCard(cursor);
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

    private void addPersonCard(Cursor cursor) {
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

        String uid = cursor.getString(cursor.getColumnIndex("uidno"));
        addDetailRow(contentLayout, "Name", cursor.getString(cursor.getColumnIndex("name")));
        addDetailRow(contentLayout, "UID", uid);
        addDetailRow(contentLayout, "Rank", cursor.getString(cursor.getColumnIndex("rnk_nm")));
        addDetailRow(contentLayout, "Branch", cursor.getString(cursor.getColumnIndex("brn_nm")));

        card.addView(contentLayout);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("uidno", uid);
            startActivity(intent);
        });

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
        messageView.setText("No personnel found in selected unit and rank");
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        messageView.setTextColor(getColor(R.color.card_value_text));
        messageView.setTextSize(16);
        resultsLayout.addView(messageView);
    }
}
