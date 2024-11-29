package com.example.details;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
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

public class TableSearchActivity extends AppCompatActivity {

    private Spinner tableSpinner;
    private LinearLayout conditionsLayout;
    private Button executeQueryButton;
    private LinearLayout resultsLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_search);

        // Initialize UI components
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

        // Set a listener for the table selection
        tableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTable = (String) parent.getItemAtPosition(position);
                if (!selectedTable.equals("Select Table")) {
                    showColumnConditions(selectedTable);
                } else {
                    conditionsLayout.removeAllViews();
                    conditionsLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set onClickListener for the button to execute the query
        executeQueryButton.setOnClickListener(v -> executeQuery());
    }

    private void loadTables() {
        List<String> tables = new ArrayList<>();
        tables.add("Select Table"); // Default option
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (cursor.moveToFirst()) {
                do {
                    tables.add(cursor.getString(0)); // Add table names to the list
                } while (cursor.moveToNext());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tables);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            tableSpinner.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showColumnConditions(String tableName) {
        // Clear previous condition views
        conditionsLayout.removeAllViews();

        // Get columns of the selected table
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor.moveToFirst()) {
                do {
                    columns.add(cursor.getString(1)); // Add column names to the list
                } while (cursor.moveToNext());
            }

            for (String column : columns) {
                // Create a TextView for the column name
                TextView textView = new TextView(this);
                textView.setText(column);
                conditionsLayout.addView(textView);

                // Create EditText for user to enter condition
                EditText conditionInput = new EditText(this);
                conditionInput.setHint("Enter value for " + column);
                conditionsLayout.addView(conditionInput);
            }
            conditionsLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void executeQuery() {
        String selectedTable = (String) tableSpinner.getSelectedItem();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + selectedTable + " WHERE ");
        List<String> conditions = new ArrayList<>();

        // Get conditions based on user input
        for (int i = 0; i < conditionsLayout.getChildCount(); i += 2) { // Assuming TextView and EditText pairs
            EditText conditionInput = (EditText) conditionsLayout.getChildAt(i + 1);
            String value = conditionInput.getText().toString();
            if (!value.isEmpty()) {
                TextView columnTextView = (TextView) conditionsLayout.getChildAt(i);
                String columnName = columnTextView.getText().toString();
                conditions.add(columnName + " LIKE '%" + value + "%'");
            }
        }

        if (!conditions.isEmpty()) {
            queryBuilder.append(String.join(" AND ", conditions));
            System.out.println("Query ::  "+queryBuilder);
            // Execute the query
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(queryBuilder.toString(), null);
                resultsLayout.removeAllViews();
                if (cursor.moveToFirst()) {
                    do {
                        StringBuilder rowBuilder = new StringBuilder();
                        for (int j = 0; j < cursor.getColumnCount(); j++) {
                            rowBuilder.append(cursor.getColumnName(j)).append(": ").append(cursor.getString(j)).append("\n");
                        }
                        TextView resultTextView = new TextView(this);
                        resultTextView.setText(rowBuilder.toString());
                        resultsLayout.addView(resultTextView);
                    } while (cursor.moveToNext());
                } else {
                    Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Toast.makeText(this, "Please enter at least one condition.", Toast.LENGTH_SHORT).show();
        }
    }
}
