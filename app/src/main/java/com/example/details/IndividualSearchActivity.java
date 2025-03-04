package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndividualSearchActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    private MaterialAutoCompleteTextView unitInput, rankInput, branchInput, nameInput;
    private LinearLayout resultsLayout;
    private ArrayAdapter<String> unitAdapter, rankAdapter, branchAdapter, nameAdapter;
    private MaterialButton btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_search);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Initialize views
        unitInput = findViewById(R.id.unitInput);
        rankInput = findViewById(R.id.rankInput);
        branchInput = findViewById(R.id.branchInput);
        nameInput = findViewById(R.id.nameInput);
        resultsLayout = findViewById(R.id.resultsLayout);
        btnSearch = findViewById(R.id.btnSearch);

        // Initialize adapters
        initializeAdapters();
        setupAutoCompleteFields();
        setupSearchButton();
    }

    private void initializeAdapters() {
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        rankAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
    }

    private void setupAutoCompleteFields() {
        unitInput.setAdapter(unitAdapter);
        rankInput.setAdapter(rankAdapter);
        branchInput.setAdapter(branchAdapter);
        nameInput.setAdapter(nameAdapter);

        unitInput.setThreshold(1);
        rankInput.setThreshold(1);
        branchInput.setThreshold(1);
        nameInput.setThreshold(1);

        // Simple item click listeners that just clear focus
        unitInput.setOnItemClickListener((parent, view, position, id) -> {
            unitInput.clearFocus();
        });
        
        rankInput.setOnItemClickListener((parent, view, position, id) -> {
            rankInput.clearFocus();
        });
        
        branchInput.setOnItemClickListener((parent, view, position, id) -> {
            branchInput.clearFocus();
        });
        
        nameInput.setOnItemClickListener((parent, view, position, id) -> {
            nameInput.clearFocus();
        });

        // Add focus change listeners
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (!hasFocus) {
                if (v == unitInput) unitInput.dismissDropDown();
                else if (v == rankInput) rankInput.dismissDropDown();
                else if (v == branchInput) branchInput.dismissDropDown();
                else if (v == nameInput) nameInput.dismissDropDown();
            }
        };
        
        unitInput.setOnFocusChangeListener(focusListener);
        rankInput.setOnFocusChangeListener(focusListener);
        branchInput.setOnFocusChangeListener(focusListener);
        nameInput.setOnFocusChangeListener(focusListener);

        setupTextChangeListener(unitInput, "unit");
        setupTextChangeListener(rankInput, "rank");
        setupTextChangeListener(branchInput, "branch");
        setupTextChangeListener(nameInput, "name");
    }

    private void setupTextChangeListener(MaterialAutoCompleteTextView input, String field) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    loadSuggestions(input, field, s.toString());
                }
            }
            
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String unit = unitInput.getText().toString().trim();
        String rank = rankInput.getText().toString().trim();
        String branch = branchInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();

        if (unit.isEmpty() && rank.isEmpty() && branch.isEmpty() && name.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search criteria", Toast.LENGTH_SHORT).show();
            return;
        }

        new SearchTask(this).execute(unit, rank, branch, name);
    }

    private void loadSuggestions(MaterialAutoCompleteTextView input, String field, String query) {
        String column, table;
        
        if (field.equals("unit")) {
            column = "unit_nm";
            table = "unitdep";
        } else if (field.equals("rank")) {
            column = "rnk_nm";
            table = "rnk_brn_mas";
        } else if (field.equals("branch")) {
            column = "brn_nm";
            table = "rnk_brn_mas";
        } else { // name
            column = "name";
            table = "parmanentinfo";
        }

        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                Set<String> suggestions = new HashSet<>();
                Cursor cursor = null;
                try {
                    cursor = database.rawQuery(
                        "SELECT DISTINCT " + column + " FROM " + table + 
                        " WHERE " + column + " LIKE ? LIMIT 10",
                        new String[]{"%" + query + "%"}
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            suggestions.add(cursor.getString(0));
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e("IndividualSearch", "Error loading suggestions", e);
                } finally {
                    if (cursor != null) cursor.close();
                }
                return new ArrayList<>(suggestions);
            }

            @Override
            protected void onPostExecute(List<String> suggestions) {
                ArrayAdapter<String> adapter;
                if (field.equals("unit")) {
                    adapter = unitAdapter;
                } else if (field.equals("rank")) {
                    adapter = rankAdapter;
                } else if (field.equals("branch")) {
                    adapter = branchAdapter;
                } else { // name
                    adapter = nameAdapter;
                }
                
                adapter.clear();
                adapter.addAll(suggestions);
                adapter.notifyDataSetChanged();
                if (!input.isPopupShowing() && !suggestions.isEmpty()) {
                    input.showDropDown();
                }
            }
        }.execute();
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
        addDetailRow(contentLayout, "Unit", cursor.getString(cursor.getColumnIndex("unit_nm")));
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
        messageView.setText("No matching records found");
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        messageView.setTextColor(getColor(R.color.card_value_text));
        messageView.setTextSize(16);
        messageView.setPadding(16, 32, 16, 32);
        messageView.setTypeface(null, Typeface.BOLD);
        resultsLayout.addView(messageView);
    }

    private static class SearchTask extends AsyncTask<String, Void, Cursor> {
        private final WeakReference<IndividualSearchActivity> activityRef;
        private final String errorMessage = "Error occurred while searching";

        SearchTask(IndividualSearchActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected Cursor doInBackground(String... params) {
            IndividualSearchActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return null;

            String unit = params[0];
            String rank = params[1];
            String branch = params[2];
            String name = params[3];

            try {
                List<String> whereArgs = new ArrayList<>();
                StringBuilder query = new StringBuilder(
                    "SELECT DISTINCT p.uidno, p.name, u.unit_nm, r.rnk_nm, r.brn_nm " +
                    "FROM parmanentinfo p " +
                    "JOIN joininfo j ON p.uidno = j.uidno " +
                    "JOIN unitdep u ON u.unit_cd = j.unit " +
                    "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                    "WHERE j.dateofrelv IS NULL"
                );

                if (!unit.isEmpty()) {
                    query.append(" AND u.unit_nm LIKE ?");
                    whereArgs.add("%" + unit + "%");
                }
                if (!rank.isEmpty()) {
                    query.append(" AND r.rnk_nm LIKE ?");
                    whereArgs.add("%" + rank + "%");
                }
                if (!branch.isEmpty()) {
                    query.append(" AND r.brn_nm LIKE ?");
                    whereArgs.add("%" + branch + "%");
                }
                if (!name.isEmpty()) {
                    query.append(" AND p.name LIKE ?");
                    whereArgs.add("%" + name + "%");
                }

                query.append(" ORDER BY p.name");

                return activity.database.rawQuery(
                    query.toString(),
                    whereArgs.toArray(new String[0])
                );
            } catch (Exception e) {
                Log.e("IndividualSearch", "Search error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            IndividualSearchActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;

            activity.resultsLayout.removeAllViews();
            
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        activity.addPersonCard(cursor);
                    } while (cursor.moveToNext());
                } else {
                    activity.showNoResultsMessage();
                }
            } catch (Exception e) {
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) database.close();
    }
}