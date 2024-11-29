package com.example.details;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();

        Button btn_namesearch = findViewById(R.id.btn_allsearch);
        btn_namesearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AllDynamicSearchActivity.class);
                startActivity(intent);
            }
        });

        Button btn_punishment = findViewById(R.id.btn_punishment);
        btn_punishment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PunishmentActivity.class);
                startActivity(intent);
            }
        });

        Button btn_training = findViewById(R.id.btn_training);
        btn_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
                startActivity(intent);
            }
        });

        Button btn_apar = findViewById(R.id.btn_apar);
        btn_apar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AparActivity.class);
                startActivity(intent);
            }
        });

        Button btn_posting = findViewById(R.id.btn_posting);
        btn_posting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostingActivity.class);
                startActivity(intent);
            }
        });

        Button btn_leave = findViewById(R.id.btn_leave);
        btn_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LeaveActivity.class);
                startActivity(intent);
            }
        });

        Button btn_unitSearch = findViewById(R.id.btn_unitsearch);
        btn_unitSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnitSearchActivity.class);
                startActivity(intent);
            }
        });
        Button btn_rankSearch = findViewById(R.id.btn_ranksearch);
        btn_rankSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RankSearchActivity.class);
                startActivity(intent);
            }
        });

        Button btn_tblSearch = findViewById(R.id.btn_tblsearch);
        btn_tblSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TableSearchActivity.class);
                startActivity(intent);
            }
        });

    }
}