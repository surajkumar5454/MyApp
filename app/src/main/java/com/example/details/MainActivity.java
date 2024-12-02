package com.example.details;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

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
    }
}