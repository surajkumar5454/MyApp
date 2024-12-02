package com.example.details;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;

public class AllDynamicSearchActivity extends AppCompatActivity {

    private LinearLayout layoutDynamicFilters;
    private MaterialButton btnAddFilter, btnQuery;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_dynamic_search_activity);

        // Initialize views
        layoutDynamicFilters = findViewById(R.id.layoutDynamicFilters);
        btnAddFilter = findViewById(R.id.btnAddFilter);
        btnQuery = findViewById(R.id.btnQuery);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Add initial filter
        addFilterViews();

        // Set click listeners
        btnAddFilter.setOnClickListener(v -> addFilterViews());
        btnQuery.setOnClickListener(v -> executeQuery());
    }

    private void executeQuery() {
        LinearLayout resultsLayout = findViewById(R.id.detailsLayout);
        resultsLayout.removeAllViews();

        String baseQuery = "SELECT DISTINCT p.uidno, p.name, u.unit_nm, r.rnk_nm, r.brn_nm " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL";

        List<String> conditions = new ArrayList<>();
        List<String> values = new ArrayList<>();

        // Collect filter conditions
        for (int i = 0; i < layoutDynamicFilters.getChildCount(); i++) {
            LinearLayout filterLayout = (LinearLayout) layoutDynamicFilters.getChildAt(i);
            MaterialAutoCompleteTextView fieldInput = (MaterialAutoCompleteTextView) 
                ((TextInputLayout) filterLayout.getChildAt(0)).getEditText();
            MaterialAutoCompleteTextView valueInput = (MaterialAutoCompleteTextView) 
                ((TextInputLayout) filterLayout.getChildAt(1)).getEditText();

            String field = fieldInput.getText().toString();
            String value = valueInput.getText().toString();

            if (!value.isEmpty()) {
                conditions.add("p." + field + " LIKE ?");
                values.add("%" + value + "%");
            }
        }

        // Build final query
        String query = baseQuery;
        if (!conditions.isEmpty()) {
            query += " AND " + String.join(" AND ", conditions);
        }
        query += " ORDER BY p.name";

        // Execute query
        try (Cursor cursor = database.rawQuery(query, values.toArray(new String[0]))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    addResultCard(
                        cursor.getString(cursor.getColumnIndex("uidno")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("rnk_nm")),
                        cursor.getString(cursor.getColumnIndex("brn_nm")),
                        cursor.getString(cursor.getColumnIndex("unit_nm"))
                    );
                } while (cursor.moveToNext());
            } else {
                showNoResultsMessage();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error executing search", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNoResultsMessage() {
        LinearLayout resultsLayout = findViewById(R.id.detailsLayout);
        resultsLayout.removeAllViews();

        MaterialTextView messageView = new MaterialTextView(this);
        messageView.setText("No matching records found");
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        messageView.setTextColor(getColor(R.color.text_primary));
        messageView.setTextSize(16);
        messageView.setTypeface(null, Typeface.NORMAL);
        messageView.setPadding(16, 32, 16, 32);

        // Add the message to a card for consistent styling
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

        card.addView(messageView);
        resultsLayout.addView(card);
    }

    private void addFilterViews() {
        LinearLayout filterLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16);
        filterLayout.setLayoutParams(layoutParams);
        filterLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Field selection
        TextInputLayout fieldInputLayout = new TextInputLayout(this, null,
            com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu);
        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        fieldParams.setMargins(0, 0, 8, 0);
        fieldInputLayout.setLayoutParams(fieldParams);
        fieldInputLayout.setHint("Select Field");
        fieldInputLayout.setHintTextColor(getColorStateList(R.color.text_secondary));
        fieldInputLayout.setBoxStrokeColor(getColor(R.color.text_primary));

        MaterialAutoCompleteTextView fieldSpinner = new MaterialAutoCompleteTextView(this);
        fieldSpinner.setTextColor(getColor(R.color.text_primary));
        List<String> columnNames = getColumnNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, columnNames);
        fieldSpinner.setAdapter(adapter);
        fieldInputLayout.addView(fieldSpinner);

        // Value input
        TextInputLayout valueInputLayout = new TextInputLayout(this, null,
            com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        valueParams.setMargins(8, 0, 8, 0);
        valueInputLayout.setLayoutParams(valueParams);
        valueInputLayout.setHint("Enter Value");
        valueInputLayout.setHintTextColor(getColorStateList(R.color.text_secondary));
        valueInputLayout.setBoxStrokeColor(getColor(R.color.text_primary));

        MaterialAutoCompleteTextView valueInput = new MaterialAutoCompleteTextView(this);
        valueInput.setTextColor(getColor(R.color.text_primary));
        valueInputLayout.addView(valueInput);

        // Remove button
        MaterialButton removeButton = new MaterialButton(this, null,
            com.google.android.material.R.style.Widget_Material3_Button_IconButton_Filled);
        removeButton.setIcon(getDrawable(R.drawable.ic_close));
        removeButton.setIconTint(getColorStateList(com.google.android.material.R.color.material_on_surface_emphasis_medium));
        removeButton.setContentDescription("Remove Filter");
        removeButton.setOnClickListener(v -> layoutDynamicFilters.removeView(filterLayout));

        filterLayout.addView(fieldInputLayout);
        filterLayout.addView(valueInputLayout);
        filterLayout.addView(removeButton);

        layoutDynamicFilters.addView(filterLayout);

        // Set up auto-complete for value input
        fieldSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedField = (String) parent.getItemAtPosition(position);
            List<String> values = getColumnValues(selectedField);
            ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, values);
            valueInput.setAdapter(valueAdapter);
        });
    }

    private List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<>();
        try (Cursor cursor = database.rawQuery("PRAGMA table_info(parmanentinfo)", null)) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String columnName = cursor.getString(cursor.getColumnIndex("name"));
                    columnNames.add(columnName);
                } while (cursor.moveToNext());
            }
        }
        return columnNames;
    }

    private List<String> getColumnValues(String columnName) {
        List<String> values = new ArrayList<>();
        try (Cursor cursor = database.query(true, "parmanentinfo", 
                new String[]{columnName}, null, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String value = cursor.getString(cursor.getColumnIndex(columnName));
                    if (value != null && !value.isEmpty()) {
                        values.add(value);
                    }
                } while (cursor.moveToNext());
            }
        }
        return values;
    }

    private void addResultCard(String uid, String name, String rank, String branch, String unit) {
        LinearLayout resultsLayout = findViewById(R.id.detailsLayout);

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

        addDetailRow(contentLayout, "Name", name);
        addDetailRow(contentLayout, "UID", uid);
        addDetailRow(contentLayout, "Rank", rank);
        addDetailRow(contentLayout, "Branch", branch);
        addDetailRow(contentLayout, "Unit", unit);

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
        labelView.setTextColor(getColor(R.color.text_secondary));
        labelView.setTextSize(16);
        labelView.setTypeface(null, Typeface.NORMAL);

        MaterialTextView valueView = new MaterialTextView(this);
        valueView.setText(value != null && !value.isEmpty() ? value : "Not Available");
        valueView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        valueView.setTextColor(getColor(R.color.text_primary));
        valueView.setTextSize(16);
        valueView.setTypeface(Typeface.DEFAULT_BOLD);
        valueView.setPadding(16, 0, 0, 0);

        row.addView(labelView);
        row.addView(valueView);
        parent.addView(row);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}
