package com.example.madproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class teacher_dashboard extends AppCompatActivity {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String teacherUid;
    private String teacherCode;


    // =========================================================
    // HEADER
    // =========================================================

    private TextView teacherNameText;
    private TextView departmentText;
    private TextView designationText;
    private TextView teacherCodeText;


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
    // TODAY'S SCHEDULE
    // =========================================================

    private LinearLayout todayScheduleContainer;
    private TextView todayScheduleLoading;


    // =========================================================
    // REQUESTS
    // =========================================================

    private LinearLayout requestContainer;
    private TextView noRequestText;


    // =========================================================
    // BUTTONS
    // =========================================================

    private Button editTimetableButton;


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


        View root =
                findViewById(R.id.main);


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


        // =====================================================
        // FIREBASE
        // =====================================================

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


        // =====================================================
        // FIND VIEWS
        // =====================================================

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

        teacherCodeText =
                findViewById(
                        R.id.teacherCodeText
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


        todayScheduleContainer =
                findViewById(
                        R.id.todayScheduleContainer
                );

        todayScheduleLoading =
                findViewById(
                        R.id.todayScheduleLoading
                );


        requestContainer =
                findViewById(
                        R.id.requestContainer
                );

        noRequestText =
                findViewById(
                        R.id.noRequestText
                );



        editTimetableButton =
                findViewById(
                        R.id.button10
                );




        // =====================================================
        // LOAD PROFILE
        // =====================================================

        loadTeacherProfile();


        // =====================================================
        // BUTTONS
        // =====================================================



        /*
         * For now these still open the timetable/import screen.
         * Later we can create a dedicated read-only timetable
         * screen for teachers.
         */



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
                                        getString(
                                                R.string.teacher_profile_not_found
                                        )
                                );

                                departmentText.setText(
                                        getString(
                                                R.string.profile_not_found
                                        )
                                );

                                designationText.setText("");

                                teacherCodeText.setText("");

                                return;
                            }


                            String name =
                                    document.getString(
                                            "name"
                                    );

                            String department =
                                    document.getString(
                                            "department"
                                    );

                            String designation =
                                    document.getString(
                                            "designation"
                                    );

                            teacherCode =
                                    document.getString(
                                            "teacherCode"
                                    );


                            // =================================================
                            // NAME
                            // =================================================

                            if (
                                    name != null &&
                                            !name.trim().isEmpty()
                            ) {

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


                            // =================================================
                            // DEPARTMENT
                            // =================================================

                            if (
                                    department != null &&
                                            !department.trim().isEmpty()
                            ) {

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


                            // =================================================
                            // DESIGNATION
                            // =================================================

                            if (
                                    designation != null &&
                                            !designation.trim().isEmpty()
                            ) {

                                designationText.setText(
                                        designation.trim()
                                );

                            } else {

                                designationText.setText("");
                            }


                            // =================================================
                            // TEACHER CODE
                            // =================================================

                            if (
                                    teacherCode != null &&
                                            !teacherCode.trim().isEmpty()
                            ) {

                                teacherCode =
                                        teacherCode
                                                .trim()
                                                .toUpperCase();

                                teacherCodeText.setText(
                                        "Teacher Code: " +
                                                teacherCode
                                );


                                /*
                                 * IMPORTANT:
                                 *
                                 * The teacher code is the bridge between
                                 * users/{uid} and masterSchedules.
                                 */

                                loadTodaySchedule();
                                loadStudentRequests();

                            } else {

                                teacherCodeText.setText(
                                        "Teacher Code unavailable"
                                );

                                showNoCurrentClass();

                                showNoNextClass();

                                showNoTodaySchedule();
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

    private String normalizeDay(String day) {

        if (day == null) {
            return "";
        }

        String normalized =
                day.trim()
                        .toLowerCase(Locale.US);

        switch (normalized) {

            case "mon":
            case "monday":
                return "mon";

            case "tue":
            case "tues":
            case "tuesday":
                return "tue";

            case "wed":
            case "wednesday":
                return "wed";

            case "thu":
            case "thur":
            case "thurs":
            case "thursday":
                return "thu";

            case "fri":
            case "friday":
                return "fri";

            case "sat":
            case "saturday":
                return "sat";

            case "sun":
            case "sunday":
                return "sun";

            default:
                return normalized;
        }
    }
    // =========================================================
    // LOAD TODAY'S SCHEDULE
    // =========================================================
    private void loadTodaySchedule() {

        if (teacherCode == null
                || teacherCode.trim().isEmpty()) {

            showNoCurrentClass();
            showNoNextClass();
            showNoTodaySchedule();

            return;
        }

        teacherCode =
                teacherCode.trim().toUpperCase();

        String today =
                new SimpleDateFormat(
                        "EEE",
                        Locale.US
                ).format(new Date());

        android.util.Log.d(
                "WIMT_DEBUG",
                "Teacher Code = " + teacherCode
        );

        android.util.Log.d(
                "WIMT_DEBUG",
                "Android Today = " + today
        );

        /*
         * First get all schedules belonging to this teacher.
         *
         * We do NOT filter day in Firestore because the database
         * currently contains both:
         *
         * Tue / Tues
         * Thu / Thurs
         *
         * We normalize the day locally.
         */
        db.collection("masterSchedules")
                .whereArrayContains(
                        "teacherCodes",
                        teacherCode
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    android.util.Log.d(
                            "WIMT_DEBUG",
                            "Schedules for teacher = "
                                    + querySnapshot.size()
                    );

                    List<DocumentSnapshot> todaySchedules =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String scheduleDay =
                                document.getString("day");

                        if (scheduleDay == null) {
                            continue;
                        }

                        String normalizedScheduleDay =
                                normalizeDay(scheduleDay);

                        String normalizedToday =
                                normalizeDay(today);

                        android.util.Log.d(
                                "WIMT_DEBUG",
                                "Schedule "
                                        + document.getId()
                                        + " | raw day="
                                        + scheduleDay
                                        + " | normalized="
                                        + normalizedScheduleDay
                        );

                        if (normalizedScheduleDay.equals(
                                normalizedToday
                        )) {

                            todaySchedules.add(
                                    document
                            );
                        }
                    }

                    android.util.Log.d(
                            "WIMT_DEBUG",
                            "Today's matching schedules = "
                                    + todaySchedules.size()
                    );

                    Collections.sort(
                            todaySchedules,
                            Comparator.comparingInt(
                                    document ->
                                            convertTimeToMinutes(
                                                    document.getString(
                                                            "startTime"
                                                    )
                                            )
                            )
                    );

                    processTodaySchedule(
                            todaySchedules
                    );

                })
                .addOnFailureListener(e -> {

                    android.util.Log.e(
                            "WIMT_DEBUG",
                            "Firestore timetable error",
                            e
                    );

                    showNoCurrentClass();
                    showNoNextClass();
                    showNoTodaySchedule();

                    Toast.makeText(
                            this,
                            "Could not load timetable: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =========================================================
    // PROCESS TODAY'S SCHEDULE
    // =========================================================

    private void processTodaySchedule(
            List<DocumentSnapshot> schedules
    ) {

        if (schedules == null || schedules.isEmpty()) {

            showNoCurrentClass();
            showNoNextClass();
            showNoTodaySchedule();

            return;
        }

        Calendar now =
                Calendar.getInstance();

        int currentMinutes =
                now.get(Calendar.HOUR_OF_DAY) * 60
                        + now.get(Calendar.MINUTE);


        DocumentSnapshot currentClass =
                null;

        DocumentSnapshot nextClass =
                null;

        int nextStartMinutes =
                Integer.MAX_VALUE;


        // =====================================================
        // FIND CURRENT + NEXT
        // =====================================================

        for (DocumentSnapshot document : schedules) {

            String startTime =
                    document.getString("startTime");

            String endTime =
                    document.getString("endTime");


            if (startTime == null || endTime == null) {
                continue;
            }


            int start =
                    convertTimeToMinutes(startTime);

            int end =
                    convertTimeToMinutes(endTime);


            if (start < 0 || end < 0) {
                continue;
            }


            // -------------------------------------------------
            // CURRENT CLASS
            // -------------------------------------------------

            if (currentMinutes >= start
                    && currentMinutes < end) {

                currentClass = document;

                continue;
            }


            // -------------------------------------------------
            // NEXT CLASS
            // -------------------------------------------------

            if (start > currentMinutes
                    && start < nextStartMinutes) {

                nextStartMinutes = start;

                nextClass = document;
            }
        }


        // =====================================================
        // CURRENT
        // =====================================================

        if (currentClass != null) {

            displayCurrentClass(
                    currentClass
            );

        } else {

            showNoCurrentClass();
        }


        // =====================================================
        // NEXT
        // =====================================================

        if (nextClass != null) {

            displayNextClass(
                    nextClass
            );

        } else {

            showNoNextClass();
        }


        // =====================================================
        // IMPORTANT:
        // ALWAYS DISPLAY THE COMPLETE DAY
        // =====================================================

        displayTodaySchedule(
                schedules
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
                        "Current Class"
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


        currentSubjectText.setText(
                subject
        );


        currentTimeText.setText(
                start +
                        " - " +
                        end
        );


        currentBatchText.setText(
                buildAudienceText(
                        document
                )
        );


        currentRoomText.setText(
                buildRoomText(
                        document
                )
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
                        "Next Class"
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


        nextSubjectText.setText(
                subject
        );


        nextTimeText.setText(
                start +
                        " - " +
                        end
        );


        nextBatchText.setText(
                buildAudienceText(
                        document
                )
        );


        nextRoomText.setText(
                buildRoomText(
                        document
                )
        );
    }


    // =========================================================
    // DISPLAY COMPLETE TODAY'S SCHEDULE
    // =========================================================

    private void displayTodaySchedule(
            List<DocumentSnapshot> schedules
    ) {

        todayScheduleContainer.removeAllViews();

        if (schedules == null || schedules.isEmpty()) {

            showNoTodaySchedule();

            return;
        }


        todayScheduleLoading.setVisibility(
                View.GONE
        );


        for (DocumentSnapshot document :
                schedules) {

            addScheduleCard(
                    document
            );
        }
    }


    // =========================================================
    // ADD SCHEDULE CARD
    // =========================================================

    private void addScheduleCard(
            DocumentSnapshot document
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
                18,
                20,
                18
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


        // =====================================================
        // SUBJECT
        // =====================================================

        TextView subject =
                new TextView(
                        this
                );


        subject.setText(
                getValue(
                        document,
                        "subject",
                        "Class"
                )
        );


        subject.setTextSize(
                18
        );


        subject.setTextColor(
                0xFF111111
        );


        subject.setTypeface(
                null,
                Typeface.BOLD
        );


        card.addView(
                subject
        );


        // =====================================================
        // TIME
        // =====================================================

        TextView time =
                new TextView(
                        this
                );


        time.setText(
                getValue(
                        document,
                        "startTime",
                        ""
                ) +
                        " - " +
                        getValue(
                                document,
                                "endTime",
                                ""
                        )
        );


        time.setTextSize(
                15
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
                7,
                0,
                0
        );


        time.setLayoutParams(
                timeParams
        );


        card.addView(
                time
        );


        // =====================================================
        // AUDIENCE
        // =====================================================

        String audience =
                buildAudienceText(
                        document
                );


        if (
                !audience.isEmpty()
        ) {

            TextView audienceText =
                    new TextView(
                            this
                    );


            audienceText.setText(
                    audience
            );


            audienceText.setTextSize(
                    14
            );


            audienceText.setTextColor(
                    0xFF555555
            );


            LinearLayout.LayoutParams audienceParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            audienceParams.setMargins(
                    0,
                    5,
                    0,
                    0
            );


            audienceText.setLayoutParams(
                    audienceParams
            );


            card.addView(
                    audienceText
            );
        }


        // =====================================================
        // ROOM
        // =====================================================

        String room =
                buildRoomText(
                        document
                );


        if (
                !room.isEmpty()
        ) {

            TextView roomText =
                    new TextView(
                            this
                    );


            roomText.setText(
                    room
            );


            roomText.setTextSize(
                    14
            );


            roomText.setTextColor(
                    0xFF555555
            );


            LinearLayout.LayoutParams roomParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            roomParams.setMargins(
                    0,
                    4,
                    0,
                    0
            );


            roomText.setLayoutParams(
                    roomParams
            );


            card.addView(
                    roomText
            );
        }


        // =====================================================
        // TYPE
        // =====================================================

        String type =
                getValue(
                        document,
                        "type",
                        ""
                );


        if (
                !type.isEmpty()
        ) {

            TextView typeText =
                    new TextView(
                            this
                    );


            typeText.setText(
                    type
            );


            typeText.setTextSize(
                    13
            );


            typeText.setTextColor(
                    0xFF777777
            );


            LinearLayout.LayoutParams typeParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            typeParams.setMargins(
                    0,
                    5,
                    0,
                    0
            );


            typeText.setLayoutParams(
                    typeParams
            );


            card.addView(
                    typeText
            );
        }


        todayScheduleContainer.addView(
                card
        );
    }


    // =========================================================
    // BUILD AUDIENCE TEXT
    // =========================================================

    private String buildAudienceText(
            DocumentSnapshot document
    ) {

        StringBuilder result =
                new StringBuilder();

        List<String> programs =
                getStringList(
                        document,
                        "programs"
                );

        List<String> sections =
                getStringList(
                        document,
                        "sections"
                );

        String semester =
                getValue(
                        document,
                        "semester",
                        ""
                );

        String batch =
                getValue(
                        document,
                        "batch",
                        ""
                );


        // -----------------------------------------------------
        // PROGRAM
        // -----------------------------------------------------

        if (!programs.isEmpty()) {

            result.append("Program: ");

            result.append(
                    joinList(programs)
            );
        }


        // -----------------------------------------------------
        // SEMESTER
        // -----------------------------------------------------

        if (!semester.isEmpty()) {

            appendSeparator(result);

            result.append("Sem ");

            result.append(semester);
        }


        // -----------------------------------------------------
        // SECTION
        // -----------------------------------------------------

        if (!sections.isEmpty()) {

            appendSeparator(result);

            result.append("Section: ");

            result.append(
                    cleanSection(
                            sections.get(0)
                    )
            );
        }


        // -----------------------------------------------------
        // BATCH
        // -----------------------------------------------------

        if (!batch.isEmpty()) {

            appendSeparator(result);

            result.append("Batch: ");

            result.append(
                    cleanBatch(batch)
            );
        }


        return result.toString();
    }

    private String cleanSection(String section) {

        if (section == null) {
            return "";
        }

        section =
                section.trim()
                        .toUpperCase(Locale.US);


        /*
         * Examples:
         *
         * CE 5 A  -> A
         * CE 5 B  -> B
         * A       -> A
         * B       -> B
         * AB      -> AB
         */

        String[] parts =
                section.split("\\s+");

        if (parts.length > 0) {

            String last =
                    parts[parts.length - 1];

            if (last.matches("[A-Z]+")) {

                return last;
            }
        }


        return section;
    }

    private String cleanBatch(String batch) {

        if (batch == null) {
            return "";
        }

        batch =
                batch.trim()
                        .toUpperCase(Locale.US);


        /*
         * Examples:
         *
         * B01 -> 1
         * B02 -> 2
         * A1  -> 1
         * A2  -> 2
         * 1   -> 1
         * 2   -> 2
         */

        String digits =
                batch.replaceAll(
                        "[^0-9]",
                        ""
                );


        if (!digits.isEmpty()) {

            /*
             * Remove leading zero:
             *
             * 01 -> 1
             * 02 -> 2
             */
            try {

                return String.valueOf(
                        Integer.parseInt(digits)
                );

            } catch (NumberFormatException ignored) {

                return digits;
            }
        }


        return batch;
    }
    // =========================================================
    // BUILD ROOM TEXT
    // =========================================================

    private String buildRoomText(
            DocumentSnapshot document
    ) {

        String room =
                getValue(
                        document,
                        "room",
                        ""
                );


        if (
                room.isEmpty()
        ) {

            return "";
        }


        return "Room: " + room;
    }


    // =========================================================
    // STRING LIST
    // =========================================================

    private List<String> getStringList(
            DocumentSnapshot document,
            String field
    ) {

        List<String> result =
                new ArrayList<>();


        Object value =
                document.get(field);


        if (
                value instanceof List
        ) {

            List<?> list =
                    (List<?>) value;


            for (
                    Object item :
                    list
            ) {

                if (
                        item != null
                ) {

                    String text =
                            String.valueOf(
                                    item
                            ).trim();


                    if (
                            !text.isEmpty()
                    ) {

                        result.add(
                                text
                        );
                    }
                }
            }
        }


        return result;
    }


    // =========================================================
    // JOIN LIST
    // =========================================================

    private String joinList(
            List<String> values
    ) {

        if (
                values == null ||
                        values.isEmpty()
        ) {

            return "";
        }


        StringBuilder result =
                new StringBuilder();


        for (
                int i = 0;
                i < values.size();
                i++
        ) {

            if (
                    i > 0
            ) {

                result.append(
                        ", "
                );
            }


            result.append(
                    values.get(i)
            );
        }


        return result.toString();
    }


    // =========================================================
    // APPEND SEPARATOR
    // =========================================================

    private void appendSeparator(
            StringBuilder builder
    ) {

        if (
                builder.length() > 0
        ) {

            builder.append(
                    "  •  "
            );
        }
    }


    // =========================================================
    // NO CURRENT CLASS
    // =========================================================

    private void showNoCurrentClass() {

        currentSubjectText.setText(
                "No Current Class"
        );

        currentTimeText.setText("");

        currentBatchText.setText("");

        currentRoomText.setText("");
    }


    // =========================================================
    // NO NEXT CLASS
    // =========================================================

    private void showNoNextClass() {

        nextSubjectText.setText(
                "No More Classes"
        );

        nextTimeText.setText("");

        nextBatchText.setText("");

        nextRoomText.setText("");
    }


    // =========================================================
    // NO TODAY SCHEDULE
    // =========================================================

    private void showNoTodaySchedule() {

        todayScheduleContainer.removeAllViews();


        TextView text =
                new TextView(
                        this
                );


        text.setText(
                "No classes scheduled for today."
        );


        text.setTextSize(
                15
        );


        text.setTextColor(
                0xFF666666
        );


        text.setGravity(
                Gravity.CENTER
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


        todayScheduleContainer.addView(
                text
        );
    }


    // =========================================================
    // LOAD STUDENT REQUESTS
    // =========================================================

    private void loadStudentRequests() {

        if (teacherCode == null || teacherCode.trim().isEmpty()) {

            showNoRequests();

            return;
        }

        final String currentTeacherCode =
                teacherCode.trim().toUpperCase(Locale.US);

        android.util.Log.d(
                "WIMT_REQUESTS",
                "Loading requests for teacherCode = "
                        + currentTeacherCode
        );

        db.collection("meetingRequests")
                .whereEqualTo(
                        "teacherCode",
                        currentTeacherCode
                )
                .whereEqualTo(
                        "status",
                        "PENDING"
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            requestContainer.removeAllViews();

                            android.util.Log.d(
                                    "WIMT_REQUESTS",
                                    "Pending requests found = "
                                            + querySnapshot.size()
                            );

                            if (querySnapshot.isEmpty()) {

                                showNoRequests();

                                return;
                            }

                            for (
                                    DocumentSnapshot request :
                                    querySnapshot.getDocuments()
                            ) {

                                addRequestCard(request);
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            android.util.Log.e(
                                    "WIMT_REQUESTS",
                                    "Could not load meeting requests",
                                    e
                            );

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

    // =========================================================
    // REQUEST CARD
    // =========================================================

    private int dp(int value) {
        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }


    private void addRequestCard(
            DocumentSnapshot request
    ) {

        // IMPORTANT:
        // Android LayoutParams use PIXELS, not dp.
        // The previous request-card code used values such as 52
        // directly, which made the Accept/Reject buttons only a
        // few pixels high on a modern phone. That is why they
        // appeared as thin blank bars.

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
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
                dp(14)
        );

        card.setLayoutParams(cardParams);


        // =====================================================
        // STUDENT NAME
        // =====================================================

        String studentName =
                getValue(
                        request,
                        "studentName",
                        "Student"
                );

        TextView name =
                new TextView(this);

        name.setText(studentName);
        name.setTextSize(19);
        name.setTextColor(0xFF222222);
        name.setTypeface(
                null,
                Typeface.BOLD
        );

        card.addView(name);


        // =====================================================
        // EMAIL
        // =====================================================

        String studentEmail =
                getValue(
                        request,
                        "studentEmail",
                        ""
                );

        if (!studentEmail.isEmpty()) {

            TextView email =
                    new TextView(this);

            email.setText(studentEmail);
            email.setTextSize(14);
            email.setTextColor(0xFF666666);

            LinearLayout.LayoutParams emailParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            emailParams.setMargins(
                    0,
                    dp(4),
                    0,
                    0
            );

            email.setLayoutParams(emailParams);

            card.addView(email);
        }


        // =====================================================
        // DATE + TIME
        // =====================================================

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
                new TextView(this);

        time.setText(
                date
                        + "    "
                        + start
                        + " - "
                        + end
        );

        time.setTextSize(16);
        time.setTextColor(0xFF444444);

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        timeParams.setMargins(
                0,
                dp(10),
                0,
                0
        );

        time.setLayoutParams(timeParams);

        card.addView(time);


        // =====================================================
        // MESSAGE / SUBJECT
        // =====================================================

        String subject =
                getValue(
                        request,
                        "subject",
                        ""
                );

        String message =
                getValue(
                        request,
                        "message",
                        ""
                );

        String displayMessage =
                !message.isEmpty()
                        ? message
                        : subject;

        if (!displayMessage.isEmpty()) {

            TextView messageText =
                    new TextView(this);

            messageText.setText(
                    "Message: "
                            + displayMessage
            );

            messageText.setTextSize(15);
            messageText.setTextColor(0xFF555555);

            LinearLayout.LayoutParams messageParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            messageParams.setMargins(
                    0,
                    dp(10),
                    0,
                    0
            );

            messageText.setLayoutParams(
                    messageParams
            );

            card.addView(messageText);
        }


        // =====================================================
        // ACCEPT / REJECT BUTTONS
        // =====================================================

        LinearLayout buttons =
                new LinearLayout(this);

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
                dp(18),
                0,
                0
        );

        buttons.setLayoutParams(
                buttonsParams
        );


        Button accept =
                new Button(this);

        accept.setText("Accept");
        accept.setTextSize(14);
        accept.setTextColor(0xFFFFFFFF);
        accept.setAllCaps(false);
        accept.setMinHeight(0);
        accept.setMinimumHeight(0);
        accept.setPadding(
                dp(8),
                0,
                dp(8),
                0
        );


        Button reject =
                new Button(this);

        reject.setText("Reject");
        reject.setTextSize(14);
        reject.setTextColor(0xFFFFFFFF);
        reject.setAllCaps(false);
        reject.setMinHeight(0);
        reject.setMinimumHeight(0);
        reject.setPadding(
                dp(8),
                0,
                dp(8),
                0
        );


        LinearLayout.LayoutParams acceptParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48)
                );

        acceptParams.weight = 1;

        accept.setLayoutParams(
                acceptParams
        );


        LinearLayout.LayoutParams rejectParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48)
                );

        rejectParams.weight = 1;

        rejectParams.setMargins(
                dp(12),
                0,
                0,
                0
        );

        reject.setLayoutParams(
                rejectParams
        );


        buttons.addView(accept);
        buttons.addView(reject);

        card.addView(buttons);


        // =====================================================
        // ACTIONS
        // =====================================================

        accept.setOnClickListener(
                v -> showResponseDialog(
                        request.getId(),
                        "ACCEPTED"
                )
        );

        reject.setOnClickListener(
                v -> showResponseDialog(
                        request.getId(),
                        "REJECTED"
                )
        );


        requestContainer.addView(card);
    }


    // =========================================================
    // UPDATE REQUEST STATUS
    // =========================================================

    private void showResponseDialog(
            String requestId,
            String status
    ) {

        final android.widget.EditText input =
                new android.widget.EditText(this);

        input.setHint(
                status.equals("ACCEPTED")
                        ? "Message to student"
                        : "Reason for rejection"
        );

        input.setSingleLine(false);
        input.setMinLines(2);
        input.setPadding(
                20,
                20,
                20,
                20
        );


        android.app.AlertDialog dialog =
                new android.app.AlertDialog.Builder(this)
                        .setTitle(
                                status.equals("ACCEPTED")
                                        ? "Accept Meeting Request"
                                        : "Reject Meeting Request"
                        )
                        .setMessage(
                                status.equals("ACCEPTED")
                                        ? "Add a response message for the student."
                                        : "Please provide a reason for rejecting the request."
                        )
                        .setView(input)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                status.equals("ACCEPTED")
                                        ? "Accept"
                                        : "Reject",
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                dialogInterface -> {

                    Button positiveButton =
                            dialog.getButton(
                                    android.app.AlertDialog.BUTTON_POSITIVE
                            );

                    positiveButton.setOnClickListener(
                            v -> {

                                String response =
                                        input.getText()
                                                .toString()
                                                .trim();

                                if (response.isEmpty()) {

                                    input.setError(
                                            "Response message is required"
                                    );

                                    input.requestFocus();

                                    return;
                                }

                                dialog.dismiss();

                                updateRequestStatus(
                                        requestId,
                                        status,
                                        response
                                );
                            }
                    );
                }
        );


        dialog.show();
    }


    private void updateRequestStatus(
            String requestId,
            String status,
            String responseMessage
    ) {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                status
        );

        updates.put(
                "responseMessage",
                responseMessage
        );

        updates.put(
                "respondedAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        updates.put(
                "respondedBy",
                currentUser.getUid()
        );

        updates.put(
                "updatedAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );


        db.collection("meetingRequests")
                .document(requestId)
                .update(updates)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    status.equals("ACCEPTED")
                                            ? "Meeting request accepted."
                                            : "Meeting request rejected.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadStudentRequests();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            android.util.Log.e(
                                    "WIMT_REQUESTS",
                                    "Could not update meeting request",
                                    e
                            );

                            Toast.makeText(
                                    this,
                                    "Could not update request: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
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
                Gravity.CENTER
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
                time == null ||
                        time.trim().isEmpty()
        ) {

            return -1;
        }


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


                format.setLenient(
                        false
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

        Object value =
                document.get(field);

        if (value == null) {
            return defaultValue;
        }

        return String.valueOf(value).trim();
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @Override

    protected void onResume() {

        super.onResume();

        if (teacherCode != null
                && !teacherCode.trim().isEmpty()) {

            loadStudentRequests();

        } else if (teacherUid != null) {

            loadTeacherProfile();
        }
    }
}
