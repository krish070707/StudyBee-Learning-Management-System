package com.example.tutorial;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class AdminActivity extends AppCompatActivity {

    FloatingActionButton fab;
    EditText cla, tsk,cpt;
    Button upl, selectFileButton,logout;
    ProgressBar progressBar;
    private Uri selectedFileUri;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        String fileName = selectedFileUri.getLastPathSegment();
                        Toast.makeText(this, "File selected: " + fileName, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        cla = findViewById(R.id.editTextNumber);
        tsk = findViewById(R.id.taskNumber);
        upl = findViewById(R.id.uploadButton);
        selectFileButton = findViewById(R.id.selectFileButton);
        fab = findViewById(R.id.floatbutton);
        cpt=findViewById(R.id.caption);
        progressBar = findViewById(R.id.uploadProgressBar);
        logout=findViewById(R.id.Logout_button);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        //to pick a file from device
        selectFileButton.setOnClickListener(v -> {
            // Allow both .txt and .pdf
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            String[] mimeTypes = {"text/plain", "application/pdf"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            //startActivityForResult(intent, PICK_FILE); // you can rename this to PICK_FILE if you want
            filePickerLauncher.launch(intent);

        });

        //when this button is clicked, it shows progress bar and it uploads the data using Helper1 class
        upl.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String classValue = cla.getText().toString().trim();
            String rawTask = tsk.getText().toString().trim();
            String caption=cpt.getText().toString().trim();

            if (caption.isEmpty() ||classValue.isEmpty() || rawTask.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedFileUri == null) {
                Toast.makeText(AdminActivity.this, "Please select a file", Toast.LENGTH_SHORT).show();
                return;
            }
            String task = String.format("%02d", Integer.parseInt(rawTask)); // ensures Task01, Task02...
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Class/Class" + classValue);
            databaseReference.child("Task" + task).get().addOnSuccessListener(dataSnapshot -> {
                //checks if task already exits in the database else uploads
                if (dataSnapshot.exists()) {
                    Toast.makeText(AdminActivity.this, "Error: Task " + task + " already exists!", Toast.LENGTH_LONG).show();
                } else {
                    uploadFileToFirebaseStorage(classValue, task, selectedFileUri, downloadUrl -> {
                        Helper1 ob = new Helper1(caption, downloadUrl);
                        //reference.child(username).setValue(helperClass)
                        databaseReference.child("Task" + task).setValue(ob); // Save dummy flag or file reference if needed
                        Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                        cla.setText("");
                        tsk.setText("");
                        cpt.setText("");
                        selectedFileUri = null;
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(AdminActivity.this, "Database check failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
        //floating button for mcq upload
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, UploadActivity.class);
            startActivity(intent);
            finish();
        });
    }

    //file upload method
    public void uploadFileToFirebaseStorage(String classValue, String task, Uri fileUri, UploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String mimeType = getContentResolver().getType(fileUri);//gets the type of the file selected
        StorageReference fileRef;
        if ("text/plain".equals(mimeType)) {
            fileRef = storageRef.child("files/Class" + classValue + "/Task" + task + ".txt");
        } else if ("application/pdf".equals(mimeType)) {
            fileRef = storageRef.child("files/Class" + classValue + "/Task" + task + ".pdf");
        } else {
            Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
            return;
        }
        //Sets Firebase Storage path based on file type.
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    progressBar.setVisibility(View.GONE); // hide on success
                    callback.onUploadSuccess(downloadUri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE); // hide on success
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        //Uploads the file, gets download URL, calls onUploadSuccess.
    }

    //handles file picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
            String fileName = selectedFileUri.getLastPathSegment();
            Toast.makeText(this, "File selected: " + fileName, Toast.LENGTH_SHORT).show();
        }
    }
}
//A simple interface to handle actions after successful file upload.
interface UploadCallback {
    void onUploadSuccess(String downloadUrl);
}