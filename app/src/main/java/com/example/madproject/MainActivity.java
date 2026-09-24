package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;

    private Button login;
    private Button register;

    private TextView forgotPassword;

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);


        // -------------------------------------------------
        // WINDOW INSETS
        // -------------------------------------------------

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


        // -------------------------------------------------
        // UI REFERENCES
        // -------------------------------------------------

        username =
                findViewById(R.id.editTextText);

        password =
                findViewById(R.id.editTextText2);

        login =
                findViewById(R.id.button);

        register =
                findViewById(R.id.button2);

        forgotPassword =
                findViewById(R.id.forgotPassword);


        // -------------------------------------------------
        // FIREBASE
        // -------------------------------------------------

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // -------------------------------------------------
        // LOGIN
        // -------------------------------------------------

        login.setOnClickListener(
                v -> loginUser()
        );


        // -------------------------------------------------
        // REGISTER
        // -------------------------------------------------

        register.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            MainActivity2.class
                    );

            startActivity(intent);
        });


        // -------------------------------------------------
        // FORGOT PASSWORD
        // -------------------------------------------------

        forgotPassword.setOnClickListener(
                v -> showForgotPasswordDialog()
        );
    }


    // =====================================================
    // FORGOT PASSWORD DIALOG
    // =====================================================

    private void showForgotPasswordDialog() {

        EditText emailInput =
                new EditText(this);

        emailInput.setHint(
                "Enter your registered email"
        );

        emailInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        emailInput.setSingleLine(true);

        emailInput.setPadding(
                40,
                20,
                40,
                20
        );


        String existingEmail =
                username.getText()
                        .toString()
                        .trim();

        if (!existingEmail.isEmpty()) {

            emailInput.setText(
                    existingEmail
            );

            emailInput.setSelection(
                    emailInput.length()
            );
        }


        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Reset Password")
                        .setMessage(
                                "Enter your registered email address. " +
                                        "We will send you a password reset link."
                        )
                        .setView(emailInput)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Send Reset Link",
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                d -> {

                    Button sendButton =
                            dialog.getButton(
                                    androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
                            );

                    sendButton.setOnClickListener(
                            v -> {

                                String email =
                                        emailInput.getText()
                                                .toString()
                                                .trim();


                                if (email.isEmpty()) {

                                    emailInput.setError(
                                            "Enter email"
                                    );

                                    emailInput.requestFocus();

                                    return;
                                }


                                if (
                                        !Patterns.EMAIL_ADDRESS
                                                .matcher(email)
                                                .matches()
                                ) {

                                    emailInput.setError(
                                            "Enter a valid email"
                                    );

                                    emailInput.requestFocus();

                                    return;
                                }


                                dialog.dismiss();

                                sendPasswordResetEmail(
                                        email
                                );
                            }
                    );
                }
        );


        dialog.show();
    }


    // =====================================================
    // SEND RESET EMAIL
    // =====================================================

    private void sendPasswordResetEmail(
            String email
    ) {

        if (email.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter your email address.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        if (
                !Patterns.EMAIL_ADDRESS
                        .matcher(email)
                        .matches()
        ) {

            Toast.makeText(
                    this,
                    "Enter a valid email address.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        Toast.makeText(
                this,
                "Sending password reset email...",
                Toast.LENGTH_SHORT
        ).show();


        auth.sendPasswordResetEmail(
                email
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Toast.makeText(
                        MainActivity.this,
                        "Password reset email sent. " +
                                "Check your inbox.",
                        Toast.LENGTH_LONG
                ).show();

            } else {

                String message =
                        task.getException() != null
                                ? task.getException()
                                .getMessage()
                                : "Could not send reset email.";

                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }


    // =====================================================
    // LOGIN
    // =====================================================

    private void loginUser() {

        String email =
                username.getText()
                        .toString()
                        .trim();

        String userPassword =
                password.getText()
                        .toString();


        // -------------------------------------------------
        // EMAIL VALIDATION
        // -------------------------------------------------

        if (email.isEmpty()) {

            username.setError(
                    "Enter email"
            );

            username.requestFocus();

            return;
        }


        if (
                !Patterns.EMAIL_ADDRESS
                        .matcher(email)
                        .matches()
        ) {

            username.setError(
                    "Enter a valid email"
            );

            username.requestFocus();

            return;
        }


        // -------------------------------------------------
        // PASSWORD VALIDATION
        // -------------------------------------------------

        if (userPassword.isEmpty()) {

            password.setError(
                    "Enter password"
            );

            password.requestFocus();

            return;
        }


        // -------------------------------------------------
        // DISABLE LOGIN BUTTON
        // -------------------------------------------------

        login.setEnabled(false);


        // -------------------------------------------------
        // FIREBASE LOGIN
        // -------------------------------------------------

        auth.signInWithEmailAndPassword(
                email,
                userPassword
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                if (auth.getCurrentUser() == null) {

                    login.setEnabled(true);

                    Toast.makeText(
                            MainActivity.this,
                            "Authentication failed.",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }


                String uid =
                        auth.getCurrentUser()
                                .getUid();


                // -----------------------------------------
                // LOAD USER PROFILE
                // -----------------------------------------

                db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(
                                documentSnapshot -> {

                                    login.setEnabled(true);


                                    if (
                                            !documentSnapshot.exists()
                                    ) {

                                        Toast.makeText(
                                                MainActivity.this,
                                                "User profile not found.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        auth.signOut();

                                        return;
                                    }


                                    String role =
                                            documentSnapshot
                                                    .getString(
                                                            "role"
                                                    );


                                    if (
                                            role == null ||
                                                    role.trim()
                                                            .isEmpty()
                                    ) {

                                        Toast.makeText(
                                                MainActivity.this,
                                                "User role not found.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        auth.signOut();

                                        return;
                                    }


                                    // ---------------------------------
                                    // STUDENT
                                    // ---------------------------------

                                    if (
                                            role.equals(
                                                    "STUDENT"
                                            )
                                    ) {

                                        Intent intent =
                                                new Intent(
                                                        MainActivity.this,
                                                        MainActivity5.class
                                                );

                                        intent.setFlags(
                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        );

                                        startActivity(
                                                intent
                                        );

                                        finish();


                                    }

                                    // ---------------------------------
                                    // TEACHER
                                    // ---------------------------------

                                    else if (
                                            role.equals(
                                                    "TEACHER"
                                            )
                                    ) {

                                        Intent intent =
                                                new Intent(
                                                        MainActivity.this,
                                                        teacher_dashboard.class
                                                );

                                        intent.setFlags(
                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        );

                                        startActivity(
                                                intent
                                        );

                                        finish();


                                    }

                                    // ---------------------------------
                                    // INVALID ROLE
                                    // ---------------------------------

                                    else {

                                        Toast.makeText(
                                                MainActivity.this,
                                                "Invalid user role.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        auth.signOut();
                                    }
                                }
                        )
                        .addOnFailureListener(e -> {

                            login.setEnabled(true);

                            Toast.makeText(
                                    MainActivity.this,
                                    "Could not load user profile: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });

            }

            // ---------------------------------------------
            // LOGIN FAILED
            // ---------------------------------------------

            else {

                login.setEnabled(true);

                String message =
                        task.getException() != null
                                ? task.getException()
                                .getMessage()
                                : "Login failed";


                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}