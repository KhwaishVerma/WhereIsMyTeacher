package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;

    private Button login;
    private Button register;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        username = findViewById(R.id.editTextText);
        password = findViewById(R.id.editTextText2);

        login = findViewById(R.id.button);
        register = findViewById(R.id.button2);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        login.setOnClickListener(v -> loginUser());

        register.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    MainActivity2.class
            );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email = username.getText().toString().trim();
        String userPassword = password.getText().toString();

        if (email.isEmpty()) {
            username.setError("Enter email");
            username.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            username.setError("Enter a valid email");
            username.requestFocus();
            return;
        }

        if (userPassword.isEmpty()) {
            password.setError("Enter password");
            password.requestFocus();
            return;
        }

        login.setEnabled(false);

        auth.signInWithEmailAndPassword(
                email,
                userPassword
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                String uid = auth.getCurrentUser().getUid();

                db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {

                            login.setEnabled(true);

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "User profile not found.",
                                        Toast.LENGTH_LONG
                                ).show();

                                auth.signOut();
                                return;
                            }

                            String role =
                                    documentSnapshot.getString("role");

                            if (role == null) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "User role not found.",
                                        Toast.LENGTH_LONG
                                ).show();

                                auth.signOut();
                                return;
                            }

                            if (role.equals("STUDENT")) {

                                Intent intent = new Intent(
                                        MainActivity.this,
                                        MainActivity5.class
                                );

                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);
                                finish();

                            } else if (role.equals("TEACHER")) {

                                Intent intent = new Intent(
                                        MainActivity.this,
                                        teacher_dashboard.class
                                );

                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);
                                finish();

                            } else {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Invalid user role.",
                                        Toast.LENGTH_LONG
                                ).show();

                                auth.signOut();
                            }
                        })
                        .addOnFailureListener(e -> {

                            login.setEnabled(true);

                            Toast.makeText(
                                    MainActivity.this,
                                    "Could not load user profile: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });

            } else {

                login.setEnabled(true);

                String message = task.getException() != null
                        ? task.getException().getMessage()
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