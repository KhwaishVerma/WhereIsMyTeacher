package com.example.madproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity5 extends AppCompatActivity {

    private Button findteacher;
    private Button myrequests;

    private TextView textViewWelcome;
    private TextView textViewDepartment;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main5);

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


        // --------------------------------------------------
        // VIEWS
        // --------------------------------------------------

        textViewWelcome =
                findViewById(R.id.textViewWelcome);

        textViewDepartment =
                findViewById(R.id.textViewDepartment);

        findteacher =
                findViewById(R.id.buttonFindTeacher);

        myrequests =
                findViewById(R.id.buttonMyRequests);


        // --------------------------------------------------
        // FIREBASE
        // --------------------------------------------------

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // --------------------------------------------------
        // LOAD STUDENT PROFILE
        // --------------------------------------------------

        loadStudentProfile();


        // --------------------------------------------------
        // FIND TEACHER
        // --------------------------------------------------

        findteacher.setOnClickListener(v -> {

            Intent i =
                    new Intent(
                            MainActivity5.this,
                            MainActivity6.class
                    );

            startActivity(i);
        });


        // --------------------------------------------------
        // MY REQUESTS
        // --------------------------------------------------

        myrequests.setOnClickListener(v -> {

            Intent i =
                    new Intent(
                            MainActivity5.this,
                            student_requests.class
                    );

            startActivity(i);
        });
    }


    // ======================================================
    // LOAD STUDENT PROFILE
    // ======================================================

    @SuppressLint("StringFormatInvalid")
    private void loadStudentProfile() {

        FirebaseUser currentUser =
                auth.getCurrentUser();


        // --------------------------------------------------
        // CHECK LOGIN
        // --------------------------------------------------

        if (currentUser == null) {

            textViewWelcome.setText(
                    getString(
                            R.string.student_name_not_available
                    )
            );

            textViewDepartment.setText(
                    getString(
                            R.string.department_not_available
                    )
            );

            return;
        }


        String studentUid =
                currentUser.getUid();


        // --------------------------------------------------
        // GET STUDENT FROM FIRESTORE
        // --------------------------------------------------

        db.collection("users")
                .document(studentUid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        textViewWelcome.setText(
                                getString(
                                        R.string.student_name_not_available
                                )
                        );

                        textViewDepartment.setText(
                                getString(
                                        R.string.profile_not_found
                                )
                        );

                        return;
                    }


                    // --------------------------------------------------
                    // STUDENT NAME
                    // --------------------------------------------------

                    String studentName =
                            document.getString("name");


                    if (studentName != null
                            && !studentName.trim().isEmpty()) {

                        textViewWelcome.setText(
                                getString(R.string.student_greeting)
                                        + " "
                                        + studentName.trim()
                        );

                    } else {

                        textViewWelcome.setText(
                                getString(
                                        R.string.student_name_not_available
                                )
                        );
                    }


                    // --------------------------------------------------
                    // STUDENT DEPARTMENT
                    // --------------------------------------------------

                    String department =
                            document.getString("department");


                    if (department != null
                            && !department.trim().isEmpty()) {

                        textViewDepartment.setText(
                                department.trim()
                        );

                    } else {

                        textViewDepartment.setText(
                                getString(
                                        R.string.department_not_available
                                )
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    textViewWelcome.setText(
                            getString(
                                    R.string.student_name_not_available
                            )
                    );

                    textViewDepartment.setText(
                            getString(
                                    R.string.profile_load_failed
                            )
                    );

                    Toast.makeText(
                            MainActivity5.this,
                            getString(
                                    R.string.profile_load_failed
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}