package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity4 extends AppCompatActivity {

    private EditText facultyId;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;

    private Spinner teacherCodeSpinner;
    private Spinner department;
    private Spinner designation;

    private TextView teacherNamePreview;

    private Button registerButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<String> teacherCodes = new ArrayList<>();

    private final Map<String, String> teacherNames =
            new HashMap<>();

    private boolean teacherDirectoryLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main4);


        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(
                root,
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


        // -------------------------------------------------
        // Firebase
        // -------------------------------------------------

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // -------------------------------------------------
        // Fields
        // -------------------------------------------------

        facultyId =
                findViewById(
                        R.id.editTextText11
                );

        email =
                findViewById(
                        R.id.editTextText12
                );

        teacherCodeSpinner =
                findViewById(
                        R.id.editTextText13
                );

        department =
                findViewById(
                        R.id.editTextText14
                );

        designation =
                findViewById(
                        R.id.editTextText15
                );

        password =
                findViewById(
                        R.id.editTextText16
                );

        confirmPassword =
                findViewById(
                        R.id.editTextText17
                );

        teacherNamePreview =
                findViewById(
                        R.id.teacherNamePreview
                );

        registerButton =
                findViewById(
                        R.id.button7
                );


        // -------------------------------------------------
        // Initial state
        // -------------------------------------------------

        registerButton.setEnabled(false);


        setupDepartmentSpinner();

        setupDesignationSpinner();

        loadTeacherDirectory();


        // -------------------------------------------------
        // Teacher code selection
        // -------------------------------------------------

        teacherCodeSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (
                                position == 0 ||
                                        position >= teacherCodes.size()
                        ) {

                            teacherNamePreview.setText(
                                    "Select a teacher code"
                            );

                            return;
                        }


                        String selectedCode =
                                teacherCodes.get(position);


                        String selectedName =
                                teacherNames.get(
                                        selectedCode
                                );


                        if (
                                selectedName != null &&
                                        !selectedName.trim().isEmpty()
                        ) {

                            teacherNamePreview.setText(
                                    "Teacher: " +
                                            selectedName
                            );

                        } else {

                            teacherNamePreview.setText(
                                    "Teacher name unavailable"
                            );
                        }
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {

                        teacherNamePreview.setText(
                                "Select a teacher code"
                        );
                    }
                }
        );


        registerButton.setOnClickListener(
                v -> registerTeacher()
        );
    }


    // =====================================================
    // LOAD TEACHER DIRECTORY
    // =====================================================

    private void loadTeacherDirectory() {

        teacherDirectoryLoaded = false;

        registerButton.setEnabled(false);


        teacherCodes.clear();

        teacherNames.clear();


        teacherCodes.add(
                "Select Teacher Code"
        );


        db.collection("teacherDirectory")
                .whereEqualTo(
                        "active",
                        true
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            for (
                                    DocumentSnapshot document
                                    : querySnapshot
                            ) {

                                String code =
                                        document.getString(
                                                "teacherCode"
                                        );

                                String name =
                                        document.getString(
                                                "name"
                                        );


                                if (
                                        code == null ||
                                                code.trim().isEmpty()
                                ) {

                                    continue;
                                }


                                code =
                                        code.trim()
                                                .toUpperCase();


                                teacherCodes.add(
                                        code
                                );


                                teacherNames.put(
                                        code,
                                        name != null
                                                ? name.trim()
                                                : ""
                                );
                            }


                            // ---------------------------------
                            // Sort codes alphabetically
                            // ---------------------------------

                            if (
                                    teacherCodes.size() > 1
                            ) {

                                List<String> actualCodes =
                                        new ArrayList<>(
                                                teacherCodes.subList(
                                                        1,
                                                        teacherCodes.size()
                                                )
                                        );

                                java.util.Collections.sort(
                                        actualCodes
                                );


                                teacherCodes.clear();

                                teacherCodes.add(
                                        "Select Teacher Code"
                                );

                                teacherCodes.addAll(
                                        actualCodes
                                );
                            }


                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<>(
                                            MainActivity4.this,
                                            android.R.layout.simple_spinner_item,
                                            teacherCodes
                                    );


                            adapter.setDropDownViewResource(
                                    android.R.layout.simple_spinner_dropdown_item
                            );


                            teacherCodeSpinner.setAdapter(
                                    adapter
                            );


                            teacherDirectoryLoaded = true;


                            if (
                                    teacherCodes.size() > 1
                            ) {

                                registerButton.setEnabled(
                                        true
                                );

                                teacherNamePreview.setText(
                                        "Select your assigned teacher code"
                                );

                            } else {

                                teacherNamePreview.setText(
                                        "No active teacher codes found"
                                );

                                Toast.makeText(
                                        MainActivity4.this,
                                        "No teacher codes are available.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            teacherDirectoryLoaded =
                                    false;

                            registerButton.setEnabled(
                                    false
                            );


                            teacherNamePreview.setText(
                                    "Could not load teacher codes"
                            );


                            Toast.makeText(
                                    MainActivity4.this,
                                    "Could not load teacher directory: " +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =====================================================
    // DEPARTMENT SPINNER
    // =====================================================

    private void setupDepartmentSpinner() {

        String[] departments = {

                "Select Department",

                "STME",

                "SBM",

                "SOC",

                "SPTM",

                "SOL"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        departments
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        department.setAdapter(
                adapter
        );
    }


    // =====================================================
    // DESIGNATION SPINNER
    // =====================================================

    private void setupDesignationSpinner() {

        String[] designations = {

                "Select Designation",

                "Professor",

                "Associate Professor",

                "Assistant Professor",

                "Lecturer",

                "Visiting Faculty"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        designations
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        designation.setAdapter(
                adapter
        );
    }


    // =====================================================
    // REGISTER TEACHER
    // =====================================================

    private void registerTeacher() {

        if (!teacherDirectoryLoaded) {

            Toast.makeText(
                    this,
                    "Teacher directory is still loading.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -------------------------------------------------
        // Selected teacher code
        // -------------------------------------------------

        int teacherCodePosition =
                teacherCodeSpinner.getSelectedItemPosition();


        if (
                teacherCodePosition <= 0 ||
                        teacherCodePosition >= teacherCodes.size()
        ) {

            Toast.makeText(
                    this,
                    "Select your teacher code.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String teacherCode =
                teacherCodes.get(
                        teacherCodePosition
                );


        String teacherName =
                teacherNames.get(
                        teacherCode
                );


        if (
                teacherName == null ||
                        teacherName.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Teacher information could not be found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -------------------------------------------------
        // Other fields
        // -------------------------------------------------

        String teacherFacultyId =
                facultyId
                        .getText()
                        .toString()
                        .trim();


        String teacherEmail =
                email
                        .getText()
                        .toString()
                        .trim();


        String teacherPassword =
                password
                        .getText()
                        .toString();


        String teacherConfirmPassword =
                confirmPassword
                        .getText()
                        .toString();


        String teacherDepartment =
                department
                        .getSelectedItem()
                        .toString();


        String teacherDesignation =
                designation
                        .getSelectedItem()
                        .toString();


        // -------------------------------------------------
        // Validation
        // -------------------------------------------------

        if (
                teacherFacultyId.isEmpty()
        ) {

            facultyId.setError(
                    "Enter your faculty ID"
            );

            facultyId.requestFocus();

            return;
        }


        if (
                teacherEmail.isEmpty() ||
                        !Patterns.EMAIL_ADDRESS
                                .matcher(teacherEmail)
                                .matches()
        ) {

            email.setError(
                    "Enter a valid email"
            );

            email.requestFocus();

            return;
        }


        if (
                department.getSelectedItemPosition()
                        == 0
        ) {

            Toast.makeText(
                    this,
                    "Select your department",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (
                designation.getSelectedItemPosition()
                        == 0
        ) {

            Toast.makeText(
                    this,
                    "Select your designation",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (
                teacherPassword.length() < 6
        ) {

            password.setError(
                    "Password must be at least 6 characters"
            );

            password.requestFocus();

            return;
        }


        if (
                !teacherPassword.equals(
                        teacherConfirmPassword
                )
        ) {

            confirmPassword.setError(
                    "Passwords do not match"
            );

            confirmPassword.requestFocus();

            return;
        }


        // -------------------------------------------------
        // Disable button
        // -------------------------------------------------

        registerButton.setEnabled(false);


        // -------------------------------------------------
        // Firebase Authentication
        // -------------------------------------------------

        auth.createUserWithEmailAndPassword(
                        teacherEmail,
                        teacherPassword
                )
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                registerButton.setEnabled(
                                        true
                                );


                                String message =
                                        task.getException() != null
                                                ? task.getException()
                                                .getMessage()
                                                : "Registration failed";


                                Toast.makeText(
                                        MainActivity4.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            if (
                                    auth.getCurrentUser()
                                            == null
                            ) {

                                registerButton.setEnabled(
                                        true
                                );

                                Toast.makeText(
                                        MainActivity4.this,
                                        "Authentication user was not created.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            String uid =
                                    auth.getCurrentUser()
                                            .getUid();


                            // ---------------------------------
                            // User document
                            // ---------------------------------

                            Map<String, Object> teacherData =
                                    new HashMap<>();


                            teacherData.put(
                                    "name",
                                    teacherName
                            );


                            teacherData.put(
                                    "email",
                                    teacherEmail
                            );


                            teacherData.put(
                                    "role",
                                    "TEACHER"
                            );


                            teacherData.put(
                                    "teacherCode",
                                    teacherCode
                            );


                            teacherData.put(
                                    "facultyId",
                                    teacherFacultyId
                            );


                            teacherData.put(
                                    "department",
                                    teacherDepartment
                            );


                            teacherData.put(
                                    "designation",
                                    teacherDesignation
                            );


                            teacherData.put(
                                    "createdAt",
                                    com.google.firebase.firestore.FieldValue
                                            .serverTimestamp()
                            );


                            // ---------------------------------
                            // Save user
                            // ---------------------------------

                            db.collection("users")
                                    .document(uid)
                                    .set(teacherData)
                                    .addOnSuccessListener(
                                            unused -> {

                                                Toast.makeText(
                                                        MainActivity4.this,
                                                        "Registration successful!",
                                                        Toast.LENGTH_SHORT
                                                ).show();


                                                Intent intent =
                                                        new Intent(
                                                                MainActivity4.this,
                                                                MainActivity.class
                                                        );


                                                intent.setFlags(
                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                );


                                                startActivity(
                                                        intent
                                                );

                                                finish();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                registerButton.setEnabled(
                                                        true
                                                );


                                                Toast.makeText(
                                                        MainActivity4.this,
                                                        "Database error: " +
                                                                e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                );
    }
}