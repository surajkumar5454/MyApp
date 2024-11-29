package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RankSearchActivity extends AppCompatActivity {

    private Spinner dropdownSpinner;
    private LinearLayout resultsLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ranksearch_activity);

        // Initialize UI components
        dropdownSpinner = findViewById(R.id.dropdownSpinner);
        resultsLayout = findViewById(R.id.resultsLayout);

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Load dropdown values from a table column
        loadDropdownValues();

        // Set onItemSelectedListener to query another table based on the selection
        dropdownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = (String) parent.getItemAtPosition(position);
                if (!selectedValue.equals("Select Rank")) {
                    queryAndDisplayResults(selectedValue);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    // Method to load the dropdown values from a column in the table
    private void loadDropdownValues() {
        List<String> values = new ArrayList<>();
        values.add("Select Rank"); // Add a default option

        Cursor cursor = null;
        try {
            // Query to get values from the specific column of the table
            cursor = database.rawQuery("SELECT DISTINCT rnk_nm, brn_nm FROM rnk_brn_mas order by rnk_nm asc", null);
            if (cursor.moveToFirst()) {
                do {
                    String rank = cursor.getString(0);
                    String branch = cursor.getString(1);
                    String fullRank = rank.concat(" (").concat(branch).concat(")");
                    values.add(fullRank); // Add combined rank and branch to the list
                } while (cursor.moveToNext());
            }

            // Set the adapter for the Spinner (dropdown)
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dropdownSpinner.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Method to query another table based on the selected value and display results
    private void queryAndDisplayResults(String selectedValue) {
        Cursor cursor = null;

        // Split the selectedValue to separate rank and branch
        String[] rankBranch = selectedValue.split("\\(");
        String rank = rankBranch[0].trim(); // Extract rank name
        String branch = rankBranch[1].replace(")", "").trim(); // Extract branch name

        try {
            // Query personnel data based on the selected rank and branch
            cursor = database.rawQuery(
                    "SELECT DISTINCT j.uidno, p.name, r.rnk_nm, r.brn_nm, u.unit_nm " +
                            "FROM parmanentinfo p " +
                            "JOIN joininfo j ON p.uidno = j.uidno " +
                            "JOIN unitdep u ON u.unit_cd = j.unit " +
                            "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                            "WHERE j.dateofrelv IS NULL AND r.rnk_nm = ? AND r.brn_nm = ?",
                    new String[]{rank, branch});

            // Clear previous results
            resultsLayout.removeAllViews();
            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    count ++ ;
                    // Retrieve values from the result set
                    String uid = cursor.getString(0);
                    String name = cursor.getString(1);
                    String rankName = cursor.getString(2);
                    String branchName = cursor.getString(3);
                    String unit = cursor.getString(4);

                    // Display results dynamically
                    TextView resultTextView = new TextView(this);
                    resultTextView.setText(uid + "  " + name + ", " + rankName + " (" + branchName + ")"+ ", " +unit);
                    resultsLayout.addView(resultTextView);

                    // Set the result as clickable
                    Intent intent = new Intent(this, DetailsActivity.class);
                    intent.putExtra("uidno", uid);

                    resultTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(intent);
                        }
                    });
                } while (cursor.moveToNext());
                String numberString = Integer.toString(count);
                TextView resultCount = new TextView(this);
                resultCount.setTextColor(Color.RED);
                resultCount.setGravity(Gravity.CENTER);
                resultCount.setTypeface(null, android.graphics.Typeface.BOLD);
                resultCount.setText("\n\nTotal : "+numberString);
                resultsLayout.addView(resultCount);
            } else {
                Toast.makeText(this, "No results found for " + selectedValue, Toast.LENGTH_SHORT).show();
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
