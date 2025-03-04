package com.example.details;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    private MaterialAutoCompleteTextView uidSearchInput;
    private MaterialButton btnUidSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Initialize views
        uidSearchInput = findViewById(R.id.uidSearchInput);
        btnUidSearch = findViewById(R.id.btnUidSearch);

        // Setup autocomplete
        setupUidAutocomplete();

        // Set up UID search button
        btnUidSearch.setOnClickListener(v -> performSearch());

        // Initialize all buttons
        MaterialButton btnAllSearch = findViewById(R.id.btn_allsearch);
        MaterialButton btnPosting = findViewById(R.id.btn_posting);
        MaterialButton btnTraining = findViewById(R.id.btn_training);
        MaterialButton btnPunishment = findViewById(R.id.btn_punishment);
        MaterialButton btnApar = findViewById(R.id.btn_apar);
        MaterialButton btnLeave = findViewById(R.id.btn_leave);
        MaterialButton btnUnitSearch = findViewById(R.id.btn_unitsearch);
        MaterialButton btnRankSearch = findViewById(R.id.btn_ranksearch);
        MaterialButton btnTableSearch = findViewById(R.id.btn_tblsearch);
        MaterialButton btnIndividualSearch = findViewById(R.id.btn_individualsearch);
        MaterialButton btnMasterSearch = findViewById(R.id.btn_mastersearch);

        // Set click listeners
        btnAllSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllDynamicSearchActivity.class);
            startActivity(intent);
        });

        btnPosting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PostingActivity.class);
            startActivity(intent);
        });

        btnTraining.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
            startActivity(intent);
        });

        btnPunishment.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PunishmentActivity.class);
            startActivity(intent);
        });

        btnApar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AparActivity.class);
            startActivity(intent);
        });

        btnLeave.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaveActivity.class);
            startActivity(intent);
        });

        btnUnitSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UnitSearchActivity.class);
            startActivity(intent);
        });

        btnRankSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RankSearchActivity.class);
            startActivity(intent);
        });

        btnTableSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TableSearchActivity.class);
            startActivity(intent);
        });

        btnIndividualSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IndividualSearchActivity.class);
            startActivity(intent);
        });

        btnMasterSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TableSearchActivity.class);
            startActivity(intent);
        });

        // Add in onCreate after other button initializations
        Button reportsButton = findViewById(R.id.reportsButton);
        reportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupUidAutocomplete() {
        // Create custom adapter with filter capability
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_dropdown_item_1line, new ArrayList<>()) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> suggestions = new ArrayList<>();

                        if (constraint != null && constraint.length() > 0) {
                            String filterPattern = constraint.toString().trim();
                            // Query database for suggestions
                            Cursor cursor = null;
                            try {
                                cursor = database.rawQuery(
                                    "SELECT DISTINCT uidno, name FROM parmanentinfo " +
                                    "WHERE uidno LIKE ? OR name LIKE ? " +
                                    "LIMIT 10",
                                    new String[]{"%" + filterPattern + "%", "%" + filterPattern + "%"}
                                );

                                if (cursor != null && cursor.moveToFirst()) {
                                    do {
                                        String uid = cursor.getString(0);
                                        String name = cursor.getString(1);
                                        suggestions.add(uid + " - " + name);
                                    } while (cursor.moveToNext());
                                }
                            } catch (Exception e) {
                                Log.e("MainActivity", "Error getting suggestions", e);
                            } finally {
                                if (cursor != null) cursor.close();
                            }
                        }

                        results.values = suggestions;
                        results.count = suggestions.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        if (results != null && results.count > 0) {
                            addAll((List<String>) results.values);
                        }
                        notifyDataSetChanged();
                    }
                };
            }
        };

        uidSearchInput.setAdapter(adapter);
        uidSearchInput.setThreshold(1); // Show suggestions after 1 character

        // Show dropdown when focused
        uidSearchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && uidSearchInput.getText().length() > 0) {
                uidSearchInput.showDropDown();
            }
        });

        // Handle selection
        uidSearchInput.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            String uid = selected.split(" - ")[0]; // Extract UID from selection
            navigateToDetails(uid);
        });
    }

    private void performSearch() {
        String input = uidSearchInput.getText().toString().trim();
        String uid = input.split(" - ")[0]; // Handle both direct UID input and selection from dropdown
        
        if (uid.isEmpty()) {
            uidSearchInput.setError("Please enter a UID");
            return;
        }

        // Check if UID exists
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT uidno FROM parmanentinfo WHERE uidno = ?",
                new String[]{uid}
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                navigateToDetails(uid);
            } else {
                Toast.makeText(this, "No record found for this UID", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error searching UID", e);
            Toast.makeText(this, "Error searching UID", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void navigateToDetails(String uid) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("uidno", uid);
        startActivity(intent);
        
        // Animate and clear the search input
        uidSearchInput.animate()
            .alpha(0f)  // Fade out to transparent
            .setDuration(200)  // Animation duration in milliseconds
            .withEndAction(() -> {
                uidSearchInput.setText("");  // Clear text
                uidSearchInput.clearFocus();  // Remove focus
                uidSearchInput.animate()
                    .alpha(1f)  // Fade back in
                    .setDuration(200)
                    .start();
            })
            .start();
    }
}