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
import java.util.Arrays;
import java.util.List;

public class AllDynamicSearchActivity extends AppCompatActivity {

    private LinearLayout layoutDynamicFilters;
    private TextView textViewResult;
    private Button btnAddFilter, btnQuery;
    private ListView listViewResults;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_dynamic_search_activity);

        layoutDynamicFilters = findViewById(R.id.layoutDynamicFilters);
        btnAddFilter = findViewById(R.id.btnAddFilter);
        btnQuery = findViewById(R.id.btnQuery);
        listViewResults = findViewById(R.id.listViewResults);
      //  textViewResult = findViewById(R.id.textViewResult);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();
        // Initialize database
      //  database = SQLiteDatabase.openDatabase(getDatabasePath("pims_all.db").toString(), null, SQLiteDatabase.OPEN_READONLY);
        //database = openOrCreateDatabase("YourDatabaseName", MODE_PRIVATE, null);

        // Add initial filter views
        addFilterViews();

        // Add filter button click listener
        btnAddFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFilterViews();
            }
        });

        // Query button click listener
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeQuery();
            }
        });
    }

    private void addFilterViews() {
        LinearLayout filterLayout = new LinearLayout(this);
        filterLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        filterLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Add spinner for column names
        Spinner spinner = new Spinner(this);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        List<String> columnNames = getColumnNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, columnNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        filterLayout.addView(spinner);

        // Add AutoCompleteTextView for value
        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this);
        autoCompleteTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        autoCompleteTextView.setHint("Enter Value");
        filterLayout.addView(autoCompleteTextView);

        // Add "Remove Filter" button
        Button btnRemoveFilter = new Button(this);
        btnRemoveFilter.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btnRemoveFilter.setText("X");
        btnRemoveFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDynamicFilters.removeView(filterLayout);
            }
        });
        filterLayout.addView(btnRemoveFilter);

        layoutDynamicFilters.addView(filterLayout);

        // Set adapter for AutoCompleteTextView when the spinner selection changes
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Fetch column values from the database for the selected column
                String selectedColumn = (String) parent.getItemAtPosition(position);
                List<String> columnValues = getColumnValues(selectedColumn);
                ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(AllDynamicSearchActivity.this, android.R.layout.simple_dropdown_item_1line, columnValues);
                autoCompleteTextView.setAdapter(autoCompleteAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private List<String> getColumnValues(String columnName) {
        List<String> values = new ArrayList<>();
        Cursor cursor = database.query(true, "parmanentinfo", new String[]{columnName}, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String value = cursor.getString(cursor.getColumnIndex(columnName));
                values.add(value);
            }
            cursor.close();
        }
        values.add(" ");
        return values;
    }

    private List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<>();
        Cursor cursor = database.rawQuery("PRAGMA table_info(parmanentinfo)", null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String columnName = cursor.getString(cursor.getColumnIndex("name"));
                columnNames.add(columnName);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        columnNames.add("uidno");
//        columnNames.add("Name");
        return columnNames;
    }


    private void executeQuery() {
        // Build and execute the query
//        String query = "SELECT * FROM parmanentinfo WHERE ";
        String query = "  SELECT DISTINCT \n" +
                "      p.uidno\n" +
                "      ,p.name\n" +
                "\t  ,u.unit_nm\n" +
                "\t  ,r.rnk_nm\n" +
                "\t  ,r.brn_nm\n" +
                "\n" +
                "  FROM parmanentinfo p,joininfo j, unitdep u, rnk_brn_mas r where j.unit = u.unit_cd\n" +
                "  and p.uidno=j.uidno and j.rank=r.rnk_cd and j.branch=r.brn_cd and j.dateofrelv is NULL AND ";
        List<String> conditions = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < layoutDynamicFilters.getChildCount(); i++) {
            LinearLayout filterLayout = (LinearLayout) layoutDynamicFilters.getChildAt(i);
            Spinner spinner = (Spinner) filterLayout.getChildAt(0);
            EditText editText = (EditText) filterLayout.getChildAt(1);
            String columnName = spinner.getSelectedItem().toString();
            String modifycolumn = "p.".concat(columnName);
            String value = editText.getText().toString();
            System.out.println("Value: "+value);

            if (!value.isEmpty()) {
                conditions.add(modifycolumn + " LIKE ?");
                values.add("%" + value + "%");
                System.out.println("Values: "+values);

            }
        }
        if (conditions.isEmpty()) {
            query = "SELECT * FROM parmanentinfo";
//            query = "  SELECT DISTINCT \n" +
//                    "      uidno\n" +
//                    "      ,name\n" +
//                    "\t  ,u.unit_nm\n" +
//                    "\t  ,r.rnk_nm\n" +
//                    "\t  ,r.brn_nm\n" +
//                    "\n" +
//                    "  FROM joininfo j, unitdep u, rnk_brn_mas r where j.unit = u.unit_cd\n" +
//                    "  and j.rank=r.rnk_cd and j.branch=r.brn_cd and j.dateofrelv is NULL";
        } else {
            query += " " + String.join(" AND ", conditions);
        }
        System.out.println("Query: "+query);
        executeQuery(query, values.toArray(new String[0]));

    }

    private void executeQuery(String query, String[] values) {

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, values);
            LinearLayout layout = findViewById(R.id.detailsLayout); // Assuming you have a LinearLayout with id "detailsLayout" in your activity_details.xml
            layout.removeAllViews();
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder queryResult = new StringBuilder();
                    @SuppressLint("Range") String uid = cursor.getString(cursor.getColumnIndex("uidno"));
            //        @SuppressLint("Range") String rank = cursor.getString(cursor.getColumnIndex("Rank"));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                    @SuppressLint("Range") String unit = cursor.getString(cursor.getColumnIndex("unit_nm"));
                    @SuppressLint("Range") String rank = cursor.getString(cursor.getColumnIndex("rnk_nm"));
                    @SuppressLint("Range") String branch = cursor.getString(cursor.getColumnIndex("brn_nm"));


                    queryResult.append(uid).append(" ").append(name).append(", ").append(rank).append("(").append(branch).append("), ").append(unit);
                 //   queryResult.append("Name:").append(name).append("\n");
               //     queryResult.append("name:").append(name).append("\n");
                //    queryResult.append("\n");

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
                            startActivity(intent);
                            executeQuery("SELECT * FROM permanent_info WHERE UID_No=?", new String[]{uid});
                        }
                    });

                } while (cursor.moveToNext());
            }
            else {
                // If no records were found, display a message to the user
                Toast.makeText(this, "No records found with the value : " + Arrays.toString(values), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
