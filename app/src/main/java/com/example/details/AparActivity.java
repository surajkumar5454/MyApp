package com.example.details;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.ProgressBar;

public class AparActivity extends AppCompatActivity {

    private EditText uidNosearch;
    private SQLiteDatabase database;
    private LinearLayout layout;
    private ProgressBar progressBar;  // Add ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apar_activity);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        uidNosearch = findViewById(R.id.uidNo);
        layout = findViewById(R.id.detailsLayout);
        progressBar = findViewById(R.id.progressBar);  // Initialize ProgressBar

        String uid = getIntent().getStringExtra("uidno");
        if (uid == null) {
            findViewById(R.id.btnQuery).setOnClickListener(v -> {
                String uidNo = uidNosearch.getText().toString().trim();
                if (uidNo.isEmpty()) {
                    Toast.makeText(AparActivity.this, "Please enter a valid UID.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Run the query in the background
                new QueryDatabaseTask().execute(uidNo);
            });
        } else {
            new QueryDatabaseTask().execute(uid);
        }
    }

    // AsyncTask to handle database queries in the background
    @SuppressLint("StaticFieldLeak")
    private class QueryDatabaseTask extends AsyncTask<String, Void, List<String[]>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show the ProgressBar before the background task starts
            progressBar.setVisibility(View.VISIBLE);
            // Clear any existing views before adding new ones
            layout.removeAllViews();
        }

        @Override
        protected List<String[]> doInBackground(String... params) {
            List<String[]> results = new ArrayList<>();
            String values = params[0];
            Cursor cursor = database.rawQuery("SELECT DISTINCT p.uidno, p.name, u.unit_nm, r.rnk_nm, r.brn_nm, dateFrom, dateTo, grading, numericGrad, adverse, remark, integrity FROM parmanentinfo p, tbl_apar t, joininfo j, unitdep u, rnk_brn_mas r WHERE p.uidno=t.uidno AND p.uidno=j.uidno AND j.rank=r.rnk_cd AND j.branch=r.brn_cd AND u.unit_cd=j.unit AND j.dateofrelv IS NULL AND p.uidno LIKE ?", new String[]{"%" + values + "%"});

            try {
                if (cursor.moveToFirst()) {
                    do {
                        String[] row = new String[12];
                        for (int i = 0; i < row.length; i++) {
                            row[i] = cursor.getString(i);
                        }
                        results.add(row);
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<String[]> result) {
            super.onPostExecute(result);
            // Hide the ProgressBar after the background task completes
            progressBar.setVisibility(View.GONE);

            if (result.isEmpty()) {
                Toast.makeText(AparActivity.this, "No records found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the UI with query results
            for (String[] row : result) {
                CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.card_layout, null);
                TextView description = cardView.findViewById(R.id.card_description);
                String cardText = "From: " + row[5] + "\nTo: " + row[6] + "\nGrading: " + row[7] + "\nNumerical Grading: " + row[8] + "\nAdverse: " + row[9] + "\nRemark: " + row[10] + "\nIntegrity: " + row[11];
                description.setText(cardText);
                layout.addView(cardView);
            }
        }
    }
}
