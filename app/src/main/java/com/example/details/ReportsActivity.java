package com.example.details;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;
import android.widget.ScrollView;

public class ReportsActivity extends AppCompatActivity {
    
    private TextView personnelStatsText;
    private TextView trainingStatsText;
    private TextView postingStatsText;
    private TextView leaveStatsText;
    private CardView refreshButton;
    private ProgressBar progressBar;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        
        // Initialize database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createDatabase();
        database = dbHelper.getDatabase();
        
        // Initialize views
        personnelStatsText = findViewById(R.id.personnelStatsText);
        trainingStatsText = findViewById(R.id.trainingStatsText);
        postingStatsText = findViewById(R.id.postingStatsText);
        leaveStatsText = findViewById(R.id.leaveStatsText);
        refreshButton = findViewById(R.id.refreshButton);
        progressBar = findViewById(R.id.progressBar);
        
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadReports();
            }
        });
        
        loadReports();
    }
    
    private void loadReports() {
        new LoadReportsTask().execute();
    }
    
    private class LoadReportsTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            refreshButton.setEnabled(false);
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            String[] results = new String[4];
            results[0] = loadPersonnelStats();
            results[1] = loadTrainingStats();
            results[2] = loadPostingStats();
            results[3] = loadLeaveStats();
            return results;
        }

        @Override
        protected void onPostExecute(String[] results) {
            personnelStatsText.setText(getPersonnelPreview());
            trainingStatsText.setText(getTrainingPreview());
            postingStatsText.setText(getPostingPreview());
            leaveStatsText.setText(getLeavePreview());
            
            // Set click listeners for each card
            setupCardClickListeners(results);
            
            progressBar.setVisibility(View.GONE);
            refreshButton.setEnabled(true);
        }
    }
    
    private String loadPersonnelStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Personnel Statistics:\n\n");
        
        try {
            // Active Personnel Count
            Cursor cursor = database.rawQuery(
                "SELECT COUNT(DISTINCT p.uidno) " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL AND p.rlvflag = 0.0", null);
            if (cursor != null && cursor.moveToFirst()) {
                stats.append("Total Active Personnel: ").append(cursor.getInt(0)).append("\n\n");
            }
            if (cursor != null) cursor.close();

            // Rank Distribution
            cursor = database.rawQuery(
                "SELECT r.rnk_nm, COUNT(DISTINCT p.uidno) as count " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL AND p.rlvflag = 0.0 " +
                "GROUP BY r.rnk_nm, r.rnk_cd " +
                "ORDER BY r.rnk_cd ASC", null);
            stats.append("Rank Distribution:\n");
            while (cursor != null && cursor.moveToNext()) {
                String rankName = cursor.getString(0);
                int count = cursor.getInt(1);
                if (count > 0) {
                    stats.append(rankName).append(": ")
                         .append(count).append("\n");
                }
            }
            if (cursor != null) cursor.close();

        } catch (Exception e) {
            stats.append("Error: ").append(e.getMessage());
        }
        
        return stats.toString();
    }
    
    private String loadTrainingStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Training Statistics:\n\n");
        
        try {
            // Recent Training Count
            Cursor cursor = database.rawQuery(
                "SELECT COUNT(DISTINCT t.uidno) " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN training t ON p.uidno = t.uidno " +
                "JOIN trainingcourse c ON t.course = c.id " +
                "WHERE j.dateofrelv IS NULL AND p.rlvflag = 0.0 " +
                "AND date(t.toDate) >= date('now', '-6 months')", null);
            if (cursor != null && cursor.moveToFirst()) {
                stats.append("Personnel Trained (Last 6 months): ")
                     .append(cursor.getInt(0)).append("\n\n");
            }
            if (cursor != null) cursor.close();

            // Popular Courses
            cursor = database.rawQuery(
                "SELECT c.course_nm, COUNT(DISTINCT t.uidno) as count " +
                "FROM training t " +
                "JOIN trainingcourse c ON t.course = c.id " +
                "JOIN parmanentinfo p ON p.uidno = t.uidno " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "WHERE j.dateofrelv IS NULL AND p.rlvflag = 0.0 " +
                "GROUP BY c.course_nm " +
                "ORDER BY count DESC LIMIT 5", null);
            stats.append("Top Training Courses:\n");
            while (cursor != null && cursor.moveToNext()) {
                stats.append(cursor.getString(0)).append(": ")
                     .append(cursor.getInt(1)).append("\n");
            }
            if (cursor != null) cursor.close();

        } catch (Exception e) {
            stats.append("Error: ").append(e.getMessage());
        }
        
        return stats.toString();
    }
    
    private String loadPostingStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Posting Insights:\n\n");
        
        try {
            // Unit Distribution
            Cursor cursor = database.rawQuery(
                "SELECT u.unit_nm, COUNT(DISTINCT p.uidno) as count " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL AND p.rlvflag = 0.0 " +
                "GROUP BY u.unit_nm " +
                "ORDER BY u.unit_nm ASC", null);
            stats.append("Unit Distribution:\n");
            while (cursor != null && cursor.moveToNext()) {
                stats.append(cursor.getString(0)).append(": ")
                     .append(cursor.getInt(1)).append("\n");
            }
            if (cursor != null) cursor.close();

        } catch (Exception e) {
            stats.append("Error: ").append(e.getMessage());
        }
        
        return stats.toString();
    }
    
    private String loadLeaveStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Leave Analysis:\n\n");
        
        try {
            // Current Leave Count
            Cursor cursor = database.rawQuery(
                "SELECT COUNT(DISTINCT l.uidno) " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN tblLeave l ON p.uidno = l.uidno " +
                "JOIN unitdep u ON u.unit_cd = j.unit " +
                "JOIN rnk_brn_mas r ON j.rank = r.rnk_cd AND j.branch = r.brn_cd " +
                "WHERE j.dateofrelv IS NULL AND p.rlvflag = 0.0 " +
                "AND date(l.LeaveTo) >= date('now') " +
                "AND date(l.LeaveFrom) <= date('now')", null);
            if (cursor != null && cursor.moveToFirst()) {
                stats.append("Currently on Leave: ")
                     .append(cursor.getInt(0)).append("\n\n");
            }
            if (cursor != null) cursor.close();

            // Leave Type Distribution
            cursor = database.rawQuery(
                "SELECT l.LeaveType, COUNT(DISTINCT l.uidno) as count " +
                "FROM parmanentinfo p " +
                "JOIN joininfo j ON p.uidno = j.uidno " +
                "JOIN tblLeave l ON p.uidno = l.uidno " +
                "WHERE j.dateofrelv IS NULL AND p.rlvflag = 0.0 " +
                "AND date(l.LeaveFrom) >= date('now', 'start of year') " +
                "GROUP BY l.LeaveType " +
                "ORDER BY count DESC", null);
            stats.append("Leave Type Distribution (This Year):\n");
            while (cursor != null && cursor.moveToNext()) {
                stats.append(cursor.getString(0)).append(": ")
                     .append(cursor.getInt(1)).append("\n");
            }
            if (cursor != null) cursor.close();

        } catch (Exception e) {
            stats.append("Error: ").append(e.getMessage());
        }
        
        return stats.toString();
    }
    
    private void showFullReport(String title, String report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Create ScrollView to make long content scrollable
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(report);
        textView.setPadding(30, 30, 30, 30);
        textView.setTextSize(16);
        scrollView.addView(textView);

        builder.setView(scrollView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }
    
    private String getPersonnelPreview() {
        String fullStats = loadPersonnelStats();
        String[] lines = fullStats.split("\n");
        StringBuilder preview = new StringBuilder();
        
        // Show first 3 lines of stats
        int count = 0;
        for (String line : lines) {
            preview.append(line).append("\n");
            count++;
            if (count >= 3) {
                preview.append("...\nTap to view more");
                break;
            }
        }
        return preview.toString();
    }
    
    private String getTrainingPreview() {
        String fullStats = loadTrainingStats();
        String[] lines = fullStats.split("\n");
        StringBuilder preview = new StringBuilder();
        
        int count = 0;
        for (String line : lines) {
            preview.append(line).append("\n");
            count++;
            if (count >= 3) {
                preview.append("...\nTap to view more");
                break;
            }
        }
        return preview.toString();
    }
    
    private String getPostingPreview() {
        String fullStats = loadPostingStats();
        String[] lines = fullStats.split("\n");
        StringBuilder preview = new StringBuilder();
        
        int count = 0;
        for (String line : lines) {
            preview.append(line).append("\n");
            count++;
            if (count >= 3) {
                preview.append("...\nTap to view more");
                break;
            }
        }
        return preview.toString();
    }
    
    private String getLeavePreview() {
        String fullStats = loadLeaveStats();
        String[] lines = fullStats.split("\n");
        StringBuilder preview = new StringBuilder();
        
        int count = 0;
        for (String line : lines) {
            preview.append(line).append("\n");
            count++;
            if (count >= 3) {
                preview.append("...\nTap to view more");
                break;
            }
        }
        return preview.toString();
    }
    
    private void setupCardClickListeners(final String[] fullReports) {
        View personnelCard = findViewById(R.id.personnelCard);
        View trainingCard = findViewById(R.id.trainingCard);
        View postingCard = findViewById(R.id.postingCard);
        View leaveCard = findViewById(R.id.leaveCard);

        personnelCard.setOnClickListener(v -> 
            showFullReport("Personnel Statistics", fullReports[0]));
        
        trainingCard.setOnClickListener(v -> 
            showFullReport("Training Statistics", fullReports[1]));
        
        postingCard.setOnClickListener(v -> 
            showFullReport("Posting Insights", fullReports[2]));
        
        leaveCard.setOnClickListener(v -> 
            showFullReport("Leave Analysis", fullReports[3]));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
} 