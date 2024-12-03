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

            // Personal Details
            TableLayout personalTable = findViewById(R.id.personalDetailsTable);
            personalTable.removeAllViews();
            addTableRow(personalTable, "Date of Birth", getColumnValue(cursor, "dob"));
            addTableRow(personalTable, "Gender", getColumnValue(cursor, "gen"));
            addTableRow(personalTable, "Father's Name", getColumnValue(cursor, "fathername"));
            addTableRow(personalTable, "Mother's Name", getColumnValue(cursor, "mothername"));
            addTableRow(personalTable, "Marital Status", getColumnValue(cursor, "marital_st"));
            addTableRow(personalTable, "Education", getColumnValue(cursor, "education"));
            addTableRow(personalTable, "Category", getCasteCategoryName(cursor.getString(cursor.getColumnIndex("caste_cat"))));
            addTableRow(personalTable, "ID Mark", getColumnValue(cursor, "idmark"));
            addTableRow(personalTable, "PAN", getColumnValue(cursor, "pan"));
            addTableRow(personalTable, "Aadhar", getColumnValue(cursor, "old_aadhar"));

            // Address Details
            TableLayout addressTable = findViewById(R.id.addressDetailsTable);
            addressTable.removeAllViews();
            addTableRow(addressTable, "Spouse Name", getColumnValue(cursor, "hname"));
            addTableRow(addressTable, "Address", getColumnValue(cursor, "paddress"));
            addTableRow(addressTable, "Police Station", getColumnValue(cursor, "policestation"));
            addTableRow(addressTable, "District", getDistrictName(cursor.getString(cursor.getColumnIndex("district"))));
            addTableRow(addressTable, "State", getStateName(cursor.getString(cursor.getColumnIndex("state"))));
            addTableRow(addressTable, "Tahsil", getColumnValue(cursor, "tahsil"));
            addTableRow(addressTable, "Pin Code", getColumnValue(cursor, "pincode"));

            // Service Details
            TableLayout serviceTable = findViewById(R.id.serviceDetailsTable);
            serviceTable.removeAllViews();
            addTableRow(serviceTable, "Unit", getUnitName(cursor.getString(cursor.getColumnIndex("unit"))));
            addTableRow(serviceTable, "Rank", getRankName(cursor.getString(cursor.getColumnIndex("rank"))));
            addTableRow(serviceTable, "Cadre", getCadreName(cursor.getString(cursor.getColumnIndex("cadre"))));
            addTableRow(serviceTable, "Date of Joining", getDateOfJoining(cursor.getString(cursor.getColumnIndex("uidno"))));
            addTableRow(serviceTable, "Date of Present Rank", getColumnValue(cursor, "datepresentrank"));

            // Bank Details
            TableLayout bankTable = findViewById(R.id.bankDetailsTable);
            bankTable.removeAllViews();
            addTableRow(bankTable, "Bank Name", getColumnValue(cursor, "bankname"));
            addTableRow(bankTable, "Account Number", getColumnValue(cursor, "account"));

            // Contact Details (existing)
            TableLayout contactTable = findViewById(R.id.contactDetailsTable);
            contactTable.removeAllViews();
            addTableRow(contactTable, "Mobile", getColumnValue(cursor, "mobno"));
            addTableRow(contactTable, "Home Phone", getColumnValue(cursor, "homephone"));
            addTableRow(contactTable, "Email", getColumnValue(cursor, "eMail"));

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

    private String getStateName(String id) {
        if (id == null || id.isEmpty()) return "Not Available";
        
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT state_nm FROM district WHERE state_cd = ?",
                new String[]{id}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting state name", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Not Available";
    }

    private String getDistrictName(String id) {
        if (id == null || id.isEmpty()) return "Not Available";
        
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT dist_nm FROM district WHERE dist_cd = ?",
                new String[]{id}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting district name", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Not Available";
    }

    private String getCasteCategoryName(String id) {
        if (id == null || id.isEmpty()) return "Not Available";
        
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT caste_cat FROM caste WHERE casteCat_cd = ?",
                new String[]{id}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting caste category", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Not Available";
    }

    private String getUnitName(String id) {
        if (id == null || id.isEmpty()) return "Not Available";
        
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT unit_nm FROM unitdep WHERE unit_cd = ?",
                new String[]{id}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting unit name", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Not Available";
    }

    private String getRankName(String id) {
        if (id == null || id.isEmpty()) return "Not Available";
        
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT rnk_nm FROM rnk_brn_mas WHERE rnk_cd = ?",
                new String[]{id}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting rank name", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Not Available";
    }

    private String getCadreName(String id) {
        if (id == null || id.isEmpty()) return "Not Available";
        
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT brn_nm FROM rnk_brn_mas WHERE brn_cd = ?",
                new String[]{id}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cadre name", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Not Available";
    }

    private String getDateOfJoining(String uid) {
        if (uid == null || uid.isEmpty()) return "Not Available";
        
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT dateofjoin FROM joininfo WHERE uidno = ? AND dateofrelv IS NULL",
                new String[]{uid}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting date of joining", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Not Available";
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
