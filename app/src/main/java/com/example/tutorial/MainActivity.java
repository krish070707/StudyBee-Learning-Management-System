package com.example.tutorial;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button refreshBtn,logout;
    ListView fileListView;
    ArrayAdapter<String> fileAdapter;

    ArrayList<String> fileNameList;
    Map<String, String> urlMap;
    DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String ClassValue = getIntent().getStringExtra("ClassValue");
        if (ClassValue != null) {
            // Use the classValue (e.g., display it or load data)
            Toast.makeText(this, "Class: " + ClassValue, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No class info found!", Toast.LENGTH_SHORT).show();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Class/Class"+ClassValue);
        urlMap = new HashMap<>();

        refreshBtn = findViewById(R.id.showTasks);
        logout= findViewById(R.id.logoutButton);

        Button mathsButton = findViewById(R.id.Maths);
        Button physicsButton = findViewById(R.id.Physics);
        Button englishButton = findViewById(R.id.English);
        Button chemistryButton = findViewById(R.id.Chemistry);
        Button biologyButton = findViewById(R.id.Biology);
        Button hindiButton = findViewById(R.id.Hindi);
        Button sstButton = findViewById(R.id.sst);
        Button computerButton = findViewById(R.id.Computer);
        fileListView = findViewById(R.id.taskListView);
        fileNameList = new ArrayList<>();
        fileAdapter = new FileAdapter(this, fileNameList, urlMap);
        fileListView.setAdapter(fileAdapter);

        refreshBtn.setOnClickListener(v -> fetchFilesFromFirebase());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Toast.makeText(MainActivity.this, "Sucessfully Logged Out", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            }
        });

        // Set OnClickListeners for each button
        mathsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("Maths",ClassValue); // Passing the subject name "Maths"
            }
        });

        physicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("Physics",ClassValue); // Passing the subject name "Science"
            }
        });

        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("English",ClassValue); // Passing the subject name "English"
            }
        });

        chemistryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("Chemistry",ClassValue); // Passing the subject name "History"
            }
        });

        biologyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("Biology",ClassValue); // Passing the subject name "Geography"
            }
        });

        hindiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("Hindi",ClassValue); // Passing the subject name "Hindi"
            }
        });

        sstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("SST",ClassValue); // Passing the subject name "Bengali"
            }
        });

        computerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubjectActivity("Computer",ClassValue); // Passing the subject name "Computer"
            }
        });

    }
    // Helper method to open SubjectActivity and pass subject name
    private void openSubjectActivity(String subjectName,String classs) {
        Intent intent = new Intent(MainActivity.this, SubjectActivity.class);
        intent.putExtra("subject_name", subjectName); // Pass subject name
        intent.putExtra("class", classs); // Pass class
        startActivity(intent); // Launches new activity
    }

    private void fetchFilesFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fileNameList.clear();
                urlMap.clear();

                for (DataSnapshot fileSnap : snapshot.getChildren()) {
                    Helper1 task = fileSnap.getValue(Helper1.class);
                    String key = fileSnap.getKey(); // Task1, Task2, etc.

                    if (task != null && task.getTaskName() != null && task.getTaskContent() != null) {
                        String displayName = task.getTaskName() + " (" + key + ")";
                        fileNameList.add(displayName);
                        urlMap.put(displayName, task.getTaskContent());
                    }
                }

                // Instead of notifyDataSetChanged(), recreate adapter to fully refresh
                fileAdapter = new FileAdapter(MainActivity.this, fileNameList, urlMap);
                fileListView.setAdapter(fileAdapter);

                if (fileNameList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No tasks found", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(MainActivity.this, "Total items in list: "+ fileNameList.size(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}