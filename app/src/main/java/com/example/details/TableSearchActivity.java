package com.example.details;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class TableSearchActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView tableSpinner;
    private LinearLayout conditionsLayout;
    private MaterialButton executeQueryButton;
    private LinearLayout resultsLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_search);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        tableSpinner = findViewById(R.id.tableSpinner);
        conditionsLayout = findViewById(R.id.conditionsLayout);
        executeQueryButton = findViewById(R.id.executeQueryButton);
        resultsLayout = findViewById(R.id.resultsLayout);

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Load tables into the spinner
        loadTables();

        // Set onClickListener for the button to execute the query
        executeQueryButton.setOnClickListener(v -> executeQuery());
    }

    private void loadTables() {
        List<String> tables = new ArrayList<>();
        tables.add("Select Table");
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (cursor.moveToFirst()) {
                do {
                    tables.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            
            // Create custom adapter with darker text
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
                android.R.layout.simple_dropdown_item_1line, tables) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTextColor(getColor(R.color.md_theme_light_onPrimaryContainer)); // Dark green color
                    view.setTypeface(null, Typeface.BOLD);
                    view.setTextSize(16);
                    view.setPadding(16, 16, 16, 16);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTextColor(getColor(R.color.md_theme_light_onPrimaryContainer)); // Dark green color
                    view.setTextSize(16);
                    view.setPadding(16, 16, 16, 16);
                    if (position == 0) { // "Select Table" item
                        view.setTypeface(null, Typeface.NORMAL);
                    } else {
                        view.setTypeface(null, Typeface.BOLD);
                    }
                    return view;
                }
            };
            
            tableSpinner.setAdapter(adapter);
            tableSpinner.setTextColor(getColor(R.color.md_theme_light_onPrimaryContainer));
            tableSpinner.setTypeface(null, Typeface.BOLD);
            
            // Set up item click listener instead of OnItemSelectedListener
            tableSpinner.setOnItemClickListener((parent, view, position, id) -> {
                String selectedTable = (String) parent.getItemAtPosition(position);
                if (!selectedTable.equals("Select Table")) {
                    showColumnConditions(selectedTable);
                } else {
                    conditionsLayout.removeAllViews();
                    conditionsLayout.setVisibility(View.GONE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading tables", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showColumnConditions(String tableName) {
        conditionsLayout.removeAllViews();
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor.moveToFirst()) {
                do {
                    columns.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }

            for (String column : columns) {
                TextInputLayout inputLayout = new TextInputLayout(this, null,
                    com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
                inputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                inputLayout.setHint(column);
                inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                inputLayout.setHintTextColor(getColorStateList(R.color.md_theme_light_onPrimaryContainer)); // Dark green for column names
                inputLayout.setBoxStrokeColor(getColor(R.color.md_theme_light_primary));
                
                MaterialAutoCompleteTextView input = new MaterialAutoCompleteTextView(this);
                input.setTextColor(getColor(R.color.md_theme_light_onPrimaryContainer));
                input.setTextSize(16);
                input.setTypeface(null, Typeface.BOLD);
                input.setHintTextColor(getColor(R.color.md_theme_light_onPrimaryContainer)); // Dark hint text
                inputLayout.addView(input);
                
                conditionsLayout.addView(inputLayout);
            }
            conditionsLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void executeQuery() {
        String selectedTable = tableSpinner.getText().toString();
        if (selectedTable.isEmpty() || selectedTable.equals("Select Table")) {
            Toast.makeText(this, "Please select a table", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + selectedTable + " WHERE ");
        List<String> conditions = new ArrayList<>();

        // Get conditions based on user input
        for (int i = 0; i < conditionsLayout.getChildCount(); i++) {
            View view = conditionsLayout.getChildAt(i);
            if (view instanceof TextInputLayout) {
                TextInputLayout inputLayout = (TextInputLayout) view;
                MaterialAutoCompleteTextView input = (MaterialAutoCompleteTextView) inputLayout.getEditText();
                String value = input.getText().toString();
                if (!value.isEmpty()) {
                    String columnName = inputLayout.getHint().toString();
                    conditions.add(columnName + " LIKE '%" + value + "%'");
                }
            }
        }

        if (!conditions.isEmpty()) {
            queryBuilder.append(String.join(" AND ", conditions));
            System.out.println("Query :: " + queryBuilder);
            
            // Execute the query
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(queryBuilder.toString(), null);
                resultsLayout.removeAllViews();
                if (cursor.moveToFirst()) {
                    do {
                        StringBuilder rowBuilder = new StringBuilder();
                        for (int j = 0; j < cursor.getColumnCount(); j++) {
                            rowBuilder.append(cursor.getColumnName(j))
                                    .append(": ")
                                    .append(cursor.getString(j))
                                    .append("\n");
                        }
                        addResultToLayout(rowBuilder.toString());
                    } while (cursor.moveToNext());
                } else {
                    Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error executing query", Toast.LENGTH_SHORT).show();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Toast.makeText(this, "Please enter at least one condition.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addResultToLayout(String result) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setCardElevation(4f);
        card.setRadius(12f);
        card.setContentPadding(24, 16, 24, 16);
        card.setCardBackgroundColor(getColor(R.color.card_content_background));

        MaterialTextView resultView = new MaterialTextView(this);
        resultView.setText(result);
        resultView.setTextColor(getColor(R.color.card_value_text));
        resultView.setTextSize(16);
        resultView.setTypeface(null, Typeface.BOLD);  // Make text bold
        resultView.setLineSpacing(8, 1);
        
        // Add padding for better readability
        resultView.setPadding(16, 8, 16, 8);

        // Style the column names differently from values
        String[] lines = result.split("\n");
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex != -1) {
                // Column name
                builder.append(line.substring(0, colonIndex + 1), 
                    new ForegroundColorSpan(getColor(R.color.card_label_text)), 
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Value
                builder.append(line.substring(colonIndex + 1) + "\n", 
                    new ForegroundColorSpan(getColor(R.color.card_value_text)), 
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        resultView.setText(builder);

        card.addView(resultView);
        resultsLayout.addView(card);
    }
}
