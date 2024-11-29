package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    private TextView textViewResult;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        textViewResult = findViewById(R.id.textViewUid);
        database = SQLiteDatabase.openDatabase(getDatabasePath("pims_all.db").toString(), null, SQLiteDatabase.OPEN_READONLY);

        // Retrieve uidno from intent extra
        String uid = getIntent().getStringExtra("uidno");
        searchRecords(uid);

        Button btn_punishment = findViewById(R.id.btn_punishment);
        btn_punishment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, PunishmentActivity.class);
                intent.putExtra("uidno", uid);
                startActivity(intent);
            }
        });

        Button btn_training = findViewById(R.id.btn_training);
        btn_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DetailsActivity.this, TrainingActivity.class);
                intent.putExtra("uidno", uid);
                startActivity(intent);
            }
        });

        Button btn_apar = findViewById(R.id.btn_apar);
        btn_apar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, AparActivity.class);
                intent.putExtra("uidno", uid);
                startActivity(intent);
            }
        });

        Button btn_posting = findViewById(R.id.btn_posting);
        btn_posting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, PostingActivity.class);
                intent.putExtra("uidno", uid);
                startActivity(intent);
            }
        });

        Button btn_leave = findViewById(R.id.btn_leave);
        btn_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, LeaveActivity.class);
                intent.putExtra("uidno", uid);
                startActivity(intent);
            }
        });
    }

    private void searchRecords(String uid) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM parmanentinfo WHERE uidno LIKE ?", new String[]{"%" + uid + "%"});
            StringBuilder queryResult = new StringBuilder();

            if (cursor.moveToFirst()) {
                do {
                    String[] columnNames = cursor.getColumnNames();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columnName = columnNames[i];
                        String columnValue = cursor.getString(i);
                        // Format the output with HTML for better readability
                        queryResult.append("<b>").append(columnName).append(":</b> ").append(columnValue).append("<br/>");
                    }
                    queryResult.append("<br/><br/>"); // Separate rows with double line break
                } while (cursor.moveToNext());
            }

            String result = queryResult.toString();
            if (!result.isEmpty()) {
                textViewResult.setText(Html.fromHtml(result)); // Use HTML for formatting
            } else {
                textViewResult.setText("");
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
