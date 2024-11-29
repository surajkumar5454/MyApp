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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class TrainingActivity extends AppCompatActivity {

    private EditText uidNosearch;
    private Button btnQuery;
    private ListView listViewResults;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_activity);
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


                    System.out.println("uid value : " + uid);
                    String uidNo = uidNosearch.getText().toString().trim();
                    // Check if UID is empty
                    if (uidNo.isEmpty()) {
                        Toast.makeText(TrainingActivity.this, "Please enter a valid UID.", Toast.LENGTH_SHORT).show();
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
         //   cursor = database.rawQuery("SELECT * FROM training WHERE uidno LIKE ?", new String[]{"%" + query + "%"});
            cursor = database.rawQuery("SELECT DISTINCT p.uidno\n" +
                    ",p.name\n" +
                    ",u.unit_nm\n" +
                    ",r.rnk_nm\n" +
                    ",r.brn_nm\n" +
                    ",c.course_nm,fromDate,toDate,tc.tc_nm,position,prof,theory,instruction_ability, t.remarks,c.category FROM parmanentinfo p, joininfo j, training t,trainingcourse c, trainingCenter tc , unitdep u, rnk_brn_mas r \n" +
                    "where t.course=c.id and t.trainingCenter=tc.tc_cd\n" +
                    "and p.uidno=t.uidno  \n" +
                    "and p.uidno = j.uidno\n" +
                    "and j.rank=r.rnk_cd \n" +
                    "and j.branch=r.brn_cd\n" +
                    "and u.unit_cd = j.unit\n" +
                    " and j.dateofrelv is NULL \n" +
                    " and j.uidno like ?", new String[]{"%" + query + "%"});
            LinearLayout layout = findViewById(R.id.detailsLayout); // Assuming you have a LinearLayout with id "detailsLayout" in your activity_details.xml
            layout.removeAllViews();
            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder queryResult = new StringBuilder();
                    String uid = cursor.getString(0);
                    String name = cursor.getString(1);
                    String unit = cursor.getString(2);
                    String rank = cursor.getString(3);
                    String branch = cursor.getString(4);
                    String courseName = cursor.getString(5);
                    String fromDate = cursor.getString(6);
                    String toDate = cursor.getString(7);
                    String TrainingCenter = cursor.getString(8);
                    String position = cursor.getString(9);
                    String ranking = cursor.getString(10);
                    String category = cursor.getString(14);
                    if (count==0) {
                        CardView mainCardView = (CardView) getLayoutInflater().inflate(R.layout.card_layout, null);
                        mainCardView.setCardBackgroundColor(Color.parseColor("#3afcf6")); // Light pink color
                        TextView uidNameTextView = mainCardView.findViewById(R.id.card_description);
                        uidNameTextView.setTextColor(Color.BLACK);
                        uidNameTextView.setGravity(Gravity.CENTER);
                        uidNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                        uidNameTextView.setText(uid + " " + name+ " " + rank+ "(" + branch+ ")," + unit);
                        layout.addView(mainCardView);
                        count = 1;
                    }

                    // Create a new card for the rest of the details
                    CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.card_layout, null);
                    TextView cardDescription = cardView.findViewById(R.id.card_description);
                    queryResult.append("Course Name: ").append(courseName).append("\n");
                    queryResult.append("From: ").append(fromDate).append("\n");
                    queryResult.append("To: ").append(toDate).append("\n");
                    queryResult.append("Center: ").append(TrainingCenter).append("\n");
                    queryResult.append("Position: ").append(position).append("\n");
                    queryResult.append("Ranking: ").append(ranking).append("\n");
                    queryResult.append("Category: ").append(category).append("\n");
                    queryResult.append("\n");

                    cardDescription.setText(queryResult.toString());
                    layout.addView(cardView);

                    Intent intent = new Intent(this, DetailsActivity.class);
                    intent.putExtra("uidno", uid);
//                // Create a new TextView to display the record
//                    TextView recordTextView = new TextView(this);
//                    recordTextView.setLayoutParams(new LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            LinearLayout.LayoutParams.WRAP_CONTENT));
//                    recordTextView.setText(queryResult);
                    // Add the TextView to the layout
               //     layout.addView(recordTextView);

                    // Add a separator between records (optional)
                    View separatorView = new View(this);
                    separatorView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1)); // Height of separator
                    separatorView.setBackgroundColor(0); // Color of separator
                    layout.addView(separatorView);


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
                    Toast.makeText(TrainingActivity.this, "Please enter a valid UID.", Toast.LENGTH_SHORT).show();
                    return;  // Stop execution if the UID is blank
                }
                executeQuery(uidNo);
            }
        });
    }


}
