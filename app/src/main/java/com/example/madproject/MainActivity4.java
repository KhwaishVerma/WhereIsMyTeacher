package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity4 extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private EditText teacherFullName;
    private EditText facultyId;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;

    private Spinner teacherCodeSpinner;
    private Spinner department;
    private Spinner designation;

    private TextView teacherNamePreview;

    private Button registerButton;


    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // =====================================================
    // TEACHER DIRECTORY DATA
    // =====================================================

    private final List<String> teacherCodes =
            new ArrayList<>();

    private final Map<String, String> teacherNames =
            new HashMap<>();

    private boolean teacherDirectoryLoaded = false;


    // =====================================================
    // ACTIVITY CREATED
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main4);


        // =================================================
        // EDGE TO EDGE
        // =================================================

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


        // =================================================
        // FIREBASE
        // =================================================

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =================================================
        // FIND VIEWS
        // =================================================

        teacherFullName =
                findViewById(
                        R.id.teacherFullName
                );

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


        // =================================================
        // INITIAL STATE
        // =================================================

        registerButton.setEnabled(false);


        // =================================================
        // SPINNERS
        // =================================================

        setupDepartmentSpinner();

        setupDesignationSpinner();


        // =================================================
        // LOAD TEACHER DIRECTORY
        // =================================================

        loadTeacherDirectory();


        // =================================================
        // TEACHER CODE SELECTION
        // =================================================

        teacherCodeSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        // First item is placeholder
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


                        // -------------------------------------
                        // Show existing directory name
                        // -------------------------------------

                        if (
                                selectedName != null &&
                                        !selectedName.trim().isEmpty()
                        ) {

                            teacherNamePreview.setText(
                                    "Directory name: " +
                                            selectedName
                            );

                        } else {

                            teacherNamePreview.setText(
                                    "Directory name unavailable"
                            );
                        }


                        // -------------------------------------
                        // Clear manual name field
                        // -------------------------------------

                        teacherFullName.setText("");
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {

                        teacherNamePreview.setText(
                                "Select a teacher code"
                        );
                    }
                }
        );


        // =================================================
        // REGISTER BUTTON
        // =================================================

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


        // Placeholder
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

                            // ---------------------------------
                            // Read teacher directory
                            // ---------------------------------

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot
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
                                                .toUpperCase(
                                                        Locale.US
                                                );


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
                            // Sort teacher codes
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


                                Collections.sort(
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


                            // ---------------------------------
                            // Spinner adapter
                            // ---------------------------------

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


                            // ---------------------------------
                            // Enable registration
                            // ---------------------------------

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

        // =================================================
        // CHECK DIRECTORY
        // =================================================

        if (!teacherDirectoryLoaded) {

            Toast.makeText(
                    this,
                    "Teacher directory is still loading.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =================================================
        // GET SELECTED TEACHER CODE
        // =================================================

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


        // =================================================
        // GET FULL NAME
        // =================================================

        String enteredName =
                teacherFullName
                        .getText()
                        .toString()
                        .trim();


        if (enteredName.isEmpty()) {

            teacherFullName.setError(
                    "Enter your full name"
            );

            teacherFullName.requestFocus();

            return;
        }


        // =================================================
        // NORMALIZE NAME
        // =================================================
        //
        // Teacher enters:
        //
        // Pushpanjay Sharma
        //
        // We store:
        //
        // Dr. Pushpanjay Sharma
        //
        // If teacher enters:
        //
        // Dr Pushpanjay Sharma
        //
        // We still store:
        //
        // Dr. Pushpanjay Sharma
        //
        // =================================================

        String cleanName =
                enteredName
                        .trim()
                        .replaceAll(
                                "(?i)^dr\\.?\\s*",
                                ""
                        )
                        .trim();


        if (cleanName.isEmpty()) {

            teacherFullName.setError(
                    "Enter your full name"
            );

            teacherFullName.requestFocus();

            return;
        }


        String teacherName =
                "Dr. " + cleanName;


        // =================================================
        // OTHER FIELDS
        // =================================================

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


        // =================================================
        // VALIDATION
        // =================================================

        if (teacherFacultyId.isEmpty()) {

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


        // =================================================
        // DISABLE REGISTER BUTTON
        // =================================================

        registerButton.setEnabled(false);


        // =================================================
        // CREATE FIREBASE AUTH ACCOUNT
        // =================================================

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


                            // =================================
                            // GET AUTH USER
                            // =================================

                            FirebaseUser currentUser =
                                    auth.getCurrentUser();


                            if (currentUser == null) {

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
                                    currentUser.getUid();


                            // =================================
                            // USER DOCUMENT
                            // =================================

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
                                    FieldValue.serverTimestamp()
                            );


                            // =================================
                            // SAVE USERS/{UID}
                            // =================================

                            db.collection("users")
                                    .document(uid)
                                    .set(teacherData)
                                    .addOnSuccessListener(
                                            unused -> {

                                                // =================================
                                                // UPDATE TEACHER DIRECTORY
                                                // =================================

                                                updateTeacherDirectory(
                                                        teacherCode,
                                                        teacherName
                                                );
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


    // =====================================================
    // UPDATE TEACHER DIRECTORY
    // =====================================================
    //
    // teacherDirectory/{teacherCode}
    //
    // Example:
    //
    // teacherDirectory/PS
    //
    // {
    //     teacherCode: "PS",
    //     name: "Dr. Pushpanjay Sharma",
    //     active: true
    // }
    //
    // =====================================================

    private void updateTeacherDirectory(
            String teacherCode,
            String teacherName
    ) {

        Map<String, Object> directoryUpdate =
                new HashMap<>();


        directoryUpdate.put(
                "teacherCode",
                teacherCode
        );


        directoryUpdate.put(
                "name",
                teacherName
        );


        directoryUpdate.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );


        db.collection("teacherDirectory")
                .document(teacherCode)
                .set(
                        directoryUpdate,
                        SetOptions.merge()
                )
                .addOnSuccessListener(
                        unused -> {

                            // =================================
                            // SUCCESS
                            // =================================

                            Toast.makeText(
                                    MainActivity4.this,
                                    "Registration successful!",
                                    Toast.LENGTH_SHORT
                            ).show();


                            // =================================
                            // GO TO LOGIN
                            // =================================

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


                            startActivity(intent);

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            // =================================
                            // USER ACCOUNT EXISTS BUT DIRECTORY
                            // UPDATE FAILED
                            // =================================

                            registerButton.setEnabled(
                                    true
                            );


                            Toast.makeText(
                                    MainActivity4.this,
                                    "Account created, but teacher directory could not be updated: " +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }
}