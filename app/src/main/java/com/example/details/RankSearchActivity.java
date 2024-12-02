package com.example.details;

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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;
import com.example.details.databinding.RanksearchActivityBinding;

public class RankSearchActivity extends AppCompatActivity {

    private static final String TAG = "RankSearchActivity";
    private RanksearchActivityBinding binding;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RanksearchActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        // Setup toolbar
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

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
        List<String> ranks = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            ranks
        );
        binding.rankSpinner.setAdapter(adapter);
    }

    private void executeSearch(String rank) {
        binding.resultsLayout.removeAllViews();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT DISTINCT p.uidno, p.name, u.unit_nm, r.rnk_nm, r.brn_nm " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL AND r.rnk_nm = ? " +
                "ORDER BY p.name",
                new String[]{rank}
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

        addDetailRow(contentLayout, "Name", cursor.getString(cursor.getColumnIndex("name")));
        addDetailRow(contentLayout, "UID", cursor.getString(cursor.getColumnIndex("uidno")));
        addDetailRow(contentLayout, "Unit", cursor.getString(cursor.getColumnIndex("unit_nm")));
        addDetailRow(contentLayout, "Branch", cursor.getString(cursor.getColumnIndex("brn_nm")));

        card.addView(contentLayout);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}
