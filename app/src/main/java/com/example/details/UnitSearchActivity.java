package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class UnitSearchActivity extends AppCompatActivity {

    private AutoCompleteTextView unitAutoCompleteTextView;
    private Spinner rankSpinner;
    private LinearLayout resultsLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unitsearch_activity);

        // Initialize UI components
        unitAutoCompleteTextView = findViewById(R.id.unitAutoCompleteTextView);
        rankSpinner = findViewById(R.id.rankSpinner);
        resultsLayout = findViewById(R.id.resultsLayout);
        rankSpinner.setVisibility(View.GONE); // Initially hide rank spinner

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Load values for AutoCompleteTextView (unit names)
        loadUnitSuggestions();

        // Set onItemClickListener for AutoCompleteTextView
        unitAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUnit = (String) parent.getItemAtPosition(position);
                // Fetch and load rank values based on selected unit
                loadRankValues(selectedUnit);
            }
        });

        // Set onItemSelectedListener for rank Spinner
        rankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRank = (String) parent.getItemAtPosition(position);
                String selectedUnit = unitAutoCompleteTextView.getText().toString();
                // Query and display personnel based on selected unit and rank
                queryAndDisplayResults(selectedUnit, selectedRank);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
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

    // Method to load rank values after a unit is selected

        private void loadRankValues(String selectedUnit) {
            List<String> rankList = new ArrayList<>();
            rankList.add("Select Rank"); // Add default option

            Cursor cursor = null;
            try {
                // Query ranks that are present in the selected unit
                cursor = database.rawQuery("SELECT DISTINCT r.rnk_nm FROM rnk_brn_mas r, joininfo j, unitdep u " +
                        "WHERE u.unit_cd = j.unit AND j.rank = r.rnk_cd AND u.unit_nm = ? order by rnk_nm asc", new String[]{selectedUnit});
                if (cursor.moveToFirst()) {
                    do {
                        rankList.add(cursor.getString(0)); // Add available ranks to list for the unit
                    } while (cursor.moveToNext());
                }

                // Set Spinner adapter to show the ranks for the selected unit
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rankList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                rankSpinner.setAdapter(adapter);

                // Show the Spinner if there are ranks available
                if (rankList.size() > 1) {
                    rankSpinner.setVisibility(View.VISIBLE);
                } else {
                  //  Toast.makeText(this, "No ranks available for the selected unit", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }


    private void queryAndDisplayResults(String selectedUnit, String selectedRank) {
        Cursor cursor = null;
        try {
            if (selectedRank.equals("Select Rank"))
            {
                return;
            }
            // Updated query to use DISTINCT and avoid duplicates
            cursor = database.rawQuery("SELECT DISTINCT j.uidno, p.name, r.rnk_nm, r.brn_nm FROM parmanentinfo p, joininfo j, unitdep u, rnk_brn_mas r " +
                            "WHERE u.unit_cd = j.unit AND j.rank = r.rnk_cd AND j.branch = r.brn_cd AND j.dateofrelv IS NULL " +
                            "AND p.uidno = j.uidno AND u.unit_nm = ? AND r.rnk_nm = ?",
                    new String[]{selectedUnit, selectedRank});

            // Clear previous results
            resultsLayout.removeAllViews();

            if (cursor.moveToFirst()) {
                do {
                    final String uid = cursor.getString(0); // Unique ID for each personnel
                    String name = cursor.getString(1);
                    String rank = cursor.getString(2);
                    String branch = cursor.getString(3);

                    // Create TextView for each result
                    TextView resultTextView = new TextView(this);
                    resultTextView.setText(uid + "  " + name + ", " + rank + " (" + branch + ")");
                    resultTextView.setPadding(16, 16, 16, 16);

                    // Make the TextView clickable
                    resultTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Create an intent to start the DetailsActivity and pass the UID
                            Intent intent = new Intent(UnitSearchActivity.this, DetailsActivity.class);
                            intent.putExtra("uidno", uid); // Pass UID as an extra
                            startActivity(intent); // Start DetailsActivity
                        }
                    });

                    // Add the TextView to the results layout
                    resultsLayout.addView(resultTextView);

                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "No personnel found for the selected unit and rank", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
