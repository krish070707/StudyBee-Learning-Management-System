package com.example.tutorial;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    private TextView questionText;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button prevButton, nextButton, submitButton,backButton;
    private List<Question> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int[] selectedAnswers;
    private int score = 0;
    private boolean isSubmitted = false;
    private String selectedClass, selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        selectedClass = getIntent().getStringExtra("Class");
        selectedSubject = getIntent().getStringExtra("Subject");

        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);
        backButton=findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Go back to SubjectActivity
        });

        loadQuestions();

        nextButton.setOnClickListener(v -> {
            saveSelectedAnswer();
            if (currentIndex < questionList.size() - 1) {
                currentIndex++;
                showQuestion(currentIndex);
            }
        });

        prevButton.setOnClickListener(v -> {
            saveSelectedAnswer();
            if (currentIndex > 0) {
                currentIndex--;
                showQuestion(currentIndex);
            }
        });

        submitButton.setOnClickListener(v -> {
            saveSelectedAnswer();
            isSubmitted = true;
            calculateScore();
            showScoreSummaryDialog();
            submitButton.setVisibility(View.INVISIBLE);
            backButton.setVisibility(View.VISIBLE);
            showQuestion(currentIndex); // refresh UI
        });
    }
    private void showScoreSummaryDialog() {
        int totalQuestions = questionList.size();
        int correctAnswers = score;
        int wrongAnswers = totalQuestions - score;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Result Summary");
        builder.setMessage("Total Questions: " + totalQuestions +
                "\nCorrect Answers: " + correctAnswers +
                "\nWrong Answers: " + wrongAnswers +
                "\n\nWould you like to retake the test?");

        builder.setPositiveButton("Retake", (dialog, which) -> {
            // Reset test state
            isSubmitted = false;
            score = 0;
            currentIndex = 0;
            for (int i = 0; i < selectedAnswers.length; i++) {
                selectedAnswers[i] = -1;
            }
            showQuestion(currentIndex);
        });

        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
            // Optional: Finish activity or go back
            // finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadQuestions() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("MCQs").child("Class"+selectedClass).child(selectedSubject);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();
                for (DataSnapshot questionSnap : snapshot.getChildren()) {
                    Question q = questionSnap.getValue(Question.class);
                    if (q != null) {
                        questionList.add(q);
                    }
                }
                if (!questionList.isEmpty()) {
                    selectedAnswers = new int[questionList.size()];
                    for (int i = 0; i < selectedAnswers.length; i++) {
                        selectedAnswers[i] = -1;
                    }
                    showQuestion(currentIndex);
                } else {
                    Toast.makeText(TestActivity.this, "No questions found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TestActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuestion(int index) {
        Question q = questionList.get(index);
        questionText.setText("Q" + (index + 1) + ". " + q.getQuestion());
        optionA.setText(q.getOptionA());
        optionB.setText(q.getOptionB());
        optionC.setText(q.getOptionC());
        optionD.setText(q.getOptionD());
        optionsGroup.clearCheck();
        resetOptionColors();

        if (selectedAnswers[index] != -1) {
            switch (selectedAnswers[index]) {
                case 0: optionA.setChecked(true); break;
                case 1: optionB.setChecked(true); break;
                case 2: optionC.setChecked(true); break;
                case 3: optionD.setChecked(true); break;
            }
        }

        if (isSubmitted) {
            disableOptions();

            int correctIndex = getCorrectOptionIndex(q.getAnswer());
            int green = ContextCompat.getColor(this, R.color.green);
            int red = ContextCompat.getColor(this, R.color.red);

            // Highlight correct answer
            switch (correctIndex) {
                case 0: optionA.setBackgroundColor(green); break;
                case 1: optionB.setBackgroundColor(green); break;
                case 2: optionC.setBackgroundColor(green); break;
                case 3: optionD.setBackgroundColor(green); break;
            }

            // Highlight wrong selection
            if (selectedAnswers[index] != correctIndex && selectedAnswers[index] != -1) {
                switch (selectedAnswers[index]) {
                    case 0: optionA.setBackgroundColor(red); break;
                    case 1: optionB.setBackgroundColor(red); break;
                    case 2: optionC.setBackgroundColor(red); break;
                    case 3: optionD.setBackgroundColor(red); break;
                }
            }
        } else {
            enableOptions();
        }

        prevButton.setEnabled(index != 0);
        nextButton.setVisibility(index == questionList.size() - 1 ? View.GONE : View.VISIBLE);
        submitButton.setVisibility(index == questionList.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void resetOptionColors() {
        int defaultColor = ContextCompat.getColor(this, android.R.color.transparent);
        optionA.setBackgroundColor(defaultColor);
        optionB.setBackgroundColor(defaultColor);
        optionC.setBackgroundColor(defaultColor);
        optionD.setBackgroundColor(defaultColor);
    }

    private void disableOptions() {
        optionA.setEnabled(false);
        optionB.setEnabled(false);
        optionC.setEnabled(false);
        optionD.setEnabled(false);
    }

    private void enableOptions() {
        optionA.setEnabled(true);
        optionB.setEnabled(true);
        optionC.setEnabled(true);
        optionD.setEnabled(true);
    }

    private void saveSelectedAnswer() {
        if (isSubmitted) return;  // Prevent saving after submission

        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == optionA.getId()) selectedAnswers[currentIndex] = 0;
        else if (selectedId == optionB.getId()) selectedAnswers[currentIndex] = 1;
        else if (selectedId == optionC.getId()) selectedAnswers[currentIndex] = 2;
        else if (selectedId == optionD.getId()) selectedAnswers[currentIndex] = 3;
        else selectedAnswers[currentIndex] = -1;
    }

    private void calculateScore() {
        score = 0;
        for (int i = 0; i < questionList.size(); i++) {
            int correctIndex = getCorrectOptionIndex(questionList.get(i).getAnswer());
            if (selectedAnswers[i] == correctIndex) {
                score++;
            }
        }
    }

    private int getCorrectOptionIndex(String answer) {
        if (answer == null) return -1;
        switch (answer.trim().toLowerCase()) {
            case "a": return 0;
            case "b": return 1;
            case "c": return 2;
            case "d": return 3;
            default: return -1;
        }
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