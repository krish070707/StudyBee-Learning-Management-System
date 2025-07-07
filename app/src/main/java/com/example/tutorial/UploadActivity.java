package com.example.tutorial;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UploadActivity extends AppCompatActivity {
    private EditText classEditText, subjectEditText, questionEditText,
            optionAEditText, optionBEditText, optionCEditText, optionDEditText, answerEditText;
    private Button uploadMcqButton,backButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        classEditText = findViewById(R.id.classEditText);
        subjectEditText = findViewById(R.id.subjectEditText);
        questionEditText = findViewById(R.id.questionEditText);
        optionAEditText = findViewById(R.id.optionAEditText);
        optionBEditText = findViewById(R.id.optionBEditText);
        optionCEditText = findViewById(R.id.optionCEditText);
        optionDEditText = findViewById(R.id.optionDEditText);
        answerEditText = findViewById(R.id.answerEditText);
        uploadMcqButton = findViewById(R.id.uploadMcqButton);
        backButton=findViewById(R.id.backButton1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UploadActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });
        uploadMcqButton.setOnClickListener(v -> uploadMcq());
    }

    private void uploadMcq() {
        String className = classEditText.getText().toString().trim();
        String subject = subjectEditText.getText().toString().trim();
        String question = questionEditText.getText().toString().trim();
        String optionA = optionAEditText.getText().toString().trim();
        String optionB = optionBEditText.getText().toString().trim();
        String optionC = optionCEditText.getText().toString().trim();
        String optionD = optionDEditText.getText().toString().trim();
        String answer = answerEditText.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(subject) ||
                TextUtils.isEmpty(question) || TextUtils.isEmpty(optionA) ||
                TextUtils.isEmpty(optionB) || TextUtils.isEmpty(optionC) ||
                TextUtils.isEmpty(optionD) || TextUtils.isEmpty(answer)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!answer.matches("[ABCD]")) {
            Toast.makeText(this, "Answer must be A, B, C or D", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("MCQs")
                .child("Class"+className)
                .child(subject)
                .push(); // generates a unique key

        TestActivity.Question mcq = new TestActivity.Question();
        mcq.setQuestion(question);
        mcq.setOptionA(optionA);
        mcq.setOptionB(optionB);
        mcq.setOptionC(optionC);
        mcq.setOptionD(optionD);
        mcq.setAnswer(answer);

        ref.setValue(mcq).addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "MCQ uploaded successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to upload MCQ", Toast.LENGTH_SHORT).show());
    }

    public static class Question {
        private String question, optionA, optionB, optionC, optionD, answer;
        public Question() {}
        public String getQuestion() { return question; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public String getAnswer() { return answer; }
        public void setQuestion(String question) { this.question = question; }
        public void setOptionA(String optionA) { this.optionA = optionA; }
        public void setOptionB(String optionB) { this.optionB = optionB; }
        public void setOptionC(String optionC) { this.optionC = optionC; }
        public void setOptionD(String optionD) { this.optionD = optionD; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}