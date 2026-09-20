package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity3 extends AppCompatActivity {

    private EditText name, sapId, email, password, confirmPassword;

    private Spinner department;
    private Spinner course;
    private Spinner year;

    private Button registerButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Fields
        name = findViewById(R.id.editTextText3);
        sapId = findViewById(R.id.editTextText4);
        email = findViewById(R.id.editTextText5);

        department = findViewById(R.id.editTextText6);
        course = findViewById(R.id.courseSpinner);
        year = findViewById(R.id.editTextText7);

        password = findViewById(R.id.editTextText8);
        confirmPassword = findViewById(R.id.editTextText9);

        registerButton = findViewById(R.id.button6);

        setupDepartmentSpinner();

        // Initially disable course and year
        course.setEnabled(false);
        year.setEnabled(false);

        // Department changes → update courses
        department.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        if (position == 0) {

                            course.setEnabled(false);
                            year.setEnabled(false);

                            setSpinner(
                                    course,
                                    new String[]{"Select Course"}
                            );

                            setSpinner(
                                    year,
                                    new String[]{"Select Year"}
                            );

                            return;
                        }

                        String selectedDepartment =
                                department.getSelectedItem().toString();

                        updateCourses(selectedDepartment);
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );

        // Course changes → update years
        course.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        if (position == 0) {

                            year.setEnabled(false);

                            setSpinner(
                                    year,
                                    new String[]{"Select Year"}
                            );

                            return;
                        }

                        String selectedCourse =
                                course.getSelectedItem().toString();

                        updateYears(selectedCourse);
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );

        registerButton.setOnClickListener(v -> registerStudent());
    }

    // ---------------------------------------------------------
    // DEPARTMENT
    // ---------------------------------------------------------

    private void setupDepartmentSpinner() {

        String[] departments = {
                "Select Department",
                "STME",
                "SBM",
                "SOC",
                "SPTM",
                "SOL"
        };

        setSpinner(department, departments);
    }

    // ---------------------------------------------------------
    // COURSE
    // ---------------------------------------------------------

    private void updateCourses(String selectedDepartment) {

        String[] courses;

        switch (selectedDepartment) {

            case "STME":

                courses = new String[]{
                        "Select Course",
                        "B. Tech CE",
                        "B. Tech AIDS",
                        "MBA (Tech)"
                };

                break;

            case "SOC":

                courses = new String[]{
                        "Select Course",
                        "BBA",
                        "B. Comm"
                };

                break;

            case "SPTM":

                courses = new String[]{
                        "Select Course",
                        "B. Pharma",
                        "MBA (Pharma Tech)"
                };

                break;

            case "SOL":

                courses = new String[]{
                        "Select Course",
                        "BBA LLB",
                        "BA LLB"
                };

                break;

            case "SBM":

                courses = new String[]{
                        "Select Course",
                        "MBA"
                };

                break;

            default:

                courses = new String[]{
                        "Select Course"
                };

                break;
        }

        setSpinner(course, courses);

        course.setEnabled(true);

        year.setEnabled(false);

        setSpinner(
                year,
                new String[]{"Select Year"}
        );
    }

    // ---------------------------------------------------------
    // YEAR
    // ---------------------------------------------------------

    private void updateYears(String selectedCourse) {

        int duration;

        switch (selectedCourse) {

            case "B. Tech CE":
            case "B. Tech AIDS":
                duration = 4;
                break;

            case "MBA (Tech)":
            case "MBA (Pharma Tech)":
            case "BBA LLB":
            case "BA LLB":
                duration = 5;
                break;

            case "BBA":
            case "B. Comm":
            case "B. Pharma":
                duration = 3;
                break;

            case "MBA":
                duration = 2;
                break;

            default:
                duration = 0;
                break;
        }

        if (duration == 0) {

            year.setEnabled(false);

            setSpinner(
                    year,
                    new String[]{"Select Year"}
            );

            return;
        }

        String[] years =
                new String[duration + 1];

        years[0] = "Select Year";

        for (int i = 1; i <= duration; i++) {
            years[i] = getOrdinal(i);
        }

        setSpinner(year, years);

        year.setEnabled(true);
    }

    private String getOrdinal(int number) {

        switch (number) {

            case 1:
                return "1st";

            case 2:
                return "2nd";

            case 3:
                return "3rd";

            default:
                return number + "th";
        }
    }

    // ---------------------------------------------------------
    // SPINNER HELPER
    // ---------------------------------------------------------

    private void setSpinner(
            Spinner spinner,
            String[] values) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        values
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    // ---------------------------------------------------------
    // REGISTRATION
    // ---------------------------------------------------------

    private void registerStudent() {

        String studentName =
                name.getText().toString().trim();

        String studentSapId =
                sapId.getText().toString().trim();

        String studentEmail =
                email.getText().toString().trim();

        String studentPassword =
                password.getText().toString();

        String studentConfirmPassword =
                confirmPassword.getText().toString();

        // Validation
        if (studentName.isEmpty()) {

            name.setError("Enter your name");
            name.requestFocus();
            return;
        }

        if (studentSapId.isEmpty()) {

            sapId.setError("Enter SAP ID");
            sapId.requestFocus();
            return;
        }

        if (studentEmail.isEmpty()
                || !Patterns.EMAIL_ADDRESS
                .matcher(studentEmail)
                .matches()) {

            email.setError("Enter a valid email");
            email.requestFocus();
            return;
        }

        if (department.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Select your department",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (course.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Select your course",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (year.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Select your year",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String studentDepartment =
                department.getSelectedItem().toString();

        String studentCourse =
                course.getSelectedItem().toString();

        String studentYear =
                year.getSelectedItem().toString();

        if (studentPassword.length() < 6) {

            password.setError(
                    "Password must be at least 6 characters"
            );

            password.requestFocus();
            return;
        }

        if (!studentPassword.equals(
                studentConfirmPassword)) {

            confirmPassword.setError(
                    "Passwords do not match"
            );

            confirmPassword.requestFocus();
            return;
        }

        registerButton.setEnabled(false);

        // -----------------------------------------------------
        // Firebase Authentication
        // -----------------------------------------------------

        auth.createUserWithEmailAndPassword(
                studentEmail,
                studentPassword
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                String uid =
                        auth.getCurrentUser().getUid();

                // -------------------------------------------------
                // Firestore Student Profile
                // -------------------------------------------------

                Map<String, Object> studentData =
                        new HashMap<>();

                studentData.put(
                        "name",
                        studentName
                );

                studentData.put(
                        "email",
                        studentEmail
                );

                studentData.put(
                        "role",
                        "STUDENT"
                );

                studentData.put(
                        "sapId",
                        studentSapId
                );

                studentData.put(
                        "department",
                        studentDepartment
                );

                studentData.put(
                        "course",
                        studentCourse
                );

                studentData.put(
                        "year",
                        studentYear
                );

                studentData.put(
                        "createdAt",
                        com.google.firebase.firestore
                                .FieldValue
                                .serverTimestamp()
                );

                db.collection("users")
                        .document(uid)
                        .set(studentData)
                        .addOnSuccessListener(unused -> {

                            Toast.makeText(
                                    MainActivity3.this,
                                    "Registration successful!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent =
                                    new Intent(
                                            MainActivity3.this,
                                            MainActivity.class
                                    );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {

                            registerButton.setEnabled(true);

                            Toast.makeText(
                                    MainActivity3.this,
                                    "Database error: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });

            } else {

                registerButton.setEnabled(true);

                String message =
                        task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";

                Toast.makeText(
                        MainActivity3.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}