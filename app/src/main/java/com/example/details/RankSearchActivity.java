package com.example.details;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;
import com.example.details.databinding.RanksearchActivityBinding;
import android.content.Intent;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;

public class RankSearchActivity extends AppCompatActivity {

    private static final String TAG = "RankSearchActivity";
    private RanksearchActivityBinding binding;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RanksearchActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar with back button
        binding.topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Load ranks into spinner
        loadRanks();

        // Set click listener for search button
        binding.searchButton.setOnClickListener(v -> {
            String selectedRank = binding.rankSpinner.getText().toString();
            if (selectedRank.isEmpty()) {
                Toast.makeText(RankSearchActivity.this, "Please select a rank", Toast.LENGTH_SHORT).show();
                return;
            }
            executeSearch(selectedRank);
        });
    }

    private void loadRanks() {
        new LoadRanksTask(this).execute();
    }

    private static class LoadRanksTask extends AsyncTask<Void, Void, List<String>> {
        private final WeakReference<RankSearchActivity> activityReference;

        LoadRanksTask(RankSearchActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            RankSearchActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            List<String> ranks = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = activity.database.rawQuery(
                    "SELECT DISTINCT rnk_nm FROM rnk_brn_mas ORDER BY rnk_nm",
                    null
                );
                if (cursor.moveToFirst()) {
                    do {
                        ranks.add(cursor.getString(0));
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading ranks", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return ranks;
        }

        @Override
        protected void onPostExecute(List<String> ranks) {
            RankSearchActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (ranks != null && !ranks.isEmpty()) {
                activity.setupRankAdapter(ranks);
            } else {
                Toast.makeText(activity, "Error loading ranks", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRankAdapter(List<String> ranks) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_dropdown_item_1line, ranks) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> suggestions = new ArrayList<>();

                        if (constraint == null || constraint.length() == 0) {
                            suggestions.addAll(ranks);
                        } else {
                            String filterPattern = constraint.toString().toLowerCase().trim();
                            for (String rank : ranks) {
                                if (rank.toLowerCase().contains(filterPattern)) {
                                    suggestions.add(rank);
                                }
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

        binding.rankSpinner.setAdapter(adapter);
        binding.rankSpinner.setThreshold(1); // Show suggestions after 1 character
        
        // Enable text input
        binding.rankSpinner.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        
        // Show dropdown when focused
        binding.rankSpinner.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && binding.rankSpinner.getText().length() == 0) {
                binding.rankSpinner.showDropDown();
            }
        });

        // Show filtered suggestions as user types
        binding.rankSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show dropdown with filtered results
                if (!binding.rankSpinner.isPopupShowing()) {
                    binding.rankSpinner.showDropDown();
                }
                
                // Clear previous results
                binding.resultsLayout.removeAllViews();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set dropdown properties
        binding.rankSpinner.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        binding.rankSpinner.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        
        // Show all suggestions on click
        binding.rankSpinner.setOnClickListener(v -> {
            binding.rankSpinner.showDropDown();
        });
    }

    private void executeSearch(String rank) {
        new SearchTask(this).execute(rank);
    }

    private static class SearchTask extends AsyncTask<String, Void, Cursor> {
        private final WeakReference<RankSearchActivity> activityReference;
        private final String errorMessage = "Error occurred while fetching data";

        SearchTask(RankSearchActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            RankSearchActivity activity = activityReference.get();
            if (activity != null) {
                activity.binding.resultsLayout.removeAllViews();
                // Optional: Show loading indicator
                activity.showLoadingIndicator();
            }
        }

        @Override
        protected Cursor doInBackground(String... params) {
            RankSearchActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            try {
                return activity.database.rawQuery(
                    "SELECT DISTINCT p.uidno, p.name, u.unit_nm, r.rnk_nm, r.brn_nm " +
                    "FROM parmanentinfo p " +
                    "JOIN joininfo j ON p.uidno = j.uidno " +
                    "JOIN unitdep u ON u.unit_cd = j.unit " +
                    "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                    "WHERE j.dateofrelv IS NULL AND r.rnk_nm = ? " +
                    "ORDER BY p.name",
                    new String[]{params[0]}
                );
            } catch (Exception e) {
                Log.e(TAG, "Error executing search", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            RankSearchActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                if (cursor != null) cursor.close();
                return;
            }

            // Hide loading indicator if you added one
            activity.hideLoadingIndicator();

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        activity.addPersonCard(cursor);
                    } while (cursor.moveToNext());
                } else {
                    activity.showNoResultsMessage();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error displaying results", e);
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
            } finally {
                if (cursor != null) cursor.close();
            }
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

        // Store the UID for click handling
        String uid = cursor.getString(cursor.getColumnIndex("uidno"));
        
        addDetailRow(contentLayout, "Name", cursor.getString(cursor.getColumnIndex("name")));
        addDetailRow(contentLayout, "UID", uid);
        addDetailRow(contentLayout, "Unit", cursor.getString(cursor.getColumnIndex("unit_nm")));
        addDetailRow(contentLayout, "Branch", cursor.getString(cursor.getColumnIndex("brn_nm")));

        card.addView(contentLayout);

        // Add click listener to navigate to details
        card.setClickable(true);
        card.setFocusable(true);
        card.setBackgroundResource(android.R.drawable.list_selector_background);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("uidno", uid);
            startActivity(intent);
        });

        binding.resultsLayout.addView(card);
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
        messageView.setText("No personnel found with this rank");
        messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        messageView.setTextColor(getColor(R.color.card_value_text));
        messageView.setTextSize(16);
        binding.resultsLayout.addView(messageView);
    }

    private void showLoadingIndicator() {
        // Create and show a progress bar or loading animation
        MaterialTextView loadingText = new MaterialTextView(this);
        loadingText.setText("Searching...");
        loadingText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        loadingText.setTextColor(getColor(R.color.card_value_text));
        loadingText.setTextSize(16);
        loadingText.setPadding(0, 32, 0, 32);
        binding.resultsLayout.addView(loadingText);
    }

    private void hideLoadingIndicator() {
        binding.resultsLayout.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}
