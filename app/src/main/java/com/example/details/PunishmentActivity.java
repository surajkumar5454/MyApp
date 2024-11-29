package com.example.details;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PunishmentActivity extends AppCompatActivity {

    private EditText uidNosearch;

    private Button btnQuery;
    private ListView listViewResults;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.punishment_activity);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();
        uidNosearch = findViewById(R.id.uidNo);
        btnQuery = findViewById(R.id.btnQuery);
        listViewResults = findViewById(R.id.listViewResults);

        String uid = getIntent().getStringExtra("uidno");
        if (uid==null) {
        // Query button click listener
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uidNo = uidNosearch.getText().toString().trim();
                // Check if UID is empty
                if (uidNo.isEmpty()) {
                    Toast.makeText(PunishmentActivity.this, "Please enter a valid UID.", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution if the UID is blank
                }
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
            cursor = database.rawQuery("SELECT * FROM punshiment WHERE uidno LIKE ?", new String[]{"%" + query + "%"});
            LinearLayout layout = findViewById(R.id.detailsLayout); // Assuming you have a LinearLayout with id "detailsLayout" in your activity_details.xml
            layout.removeAllViews();
            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder queryResult = new StringBuilder();
                    String uid = cursor.getString(0);
                    String name = cursor.getString(1);
                    String category = cursor.getString(2);
                    String order = cursor.getString(3);
                    String punishmentType = cursor.getString(4);
                    String details = cursor.getString(4);
                    if (count==0) {
                        queryResult.append(uid).append("\n");
                        queryResult.append(name).append("\n\n\n");
                        count = 1;
                    }

                    queryResult.append("Category: ").append(category).append("\n");
                    queryResult.append("Order No: ").append(order).append("\n");
                    queryResult.append("Punishment Type: ").append(punishmentType).append("\n");
                    queryResult.append("Details: ").append(details).append("\n");
                    queryResult.append("\n");

                    Intent intent = new Intent(this, DetailsActivity.class);
                    intent.putExtra("uidno", uid);
                // Create a new TextView to display the record
                    TextView recordTextView = new TextView(this);
                    recordTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    recordTextView.setText(queryResult);
                    // Add the TextView to the layout
                    layout.addView(recordTextView);

                    // Add a separator between records (optional)
                    View separatorView = new View(this);
                    separatorView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1)); // Height of separator
                    separatorView.setBackgroundColor(0); // Color of separator
                    layout.addView(separatorView);
                    recordTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                         //   startActivity(intent);
                         //   executeQuery("SELECT * FROM posting_info WHERE uidno=?", new String[]{uid});
                        }
                    });

                } while (cursor.moveToNext());
            }
            else
            {
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
                    Toast.makeText(PunishmentActivity.this, "Please enter a valid UID.", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution if the UID is blank
                }
                executeQuery(uidNo);
            }
        });
    }


}
