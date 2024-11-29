package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PostingActivity extends AppCompatActivity {

    private EditText uidNosearch;
    private Button btnQuery;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posting_activity);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        uidNosearch = findViewById(R.id.uidNo);
        btnQuery = findViewById(R.id.btnQuery);
        String uid = getIntent().getStringExtra("uidno");
        if (uid==null) {
        // Query button click listener
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uidNo = uidNosearch.getText().toString().trim();

                // Check if UID is empty
                if (uidNo.isEmpty()) {
                    Toast.makeText(PostingActivity.this, "Please enter a valid UID.", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution if the UID is blank
                }

                // Execute query if the UID is valid
                executeQuery(uidNo);
            }
        });
        }
        else
        {
            executeQuery(uid);
        }
    }

    private void executeQuery(String query) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                    "SELECT DISTINCT j.uidno, j.name, u.unit_nm, r.rnk_nm, j.dateofjoin, j.dateofrelv " +
                            "FROM joininfo j, unitdep u, rnk_brn_mas r " +
                            "WHERE j.rank = r.rnk_cd " +
                            "AND j.branch = r.brn_cd " +
                            "AND u.unit_cd = j.unit " +
                            "AND j.uidno LIKE ? " +
                            "ORDER BY j.dateofjoin DESC",  // Sorting by dateOfJoin in descending order
                    new String[]{"%" + query + "%"}
            );

            LinearLayout layout = findViewById(R.id.detailsLayout);
            layout.removeAllViews();

            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    String uid = cursor.getString(0);
                    String name = cursor.getString(1);
                    String unit = cursor.getString(2);
                    String rank = cursor.getString(3);
                    String dateJoin = cursor.getString(4);
                    String dateRelieve = cursor.getString(5);

                    // First record UID and name on a card
                    if (count == 0) {
                        CardView mainCardView = (CardView) getLayoutInflater().inflate(R.layout.card_layout, null);
                        mainCardView.setCardBackgroundColor(Color.parseColor("#3afcf6")); // Light pink color
                        TextView uidNameTextView = mainCardView.findViewById(R.id.card_description);
                        uidNameTextView.setTextColor(Color.BLACK);
                        uidNameTextView.setGravity(Gravity.CENTER);
                        uidNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                        uidNameTextView.setText(uid + " " + name);
                        layout.addView(mainCardView);
                        count = 1;
                    }

                    // Create a new card for the rest of the details
                    CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.card_layout, null);
                    TextView cardDescription = cardView.findViewById(R.id.card_description);
                    StringBuilder details = new StringBuilder();
                    details.append("Unit: ").append(unit).append("\n");
                    details.append("Rank: ").append(rank).append("\n");
                    details.append("Date of Joining: ").append(dateJoin).append("\n");
                    details.append("Date of Relieving: ").append(dateRelieve).append("\n");

                    cardDescription.setText(details.toString());
                    layout.addView(cardView);

                    // Add separator (optional)
                    View separatorView = new View(this);
                    separatorView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1)); // Separator height
                    separatorView.setBackgroundColor(0); // Separator color
                    layout.addView(separatorView);

                    // Set click listener for each card to pass data to DetailsActivity
                    cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PostingActivity.this, DetailsActivity.class);
                            intent.putExtra("uidno", uid);
                            startActivity(intent);
                        }
                    });

                } while (cursor.moveToNext());
            } else {
                // If no records were found, display a message to the user
                Toast.makeText(this, "No records found with the UID: " + query, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uid = getIntent().getStringExtra("uidno");
                System.out.println("uid value : " + uid);
                String uidNo = uidNosearch.getText().toString().trim();
                // Check if UID is empty
                if (uidNo.isEmpty()) {
                    Toast.makeText(PostingActivity.this, "Please enter a valid UID.", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution if the UID is blank
                }
                executeQuery(uidNo);
            }
        });
    }
}
