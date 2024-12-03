package com.example.details;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.appbar.MaterialToolbar;
import android.app.Dialog;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";
    private SQLiteDatabase database;
    private SQLiteDatabase imagesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        // Setup toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // or for newer Android versions:
        // toolbar.setNavigationOnClickListener(v -> finish());

     //   Toast.makeText(this, "Long press on any value to copy", Toast.LENGTH_LONG).show();

        database = SQLiteDatabase.openDatabase(getDatabasePath("pims_all.db").toString(), null, SQLiteDatabase.OPEN_READONLY);

        // Initialize images database
        imagesDatabase = SQLiteDatabase.openDatabase(getDatabasePath("images_resize.db").toString(), null, SQLiteDatabase.OPEN_READONLY);

        // Retrieve uidno from intent extra
        String uid = getIntent().getStringExtra("uidno");
        searchRecords(uid);

        // Initialize buttons with Material Design IDs
        MaterialButton btnTraining = findViewById(R.id.btnTraining);
        MaterialButton btnPosting = findViewById(R.id.btnPosting);
        MaterialButton btnLeave = findViewById(R.id.btnLeave);

        btnTraining.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, TrainingActivity.class);
            intent.putExtra("uidno", uid);
            startActivity(intent);
        });

        btnPosting.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, PostingActivity.class);
            intent.putExtra("uidno", uid);
            startActivity(intent);
        });

        btnLeave.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, LeaveActivity.class);
            intent.putExtra("uidno", uid);
            startActivity(intent);
        });
    }

    private void searchRecords(String uid) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM parmanentinfo WHERE uidno LIKE ?", new String[]{"%" + uid + "%"});
            
            if (cursor.moveToFirst()) {
                displayDetails(cursor);
                loadProfileImage(uid);
            } else {
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching records", e);
            Toast.makeText(this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void displayDetails(Cursor cursor) {
        try {
            // Header section
            MaterialTextView nameText = findViewById(R.id.nameText);
            MaterialTextView rankText = findViewById(R.id.rankText);
            MaterialTextView uidText = findViewById(R.id.uidText);

            String name = getColumnValue(cursor, "name");
            String rank = getColumnValue(cursor, "rank");
            String uid = getColumnValue(cursor, "uidno");

            nameText.setText(name);
         //   rankText.setText(rank);
            uidText.setText("UID: " + uid);

            // Personal Details section
            TableLayout personalTable = findViewById(R.id.personalDetailsTable);
            personalTable.removeAllViews();

            // Date of Birth
            MaterialTextView dobLabel = new MaterialTextView(this);
            dobLabel.setText("Date Of Birth: ");
            dobLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView dobValue = new MaterialTextView(this);
            String dobText = getColumnValue(cursor, "dob");
            dobValue.setText(dobText);
            dobValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            dobValue.setTypeface(null, Typeface.BOLD);
            dobValue.setPadding(32, 0, 0, 0);
            makeCopyable(dobValue, "Date of Birth", dobText);
            
            TableRow dobRow = new TableRow(this);
            dobRow.setPadding(0, 8, 0, 8);
            dobRow.addView(dobLabel);
            dobRow.addView(dobValue);
            personalTable.addView(dobRow);

            // Blood Group
            MaterialTextView bloodGroupLabel = new MaterialTextView(this);
            bloodGroupLabel.setText("Blood Group: ");
            bloodGroupLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView bloodGroupValue = new MaterialTextView(this);
            bloodGroupValue.setText(getColumnValue(cursor, "bloodgr"));
            bloodGroupValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            bloodGroupValue.setTypeface(null, Typeface.BOLD);
            bloodGroupValue.setPadding(32, 0, 0, 0);
            
            TableRow bloodGroupRow = new TableRow(this);
            bloodGroupRow.setPadding(0, 8, 0, 8);
            bloodGroupRow.addView(bloodGroupLabel);
            bloodGroupRow.addView(bloodGroupValue);
            personalTable.addView(bloodGroupRow);

            // Religion
            MaterialTextView religionLabel = new MaterialTextView(this);
            religionLabel.setText("Religion: ");
            religionLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView religionValue = new MaterialTextView(this);
            religionValue.setText(getColumnValue(cursor, "rel_cat"));
            religionValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            religionValue.setTypeface(null, Typeface.BOLD);
            religionValue.setPadding(32, 0, 0, 0);
            
            TableRow religionRow = new TableRow(this);
            religionRow.setPadding(0, 8, 0, 8);
            religionRow.addView(religionLabel);
            religionRow.addView(religionValue);
            personalTable.addView(religionRow);

            // Father's Name
            MaterialTextView fatherLabel = new MaterialTextView(this);
            fatherLabel.setText("Father's Name: ");
            fatherLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView fatherValue = new MaterialTextView(this);
            fatherValue.setText(getColumnValue(cursor, "fathername"));
            fatherValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            fatherValue.setTypeface(null, Typeface.BOLD);
            fatherValue.setPadding(32, 0, 0, 0);
            
            TableRow fatherRow = new TableRow(this);
            fatherRow.setPadding(0, 8, 0, 8);
            fatherRow.addView(fatherLabel);
            fatherRow.addView(fatherValue);
            personalTable.addView(fatherRow);

            // Mother's Name
            MaterialTextView motherLabel = new MaterialTextView(this);
            motherLabel.setText("Mother's Name: ");
            motherLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView motherValue = new MaterialTextView(this);
            motherValue.setText(getColumnValue(cursor, "mothername"));
            motherValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            motherValue.setTypeface(null, Typeface.BOLD);
            motherValue.setPadding(32, 0, 0, 0);
            
            TableRow motherRow = new TableRow(this);
            motherRow.setPadding(0, 8, 0, 8);
            motherRow.addView(motherLabel);
            motherRow.addView(motherValue);
            personalTable.addView(motherRow);

            // Gender
            MaterialTextView genderLabel = new MaterialTextView(this);
            genderLabel.setText("Gender: ");
            genderLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView genderValue = new MaterialTextView(this);
            genderValue.setText(getColumnValue(cursor, "gen"));
            genderValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            genderValue.setTypeface(null, Typeface.BOLD);
            genderValue.setPadding(32, 0, 0, 0);
            
            TableRow genderRow = new TableRow(this);
            genderRow.setPadding(0, 8, 0, 8);
            genderRow.addView(genderLabel);
            genderRow.addView(genderValue);
            personalTable.addView(genderRow);

            // Marital Status
            MaterialTextView maritalLabel = new MaterialTextView(this);
            maritalLabel.setText("Marital Status: ");
            maritalLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView maritalValue = new MaterialTextView(this);
            maritalValue.setText(getColumnValue(cursor, "marital_st"));
            maritalValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            maritalValue.setTypeface(null, Typeface.BOLD);
            maritalValue.setPadding(32, 0, 0, 0);
            
            TableRow maritalRow = new TableRow(this);
            maritalRow.setPadding(0, 8, 0, 8);
            maritalRow.addView(maritalLabel);
            maritalRow.addView(maritalValue);
            personalTable.addView(maritalRow);

            // ID Mark
            MaterialTextView idMarkLabel = new MaterialTextView(this);
            idMarkLabel.setText("ID Mark: ");
            idMarkLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView idMarkValue = new MaterialTextView(this);
            idMarkValue.setText(getColumnValue(cursor, "idmark"));
            idMarkValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            idMarkValue.setTypeface(null, Typeface.BOLD);
            idMarkValue.setPadding(32, 0, 0, 0);
            
            TableRow idMarkRow = new TableRow(this);
            idMarkRow.setPadding(0, 8, 0, 8);
            idMarkRow.addView(idMarkLabel);
            idMarkRow.addView(idMarkValue);
            personalTable.addView(idMarkRow);

            // Contact Details section
            TableLayout contactTable = findViewById(R.id.contactDetailsTable);
            contactTable.removeAllViews();

            MaterialTextView mobLabel = new MaterialTextView(this);
            mobLabel.setText("Mobile No: ");
            mobLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView mobValue = new MaterialTextView(this);
            String mobText = getColumnValue(cursor, "mobno");
            mobValue.setText(mobText);
            mobValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            mobValue.setTypeface(null, Typeface.BOLD);
            mobValue.setPadding(32, 8, 32, 8);
            mobValue.setEnabled(true);
            mobValue.setClickable(true);
            mobValue.setFocusable(true);
            mobValue.setLongClickable(true);
            makeCopyable(mobValue, "Mobile Number", mobText);
            
            TableRow mobRow = new TableRow(this);
            mobRow.setPadding(0, 8, 0, 8);
            mobRow.addView(mobLabel);
            mobRow.addView(mobValue);
            contactTable.addView(mobRow);

            MaterialTextView homeLabel = new MaterialTextView(this);
            homeLabel.setText("Home Number: ");
            homeLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView homeValue = new MaterialTextView(this);
            homeValue.setText(getColumnValue(cursor, "homephone"));
            homeValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            homeValue.setTypeface(null, Typeface.BOLD);
            homeValue.setPadding(32, 0, 0, 0);
            
            TableRow homeRow = new TableRow(this);
            homeRow.setPadding(0, 8, 0, 8);
            homeRow.addView(homeLabel);
            homeRow.addView(homeValue);
            contactTable.addView(homeRow);

            MaterialTextView emailLabel = new MaterialTextView(this);
            emailLabel.setText("Email: ");
            emailLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            
            MaterialTextView emailValue = new MaterialTextView(this);
            emailValue.setText(getColumnValue(cursor, "eMail"));
            emailValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            emailValue.setTypeface(null, Typeface.BOLD);
            emailValue.setPadding(32, 0, 0, 0);
            
            TableRow emailRow = new TableRow(this);
            emailRow.setPadding(0, 8, 0, 8);
            emailRow.addView(emailLabel);
            emailRow.addView(emailValue);
            contactTable.addView(emailRow);

        } catch (Exception e) {
            Log.e(TAG, "Error displaying details", e);
            Toast.makeText(this, "Error displaying details", Toast.LENGTH_SHORT).show();
        }
    }

    private String getColumnValue(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                String value = cursor.getString(columnIndex);
                return value != null ? value : "Not Available";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting column value for " + columnName, e);
        }
        return "Not Available";
    }

    private void addTableRow(TableLayout table, String label, String value) {
        TableRow row = new TableRow(this);
        row.setPadding(0, 8, 0, 8);

        MaterialTextView labelView = new MaterialTextView(this);
        labelView.setText(label + ": ");
        labelView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        labelView.setTextColor(getColor(R.color.card_label_text));
        labelView.setTypeface(null, Typeface.BOLD);

        MaterialTextView valueView = new MaterialTextView(this);
        valueView.setText(value);
        valueView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        valueView.setTextColor(getColor(R.color.card_value_text));
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setPadding(32, 0, 0, 0);
        
        makeCopyable(valueView, label, value);

        row.addView(labelView);
        row.addView(valueView);
        table.addView(row);

        // Add divider
        View divider = new View(this);
        divider.setBackgroundColor(getColor(R.color.divider_color));
        divider.setAlpha(0.3f);  // Increased divider opacity
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2  // Increased divider thickness
        );
        dividerParams.topMargin = 8;
        dividerParams.bottomMargin = 8;
        divider.setLayoutParams(dividerParams);
        table.addView(divider);
    }

    private void makeCopyable(MaterialTextView textView, String label, String value) {
        // Enable interaction
        textView.setEnabled(true);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setLongClickable(true); // Make sure long click is enabled
        
        // Add ripple effect for feedback
        textView.setForeground(getDrawable(android.R.drawable.list_selector_background));
        
        // Set long click listener
        textView.setOnLongClickListener(v -> {
            try {
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = 
                    android.content.ClipData.newPlainText(label, value);
                clipboard.setPrimaryClip(clip);
                
                // Vibrate for feedback
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                
                // Show toast
                Toast.makeText(this, label + " copied!", Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error copying to clipboard", e);
                Toast.makeText(this, "Failed to copy text", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Add click hint
        textView.setOnClickListener(v -> {
            Toast.makeText(this, "Long press to copy " + label, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProfileImage(String uid) {
        ShapeableImageView profileImage = findViewById(R.id.profileImage);
        Cursor cursor = null;
        try {
            cursor = imagesDatabase.rawQuery(
                "SELECT image FROM images WHERE UIDNO = ?",
                new String[]{uid}
            );

            if (cursor != null && cursor.moveToFirst()) {
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex("image"));
                if (imageBytes != null && imageBytes.length > 0) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = 1;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                    if (bitmap != null) {
                        profileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        profileImage.setImageBitmap(bitmap);
                        
                        // Add click listener to show full-size image
                        profileImage.setOnClickListener(v -> showFullSizeImage(bitmap));
                    } else {
                        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image", e);
            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void showFullSizeImage(Bitmap bitmap) {
        // Create dialog
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_profile_image);
        
        // Get the ShapeableImageView from dialog
        ShapeableImageView fullSizeImage = dialog.findViewById(R.id.fullSizeImage);
        fullSizeImage.setImageBitmap(bitmap);
        
        // Add click listener to dismiss dialog when clicking anywhere
        dialog.findViewById(android.R.id.content).setOnClickListener(v -> dialog.dismiss());
        
        // Show dialog
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (imagesDatabase != null && imagesDatabase.isOpen()) {
            imagesDatabase.close();
        }
    }
}
