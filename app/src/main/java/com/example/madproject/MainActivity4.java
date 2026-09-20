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

public class MainActivity4 extends AppCompatActivity {

    private EditText name, facultyId, email, password, confirmPassword;
    private Spinner department, designation;
    private Button registerButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Fields
        name = findViewById(R.id.editTextText10);
        facultyId = findViewById(R.id.editTextText11);
        email = findViewById(R.id.editTextText12);
        department = findViewById(R.id.editTextText13);
        designation = findViewById(R.id.editTextText14);
        password = findViewById(R.id.editTextText16);
        confirmPassword = findViewById(R.id.editTextText17);
        registerButton = findViewById(R.id.button7);

        setupSpinners();

        registerButton.setOnClickListener(v -> registerTeacher());
    }

    private void setupSpinners() {

        String[] departments = {
                "Select Department",
                "STME",
                "SBM",
                "SOC",
                "SPTM",
                "SOL"
        };

        String[] designations = {
                "Select Designation",
                "Professor",
                "Associate Professor",
                "Assistant Professor",
                "Lecturer",
                "Visiting Faculty"
        };

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                departments
        );

        departmentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        department.setAdapter(departmentAdapter);

        ArrayAdapter<String> designationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                designations
        );

        designationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        designation.setAdapter(designationAdapter);
    }

    private void registerTeacher() {

        String teacherName = name.getText().toString().trim();
        String teacherFacultyId = facultyId.getText().toString().trim();
        String teacherEmail = email.getText().toString().trim();
        String teacherDepartment = department.getSelectedItem().toString();
        String teacherDesignation = designation.getSelectedItem().toString();
        String teacherPassword = password.getText().toString();
        String teacherConfirmPassword = confirmPassword.getText().toString();

        // Validation

        if (teacherName.isEmpty()) {
            name.setError("Enter your name");
            name.requestFocus();
            return;
        }

        if (teacherFacultyId.isEmpty()) {
            facultyId.setError("Enter your ID");
            facultyId.requestFocus();
            return;
        }

        if (teacherEmail.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(teacherEmail).matches()) {

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

        if (designation.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    this,
                    "Select your designation",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (teacherPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return;
        }

        if (!teacherPassword.equals(teacherConfirmPassword)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        registerButton.setEnabled(false);

        auth.createUserWithEmailAndPassword(
                teacherEmail,
                teacherPassword
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                String uid = auth.getCurrentUser().getUid();

                Map<String, Object> teacherData = new HashMap<>();

                teacherData.put("name", teacherName);
                teacherData.put("email", teacherEmail);
                teacherData.put("role", "TEACHER");
                teacherData.put("facultyId", teacherFacultyId);
                teacherData.put("department", teacherDepartment);
                teacherData.put("designation", teacherDesignation);

                teacherData.put(
                        "createdAt",
                        com.google.firebase.firestore.FieldValue.serverTimestamp()
                );

                db.collection("users")
                        .document(uid)
                        .set(teacherData)
                        .addOnSuccessListener(unused -> {

                            Toast.makeText(
                                    MainActivity4.this,
                                    "Registration successful!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    MainActivity4.this,
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
                                    MainActivity4.this,
                                    "Database error: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });

            } else {

                registerButton.setEnabled(true);

                String message = task.getException() != null
                        ? task.getException().getMessage()
                        : "Registration failed";

                Toast.makeText(
                        MainActivity4.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}