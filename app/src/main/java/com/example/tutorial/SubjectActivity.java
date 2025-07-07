package com.example.tutorial;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SubjectActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<TaskItem> taskList;
    Button Testbutton,logout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        // Retrieve subject and class
        String subject = getIntent().getStringExtra("subject_name");
        String classValue = getIntent().getStringExtra("class");

        // Display the subject in a Toast and TextView
        Toast.makeText(this, subject, Toast.LENGTH_SHORT).show();
        TextView subjectTextView = findViewById(R.id.subject_name);
        subjectTextView.setText(subject != null ? subject : "No subject received");

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(adapter);
        Testbutton=findViewById(R.id.TestButton);

        logout=findViewById(R.id.logoutButton);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(SubjectActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        Testbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SubjectActivity.this,TestActivity.class);
                Toast.makeText(SubjectActivity.this, "Test Activity", Toast.LENGTH_SHORT).show();
                intent.putExtra("Class",classValue);
                intent.putExtra("Subject",subject);
                startActivity(intent);
                finish();
            }
        });

        // Initialize Firebase reference: Pdfs/Class<ClassValue>/<Subject>
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Pdfs")
                .child("Class" + classValue)
                .child(subject);

        // Fetch data from Firebase
        loadTasks();
    }

    private void loadTasks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();  // Clear old list
                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    Helper1 task = taskSnap.getValue(Helper1.class);
                    if (task != null && task.getTaskName() != null && task.getTaskContent() != null) {
                        taskList.add(new TaskItem(task.getTaskName(), task.getTaskContent()));
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SubjectActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
