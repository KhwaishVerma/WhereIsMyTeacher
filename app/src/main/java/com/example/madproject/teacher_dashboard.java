package com.example.madproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class teacher_dashboard extends AppCompatActivity {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String teacherUid;


    // =========================================================
    // HEADER
    // =========================================================

    private TextView teacherNameText;
    private TextView departmentText;
    private TextView designationText;


    // =========================================================
    // CURRENT CLASS
    // =========================================================

    private TextView currentSubjectText;
    private TextView currentTimeText;
    private TextView currentBatchText;
    private TextView currentRoomText;


    // =========================================================
    // NEXT CLASS
    // =========================================================

    private TextView nextSubjectText;
    private TextView nextTimeText;
    private TextView nextBatchText;
    private TextView nextRoomText;


    // =========================================================
    // REQUESTS
    // =========================================================

    private LinearLayout requestContainer;
    private TextView noRequestText;


    // =========================================================
    // BUTTONS
    // =========================================================

    private Button myTimetableButton;
    private Button editTimetableButton;
    private Button uploadButton;


    // =========================================================
    // ON CREATE
    // =========================================================

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_teacher_dashboard
        );


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


        // -----------------------------------------------------
        // Firebase
        // -----------------------------------------------------

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        FirebaseUser currentUser =
                auth.getCurrentUser();


        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        teacherUid =
                currentUser.getUid();


        // -----------------------------------------------------
        // Find Views
        // -----------------------------------------------------

        teacherNameText =
                findViewById(
                        R.id.textView7
                );

        departmentText =
                findViewById(
                        R.id.textView25
                );

        designationText =
                findViewById(
                        R.id.designationText
                );


        currentSubjectText =
                findViewById(
                        R.id.currentSubjectText
                );

        currentTimeText =
                findViewById(
                        R.id.currentTimeText
                );

        currentBatchText =
                findViewById(
                        R.id.currentBatchText
                );

        currentRoomText =
                findViewById(
                        R.id.currentRoomText
                );


        nextSubjectText =
                findViewById(
                        R.id.nextSubjectText
                );

        nextTimeText =
                findViewById(
                        R.id.nextTimeText
                );

        nextBatchText =
                findViewById(
                        R.id.nextBatchText
                );

        nextRoomText =
                findViewById(
                        R.id.nextRoomText
                );


        requestContainer =
                findViewById(
                        R.id.requestContainer
                );

        noRequestText =
                findViewById(
                        R.id.noRequestText
                );


        myTimetableButton =
                findViewById(
                        R.id.button8
                );

        editTimetableButton =
                findViewById(
                        R.id.button10
                );

        uploadButton =
                findViewById(
                        R.id.upbutton
                );


        // -----------------------------------------------------
        // Load everything
        // -----------------------------------------------------

        loadTeacherProfile();

        loadTodaySchedule();

        loadStudentRequests();


        // -----------------------------------------------------
        // Upload timetable
        // -----------------------------------------------------

        uploadButton.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    teacher_dashboard.this,
                                    upload_tt.class
                            );

                    startActivity(intent);
                }
        );


        // -----------------------------------------------------
        // My timetable
        // -----------------------------------------------------

        myTimetableButton.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    teacher_dashboard.this,
                                    upload_tt.class
                            );

                    startActivity(intent);
                }
        );


        // -----------------------------------------------------
        // Edit timetable
        // -----------------------------------------------------

        editTimetableButton.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    teacher_dashboard.this,
                                    upload_tt.class
                            );

                    intent.putExtra(
                            "editMode",
                            true
                    );

                    startActivity(intent);
                }
        );
    }


    // =========================================================
    // LOAD TEACHER PROFILE
    // =========================================================

    private void loadTeacherProfile() {

        db.collection("users")
                .document(teacherUid)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                teacherNameText.setText(
                                        getString(R.string.teacher_profile_not_found)
                                );

                                departmentText.setText(
                                        getString(R.string.profile_not_found)
                                );

                                designationText.setText("");

                                return;
                            }

                            String name =
                                    document.getString("name");

                            String department =
                                    document.getString("department");

                            String designation =
                                    document.getString("designation");


                            // -------------------------
                            // TEACHER NAME
                            // -------------------------

                            if (name != null
                                    && !name.trim().isEmpty()) {

                                teacherNameText.setText(
                                        getString(
                                                R.string.teacher_greeting,
                                                name.trim()
                                        )
                                );

                            } else {

                                teacherNameText.setText(
                                        getString(
                                                R.string.teacher_name_not_available
                                        )
                                );
                            }


                            // -------------------------
                            // DEPARTMENT
                            // -------------------------

                            if (department != null
                                    && !department.trim().isEmpty()) {

                                departmentText.setText(
                                        department.trim()
                                );

                            } else {

                                departmentText.setText(
                                        getString(
                                                R.string.department_not_available
                                        )
                                );
                            }


                            // -------------------------
                            // DESIGNATION
                            // -------------------------

                            if (designation != null
                                    && !designation.trim().isEmpty()) {

                                designationText.setText(
                                        designation.trim()
                                );

                            } else {

                                designationText.setText("");
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    getString(
                                            R.string.profile_load_failed
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // =========================================================
    // LOAD TODAY'S SCHEDULE
    // =========================================================

    private void loadTodaySchedule() {

        String today =
                new SimpleDateFormat(
                        "EEEE",
                        Locale.US
                ).format(
                        new Date()
                );


        db.collection("teachers")
                .document(teacherUid)
                .collection("schedule")
                .whereEqualTo(
                        "day",
                        today
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            if (
                                    querySnapshot.isEmpty()
                            ) {

                                showNoCurrentClass();

                                showNoNextClass();

                                return;
                            }


                            Calendar now =
                                    Calendar.getInstance();


                            int currentMinutes =
                                    now.get(
                                            Calendar.HOUR_OF_DAY
                                    ) * 60
                                            +
                                            now.get(
                                                    Calendar.MINUTE
                                            );


                            DocumentSnapshot currentClass =
                                    null;

                            DocumentSnapshot nextClass =
                                    null;


                            int nextStartMinutes =
                                    Integer.MAX_VALUE;


                            for (
                                    DocumentSnapshot document :
                                    querySnapshot
                            ) {

                                String startTime =
                                        document.getString(
                                                "startTime"
                                        );

                                String endTime =
                                        document.getString(
                                                "endTime"
                                        );


                                if (
                                        startTime == null
                                                ||
                                                endTime == null
                                ) {

                                    continue;
                                }


                                int start =
                                        convertTimeToMinutes(
                                                startTime
                                        );

                                int end =
                                        convertTimeToMinutes(
                                                endTime
                                        );


                                // ---------------------------------
                                // CURRENT CLASS
                                // ---------------------------------

                                if (
                                        currentMinutes >= start
                                                &&
                                                currentMinutes < end
                                ) {

                                    currentClass =
                                            document;

                                    continue;
                                }


                                // ---------------------------------
                                // NEXT CLASS
                                // ---------------------------------

                                if (
                                        start > currentMinutes
                                                &&
                                                start < nextStartMinutes
                                ) {

                                    nextStartMinutes =
                                            start;

                                    nextClass =
                                            document;
                                }
                            }


                            if (
                                    currentClass != null
                            ) {

                                displayCurrentClass(
                                        currentClass
                                );

                            } else {

                                showNoCurrentClass();
                            }


                            if (
                                    nextClass != null
                            ) {

                                displayNextClass(
                                        nextClass
                                );

                            } else {

                                showNoNextClass();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            currentSubjectText.setText(
                                    "Unable to load class"
                            );

                            nextSubjectText.setText(
                                    "Unable to load class"
                            );
                        }
                );
    }


    // =========================================================
    // DISPLAY CURRENT CLASS
    // =========================================================

    private void displayCurrentClass(
            DocumentSnapshot document
    ) {

        String subject =
                getValue(
                        document,
                        "subject",
                        "Class"
                );

        String start =
                getValue(
                        document,
                        "startTime",
                        ""
                );

        String end =
                getValue(
                        document,
                        "endTime",
                        ""
                );

        String batch =
                getValue(
                        document,
                        "batch",
                        ""
                );

        String room =
                getValue(
                        document,
                        "room",
                        ""
                );


        currentSubjectText.setText(
                subject
        );


        currentTimeText.setText(
                start +
                        " - " +
                        end
        );


        currentBatchText.setText(
                batch.isEmpty()
                        ? ""
                        : batch
        );


        currentRoomText.setText(
                room.isEmpty()
                        ? ""
                        : room
        );
    }


    // =========================================================
    // DISPLAY NEXT CLASS
    // =========================================================

    private void displayNextClass(
            DocumentSnapshot document
    ) {

        String subject =
                getValue(
                        document,
                        "subject",
                        "Class"
                );

        String start =
                getValue(
                        document,
                        "startTime",
                        ""
                );

        String end =
                getValue(
                        document,
                        "endTime",
                        ""
                );

        String batch =
                getValue(
                        document,
                        "batch",
                        ""
                );

        String room =
                getValue(
                        document,
                        "room",
                        ""
                );


        nextSubjectText.setText(
                subject
        );


        nextTimeText.setText(
                start +
                        " - " +
                        end
        );


        nextBatchText.setText(
                batch.isEmpty()
                        ? ""
                        : batch
        );


        nextRoomText.setText(
                room.isEmpty()
                        ? ""
                        : room
        );
    }


    // =========================================================
    // NO CURRENT CLASS
    // =========================================================

    private void showNoCurrentClass() {

        currentSubjectText.setText(
                "No Current Class"
        );

        currentTimeText.setText(
                ""
        );

        currentBatchText.setText(
                ""
        );

        currentRoomText.setText(
                ""
        );
    }


    // =========================================================
    // NO NEXT CLASS
    // =========================================================

    private void showNoNextClass() {

        nextSubjectText.setText(
                "No More Classes"
        );

        nextTimeText.setText(
                ""
        );

        nextBatchText.setText(
                ""
        );

        nextRoomText.setText(
                ""
        );
    }


    // =========================================================
    // LOAD STUDENT REQUESTS
    // =========================================================

    private void loadStudentRequests() {

        db.collection("meetingRequests")
                .whereEqualTo(
                        "teacherId",
                        teacherUid
                )
                .whereEqualTo(
                        "status",
                        "PENDING"
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            requestContainer.removeAllViews();


                            if (
                                    querySnapshot.isEmpty()
                            ) {

                                showNoRequests();

                                return;
                            }


                            noRequestText =
                                    new TextView(
                                            this
                                    );


                            for (
                                    DocumentSnapshot request :
                                    querySnapshot
                            ) {

                                loadRequestStudent(
                                        request
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showNoRequests();

                            Toast.makeText(
                                    this,
                                    "Could not load student requests.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // LOAD REQUEST STUDENT
    // =========================================================

    private void loadRequestStudent(
            DocumentSnapshot request
    ) {

        String studentId =
                request.getString(
                        "studentId"
                );


        if (
                studentId == null
                        ||
                        studentId.isEmpty()
        ) {

            addRequestCard(
                    request,
                    "Student"
            );

            return;
        }


        db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        student -> {

                            String studentName =
                                    student.getString(
                                            "name"
                                    );


                            if (
                                    studentName == null
                                            ||
                                            studentName.isEmpty()
                            ) {

                                studentName =
                                        "Student";
                            }


                            addRequestCard(
                                    request,
                                    studentName
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            addRequestCard(
                                    request,
                                    "Student"
                            );
                        }
                );
    }


    // =========================================================
    // REQUEST CARD
    // =========================================================

    private void addRequestCard(
            DocumentSnapshot request,
            String studentName
    ) {

        LinearLayout card =
                new LinearLayout(
                        this
                );


        card.setOrientation(
                LinearLayout.VERTICAL
        );


        card.setPadding(
                20,
                20,
                20,
                20
        );


        card.setBackgroundColor(
                0xFFFFFFFF
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                0,
                0,
                0,
                12
        );


        card.setLayoutParams(
                cardParams
        );


        // -----------------------------------------------------
        // Student name
        // -----------------------------------------------------

        TextView name =
                new TextView(
                        this
                );


        name.setText(
                studentName
        );

        name.setTextSize(
                19
        );

        name.setTextColor(
                0xFF222222
        );

        name.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );


        card.addView(
                name
        );


        // -----------------------------------------------------
        // Time
        // -----------------------------------------------------

        String date =
                getValue(
                        request,
                        "date",
                        ""
                );

        String start =
                getValue(
                        request,
                        "startTime",
                        ""
                );

        String end =
                getValue(
                        request,
                        "endTime",
                        ""
                );


        TextView time =
                new TextView(
                        this
                );


        time.setText(
                date +
                        "  " +
                        start +
                        " - " +
                        end
        );

        time.setTextSize(
                16
        );

        time.setTextColor(
                0xFF444444
        );


        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        timeParams.setMargins(
                0,
                8,
                0,
                0
        );


        time.setLayoutParams(
                timeParams
        );


        card.addView(
                time
        );


        // -----------------------------------------------------
        // Subject
        // -----------------------------------------------------

        String subject =
                getValue(
                        request,
                        "subject",
                        ""
                );


        if (!subject.isEmpty()) {

            TextView subjectText =
                    new TextView(
                            this
                    );


            subjectText.setText(
                    subject
            );

            subjectText.setTextSize(
                    16
            );

            subjectText.setTextColor(
                    0xFF555555
            );


            LinearLayout.LayoutParams subjectParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            subjectParams.setMargins(
                    0,
                    4,
                    0,
                    0
            );


            subjectText.setLayoutParams(
                    subjectParams
            );


            card.addView(
                    subjectText
            );
        }


        // -----------------------------------------------------
        // Buttons
        // -----------------------------------------------------

        LinearLayout buttons =
                new LinearLayout(
                        this
                );


        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );


        LinearLayout.LayoutParams buttonsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        buttonsParams.setMargins(
                0,
                18,
                0,
                0
        );


        buttons.setLayoutParams(
                buttonsParams
        );


        Button accept =
                new Button(
                        this
                );


        accept.setText(
                "Accept"
        );


        Button reject =
                new Button(
                        this
                );


        reject.setText(
                "Reject"
        );


        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        0,
                        52
                );


        buttonParams.weight =
                1;


        accept.setLayoutParams(
                buttonParams
        );


        LinearLayout.LayoutParams rejectParams =
                new LinearLayout.LayoutParams(
                        0,
                        52
                );


        rejectParams.weight =
                1;


        rejectParams.setMargins(
                12,
                0,
                0,
                0
        );


        reject.setLayoutParams(
                rejectParams
        );


        buttons.addView(
                accept
        );

        buttons.addView(
                reject
        );


        card.addView(
                buttons
        );


        // -----------------------------------------------------
        // Accept
        // -----------------------------------------------------

        accept.setOnClickListener(
                v -> {

                    updateRequestStatus(
                            request.getId(),
                            "ACCEPTED"
                    );
                }
        );


        // -----------------------------------------------------
        // Reject
        // -----------------------------------------------------

        reject.setOnClickListener(
                v -> {

                    updateRequestStatus(
                            request.getId(),
                            "REJECTED"
                    );
                }
        );


        requestContainer.addView(
                card
        );
    }


    // =========================================================
    // UPDATE REQUEST STATUS
    // =========================================================

    private void updateRequestStatus(
            String requestId,
            String status
    ) {

        db.collection("meetingRequests")
                .document(requestId)
                .update(
                        "status",
                        status
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Request " +
                                            status.toLowerCase() +
                                            ".",
                                    Toast.LENGTH_SHORT
                            ).show();


                            loadStudentRequests();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not update request.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // NO REQUESTS
    // =========================================================

    private void showNoRequests() {

        requestContainer.removeAllViews();


        TextView text =
                new TextView(
                        this
                );


        text.setText(
                "No pending student requests."
        );


        text.setTextSize(
                15
        );


        text.setTextColor(
                0xFF666666
        );


        text.setGravity(
                android.view.Gravity.CENTER
        );


        text.setPadding(
                20,
                20,
                20,
                20
        );


        text.setBackgroundColor(
                0xFFFFFFFF
        );


        requestContainer.addView(
                text
        );
    }


    // =========================================================
    // CONVERT TIME TO MINUTES
    // =========================================================

    private int convertTimeToMinutes(
            String time
    ) {

        if (
                time == null
                        ||
                        time.trim().isEmpty()
        ) {

            return -1;
        }


        try {

            String normalized =
                    time.trim()
                            .toUpperCase(
                                    Locale.US
                            );


            String[] patterns = {

                    "h:mm a",

                    "hh:mm a",

                    "H:mm",

                    "HH:mm"

            };


            for (
                    String pattern :
                    patterns
            ) {

                try {

                    SimpleDateFormat format =
                            new SimpleDateFormat(
                                    pattern,
                                    Locale.US
                            );


                    Date parsed =
                            format.parse(
                                    normalized
                            );


                    if (
                            parsed != null
                    ) {

                        Calendar calendar =
                                Calendar.getInstance();


                        calendar.setTime(
                                parsed
                        );


                        return calendar.get(
                                Calendar.HOUR_OF_DAY
                        ) * 60
                                +
                                calendar.get(
                                        Calendar.MINUTE
                                );
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }


        return -1;
    }


    // =========================================================
    // GET STRING SAFELY
    // =========================================================

    private String getValue(
            DocumentSnapshot document,
            String field,
            String defaultValue
    ) {

        String value =
                document.getString(
                        field
                );


        if (
                value == null
        ) {

            return defaultValue;
        }


        return value.trim();
    }


    // =========================================================
    // REFRESH WHEN RETURNING TO DASHBOARD
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (
                teacherUid != null
        ) {

            loadTodaySchedule();

            loadStudentRequests();
        }
    }
}