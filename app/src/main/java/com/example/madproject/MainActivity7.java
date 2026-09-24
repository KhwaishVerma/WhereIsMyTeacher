package com.example.madproject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity7 extends AppCompatActivity {

    private static final String TAG = "MainActivity7";

    private FirebaseFirestore db;

    // -----------------------------------------------------
    // UI
    // -----------------------------------------------------

    private TextView teacherNameText;
    private TextView loadingText;

    private LinearLayout mondayAvailability;
    private LinearLayout tuesdayAvailability;
    private LinearLayout wednesdayAvailability;
    private LinearLayout thursdayAvailability;
    private LinearLayout fridayAvailability;


    // -----------------------------------------------------
    // TEACHER
    // -----------------------------------------------------

    private String teacherCode = "";
    private String teacherName = "";


    // -----------------------------------------------------
    // OFFICIAL CLASS SLOTS
    // -----------------------------------------------------

    private static final String[] SLOT_STARTS = {
            "09:00",
            "10:00",
            "11:10",
            "12:10",
            "14:10",
            "15:10"
    };

    private static final String[] SLOT_ENDS = {
            "10:00",
            "11:00",
            "12:10",
            "13:10",
            "15:10",
            "16:10"
    };


    // -----------------------------------------------------
    // CANONICAL FIRESTORE DAY VALUES
    // -----------------------------------------------------

    /*
     * These match the values used by the current
     * master timetable.
     *
     * normalizeDay() below also accepts Tue/Thu/etc.
     */

    private static final String[] DAYS = {
            "Mon",
            "Tues",
            "Wed",
            "Thurs",
            "Fri"
    };


    // -----------------------------------------------------
    // DATA
    // -----------------------------------------------------

    private final Map<String, List<ScheduleItem>> weeklySchedule =
            new HashMap<>();

    private final List<OverrideItem> overrides =
            new ArrayList<>();


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main7);


        // -------------------------------------------------
        // WINDOW INSETS
        // -------------------------------------------------

        View root = findViewById(R.id.main);

        if (root != null) {

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
        }


        // -------------------------------------------------
        // FIREBASE
        // -------------------------------------------------

        db = FirebaseFirestore.getInstance();


        // -------------------------------------------------
        // UI
        // -------------------------------------------------

        teacherNameText =
                findViewById(R.id.textViewTeacherName);

        loadingText =
                findViewById(R.id.loadingText);

        mondayAvailability =
                findViewById(R.id.mondayAvailability);

        tuesdayAvailability =
                findViewById(R.id.tuesdayAvailability);

        wednesdayAvailability =
                findViewById(R.id.wednesdayAvailability);

        thursdayAvailability =
                findViewById(R.id.thursdayAvailability);

        fridayAvailability =
                findViewById(R.id.fridayAvailability);


        TextView backButton =
                findViewById(R.id.backButton);

        if (backButton != null) {

            backButton.setOnClickListener(
                    v -> finish()
            );
        }


        // -------------------------------------------------
        // INITIALIZE DAYS
        // -------------------------------------------------

        for (String day : DAYS) {

            weeklySchedule.put(
                    day,
                    new ArrayList<>()
            );
        }


        // -------------------------------------------------
        // GET TEACHER CODE
        // -------------------------------------------------

        Intent intent = getIntent();

        if (intent != null) {

            teacherCode =
                    safeString(
                            intent.getStringExtra(
                                    "teacherCode"
                            )
                    );

            teacherName =
                    safeString(
                            intent.getStringExtra(
                                    "teacherName"
                            )
                    );
        }


        Log.d(
                TAG,
                "Received teacherCode = [" +
                        teacherCode +
                        "]"
        );

        Log.d(
                TAG,
                "Received teacherName = [" +
                        teacherName +
                        "]"
        );


        // -------------------------------------------------
        // VALIDATE
        // -------------------------------------------------

        if (teacherCode.isEmpty()) {

            loadingText.setText(
                    "Teacher code was not provided."
            );

            return;
        }


        // -------------------------------------------------
        // SHOW NAME RECEIVED FROM PREVIOUS SCREEN
        // -------------------------------------------------

        if (!teacherName.isEmpty()) {

            teacherNameText.setText(
                    teacherName
            );
        }


        // -------------------------------------------------
        // LOAD TEACHER
        // -------------------------------------------------

        loadTeacher();
    }


    // =====================================================
    // LOAD TEACHER
    // =====================================================

    private void loadTeacher() {

        loadingText.setVisibility(View.VISIBLE);

        loadingText.setText(
                "Loading timetable..."
        );


        /*
         * teacherDirectory is the canonical source for:
         *
         * teacherCode
         * teacher name
         *
         * No department is required anymore.
         */

        db.collection("teacherDirectory")
                .document(teacherCode)
                .get()
                .addOnSuccessListener(document -> {

                    Log.d(
                            TAG,
                            "teacherDirectory lookup successful. " +
                                    "exists = " +
                                    document.exists()
                    );


                    if (document.exists()) {

                        String name =
                                document.getString("name");


                        if (
                                name != null &&
                                        !name.trim().isEmpty()
                        ) {

                            teacherName =
                                    name.trim();

                            teacherNameText.setText(
                                    teacherName
                            );
                        }
                    }


                    loadMasterSchedule();

                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "teacherDirectory lookup failed",
                            e
                    );


                    /*
                     * Do NOT stop the timetable from loading.
                     */

                    loadMasterSchedule();
                });
    }


    // =====================================================
    // LOAD MASTER TIMETABLE
    // =====================================================

    private void loadMasterSchedule() {

        loadingText.setText(
                "Loading timetable..."
        );


        Log.d(
                TAG,
                "Querying masterSchedules for teacherCode = [" +
                        teacherCode +
                        "]"
        );


        db.collection("masterSchedules")
                .whereArrayContains(
                        "teacherCodes",
                        teacherCode
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    Log.d(
                            TAG,
                            "masterSchedules returned " +
                                    querySnapshot.size() +
                                    " documents."
                    );


                    // -----------------------------------------
                    // CLEAR OLD DATA
                    // -----------------------------------------

                    weeklySchedule.clear();

                    for (String day : DAYS) {

                        weeklySchedule.put(
                                day,
                                new ArrayList<>()
                        );
                    }


                    // -----------------------------------------
                    // READ DOCUMENTS
                    // -----------------------------------------

                    for (
                            DocumentSnapshot document :
                            querySnapshot
                    ) {

                        String rawDay =
                                document.getString("day");


                        String day =
                                normalizeDay(rawDay);


                        String start =
                                safeString(
                                        document.getString(
                                                "startTime"
                                        )
                                );


                        String end =
                                safeString(
                                        document.getString(
                                                "endTime"
                                        )
                                );


                        String subject =
                                safeString(
                                        document.getString(
                                                "subject"
                                        )
                                );


                        String type =
                                safeString(
                                        document.getString(
                                                "type"
                                        )
                                );


                        String room =
                                safeString(
                                        document.getString(
                                                "room"
                                        )
                                );


                        Log.d(
                                TAG,
                                "Schedule: " +
                                        document.getId() +
                                        " | day=" +
                                        rawDay +
                                        " -> " +
                                        day +
                                        " | " +
                                        start +
                                        "-" +
                                        end +
                                        " | " +
                                        subject
                        );


                        if (
                                day.isEmpty() ||
                                        start.isEmpty() ||
                                        end.isEmpty()
                        ) {

                            continue;
                        }


                        ScheduleItem item =
                                new ScheduleItem();


                        item.documentId =
                                document.getId();

                        item.day =
                                day;

                        item.startTime =
                                start;

                        item.endTime =
                                end;

                        item.subject =
                                subject;

                        item.type =
                                type;

                        item.room =
                                room;


                        if (
                                weeklySchedule.containsKey(day)
                        ) {

                            weeklySchedule
                                    .get(day)
                                    .add(item);
                        }
                    }


                    /*
                     * Master timetable has loaded.
                     *
                     * Render it immediately.
                     *
                     * This means even if the override query
                     * subsequently fails, the student still
                     * gets the timetable.
                     */

                    renderWeeklyAvailability();


                    /*
                     * Now try to load teacher overrides.
                     */

                    loadOverrides();

                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "masterSchedules query FAILED",
                            e
                    );


                    loadingText.setText(
                            "Could not load timetable."
                    );


                    Toast.makeText(
                            this,
                            "Timetable error: " +
                                    e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =====================================================
    // LOAD OVERRIDES
    // =====================================================

    private void loadOverrides() {

        Log.d(
                TAG,
                "Loading overrides for [" +
                        teacherCode +
                        "]"
        );


        db.collection("teacherScheduleOverrides")
                .document(teacherCode)
                .collection("entries")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    Log.d(
                            TAG,
                            "Overrides returned " +
                                    querySnapshot.size() +
                                    " documents."
                    );


                    overrides.clear();


                    for (
                            DocumentSnapshot document :
                            querySnapshot
                    ) {

                        OverrideItem item =
                                new OverrideItem();


                        item.documentId =
                                document.getId();

                        item.day =
                                normalizeDay(
                                        document.getString(
                                                "day"
                                        )
                                );

                        item.action =
                                safeString(
                                        document.getString(
                                                "action"
                                        )
                                );

                        item.startTime =
                                safeString(
                                        document.getString(
                                                "startTime"
                                        )
                                );

                        item.endTime =
                                safeString(
                                        document.getString(
                                                "endTime"
                                        )
                                );

                        item.status =
                                safeString(
                                        document.getString(
                                                "status"
                                        )
                                );

                        item.masterScheduleId =
                                safeString(
                                        document.getString(
                                                "masterScheduleId"
                                        )
                                );


                        Long slot =
                                document.getLong("slot");


                        if (slot != null) {

                            item.slot =
                                    slot.intValue();
                        }


                        overrides.add(item);
                    }


                    /*
                     * Re-render after overrides are available.
                     */

                    renderWeeklyAvailability();

                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Override query failed. " +
                                    "Showing master timetable only.",
                            e
                    );


                    /*
                     * VERY IMPORTANT:
                     *
                     * Override failure must NOT make the
                     * whole screen blank.
                     */

                    overrides.clear();

                    renderWeeklyAvailability();
                });
    }


    // =====================================================
    // RENDER WEEK
    // =====================================================

    private void renderWeeklyAvailability() {

        loadingText.setVisibility(
                View.GONE
        );


        mondayAvailability.removeAllViews();

        tuesdayAvailability.removeAllViews();

        wednesdayAvailability.removeAllViews();

        thursdayAvailability.removeAllViews();

        fridayAvailability.removeAllViews();


        renderDay(
                "Mon",
                mondayAvailability
        );

        renderDay(
                "Tues",
                tuesdayAvailability
        );

        renderDay(
                "Wed",
                wednesdayAvailability
        );

        renderDay(
                "Thurs",
                thursdayAvailability
        );

        renderDay(
                "Fri",
                fridayAvailability
        );
    }


    // =====================================================
    // RENDER DAY
    // =====================================================

    private void renderDay(
            String day,
            LinearLayout container
    ) {

        List<ScheduleItem> schedules =
                weeklySchedule.get(day);


        if (schedules == null) {

            schedules =
                    new ArrayList<>();
        }


        for (int i = 0; i < SLOT_STARTS.length; i++) {

            String start =
                    SLOT_STARTS[i];

            String end =
                    SLOT_ENDS[i];


            // ---------------------------------------------
            // SHORT BREAK
            // ---------------------------------------------

            if (i == 2) {

                addBreakRow(
                        container,
                        "11:00",
                        "11:10",
                        "SHORT BREAK"
                );
            }


            // ---------------------------------------------
            // LUNCH
            // ---------------------------------------------

            if (i == 4) {

                addBreakRow(
                        container,
                        "13:10",
                        "14:10",
                        "LUNCH BREAK"
                );
            }


            // ---------------------------------------------
            // FIND CLASS
            // ---------------------------------------------

            ScheduleItem schedule =
                    findScheduleForSlot(
                            start,
                            end,
                            schedules
                    );


            // ---------------------------------------------
            // FIND STATUS
            // ---------------------------------------------

            OverrideItem status =
                    findStatusOverride(
                            day,
                            start,
                            end
                    );


            // ---------------------------------------------
            // RENDER
            // ---------------------------------------------

            if (
                    status != null &&
                            !status.status.isEmpty()
            ) {

                addStatusRow(
                        container,
                        start,
                        end,
                        status.status
                );

            } else if (schedule != null) {

                addClassRow(
                        container,
                        start,
                        end,
                        schedule
                );

            } else {

                addAvailableRow(
                        container,
                        start,
                        end
                );
            }
        }
    }


    // =====================================================
    // FIND SCHEDULE
    // =====================================================

    private ScheduleItem findScheduleForSlot(
            String slotStart,
            String slotEnd,
            List<ScheduleItem> schedules
    ) {

        int slotStartMinutes =
                timeToMinutes(slotStart);

        int slotEndMinutes =
                timeToMinutes(slotEnd);


        for (
                ScheduleItem item :
                schedules
        ) {

            int classStart =
                    timeToMinutes(
                            item.startTime
                    );

            int classEnd =
                    timeToMinutes(
                            item.endTime
                    );


            if (
                    classStart < slotEndMinutes &&
                            classEnd > slotStartMinutes
            ) {

                return item;
            }
        }


        return null;
    }


    // =====================================================
    // FIND STATUS OVERRIDE
    // =====================================================

    private OverrideItem findStatusOverride(
            String day,
            String slotStart,
            String slotEnd
    ) {

        int slotStartMinutes =
                timeToMinutes(slotStart);

        int slotEndMinutes =
                timeToMinutes(slotEnd);


        for (
                OverrideItem item :
                overrides
        ) {

            if (
                    !item.day.equals(day)
            ) {

                continue;
            }


            if (
                    !"STATUS".equalsIgnoreCase(
                            item.action
                    )
            ) {

                continue;
            }


            if (
                    item.startTime.isEmpty()
            ) {

                continue;
            }


            int overrideStart =
                    timeToMinutes(
                            item.startTime
                    );


            int overrideEnd;


            if (
                    item.endTime.isEmpty()
            ) {

                overrideEnd =
                        overrideStart + 60;

            } else {

                overrideEnd =
                        timeToMinutes(
                                item.endTime
                        );
            }


            if (
                    overrideStart < slotEndMinutes &&
                            overrideEnd > slotStartMinutes
            ) {

                return item;
            }
        }


        return null;
    }


    // =====================================================
    // AVAILABLE
    // =====================================================

    private void addAvailableRow(
            LinearLayout container,
            String start,
            String end
    ) {

        TextView row =
                createRow();


        row.setText(
                formatTime(start) +
                        " - " +
                        formatTime(end) +
                        "    AVAILABLE"
        );


        row.setTextColor(
                Color.rgb(
                        25,
                        120,
                        70
                )
        );


        row.setTypeface(
                null,
                Typeface.BOLD
        );


        container.addView(row);
    }


    // =====================================================
    // CLASS
    // =====================================================

    private void addClassRow(
            LinearLayout container,
            String slotStart,
            String slotEnd,
            ScheduleItem item
    ) {

        TextView row =
                createRow();


        String subject =
                item.subject.isEmpty()
                        ? "Class"
                        : item.subject;


        StringBuilder text =
                new StringBuilder();


        text.append(
                formatTime(slotStart)
        );

        text.append(
                " - "
        );

        text.append(
                formatTime(slotEnd)
        );

        text.append(
                "    "
        );

        text.append(
                subject
        );


        if (!item.type.isEmpty()) {

            text.append(
                    " [" +
                            item.type +
                            "]"
            );
        }


        if (!item.room.isEmpty()) {

            text.append(
                    "    " +
                            item.room
            );
        }


        /*
         * Show complete duration for a 2-slot class/lab.
         */

        if (
                !item.startTime.equals(slotStart) ||
                        !item.endTime.equals(slotEnd)
        ) {

            text.append(
                    "\nClass duration: "
            );

            text.append(
                    formatTime(
                            item.startTime
                    )
            );

            text.append(
                    " - "
            );

            text.append(
                    formatTime(
                            item.endTime
                    )
            );
        }


        row.setText(
                text.toString()
        );


        row.setTextColor(
                Color.rgb(
                        30,
                        30,
                        30
                )
        );


        row.setTypeface(
                null,
                Typeface.BOLD
        );


        container.addView(row);
    }


    // =====================================================
    // STATUS
    // =====================================================

    private void addStatusRow(
            LinearLayout container,
            String start,
            String end,
            String status
    ) {

        TextView row =
                createRow();


        row.setText(
                formatTime(start) +
                        " - " +
                        formatTime(end) +
                        "    " +
                        status.toUpperCase(
                                Locale.US
                        )
        );


        row.setTextColor(
                Color.rgb(
                        190,
                        70,
                        50
                )
        );


        row.setTypeface(
                null,
                Typeface.BOLD
        );


        container.addView(row);
    }


    // =====================================================
    // BREAK
    // =====================================================

    private void addBreakRow(
            LinearLayout container,
            String start,
            String end,
            String title
    ) {

        TextView row =
                createRow();


        row.setText(
                formatTime(start) +
                        " - " +
                        formatTime(end) +
                        "    " +
                        title
        );


        row.setTextColor(
                Color.rgb(
                        110,
                        110,
                        110
                )
        );


        row.setTypeface(
                null,
                Typeface.ITALIC
        );


        container.addView(row);
    }


    // =====================================================
    // BASE ROW
    // =====================================================

    private TextView createRow() {

        TextView row =
                new TextView(this);


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        params.setMargins(
                0,
                2,
                0,
                2
        );


        row.setLayoutParams(params);

        row.setTextSize(14);

        row.setPadding(
                12,
                12,
                12,
                12
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setBackgroundColor(
                Color.WHITE
        );


        return row;
    }


    // =====================================================
    // NORMALIZE DAY
    // =====================================================

    private String normalizeDay(
            String day
    ) {

        if (day == null) {

            return "";
        }


        String value =
                day.trim()
                        .toLowerCase(
                                Locale.US
                        );


        switch (value) {

            case "mon":
            case "monday":

                return "Mon";


            case "tue":
            case "tues":
            case "tuesday":

                return "Tues";


            case "wed":
            case "wednesday":

                return "Wed";


            case "thu":
            case "thur":
            case "thurs":
            case "thursday":

                return "Thurs";


            case "fri":
            case "friday":

                return "Fri";


            default:

                return "";
        }
    }


    // =====================================================
    // TIME TO MINUTES
    // =====================================================

    private int timeToMinutes(
            String time
    ) {

        try {

            String[] parts =
                    time.split(":");


            int hour =
                    Integer.parseInt(
                            parts[0]
                    );


            int minute =
                    Integer.parseInt(
                            parts[1]
                    );


            return (
                    hour * 60
            ) + minute;

        } catch (Exception e) {

            return -1;
        }
    }


    // =====================================================
    // FORMAT TIME
    // =====================================================

    private String formatTime(
            String time
    ) {

        try {

            int minutes =
                    timeToMinutes(time);


            if (minutes < 0) {

                return time;
            }


            int hour =
                    minutes / 60;

            int minute =
                    minutes % 60;


            String amPm =
                    hour >= 12
                            ? "PM"
                            : "AM";


            int displayHour =
                    hour % 12;


            if (displayHour == 0) {

                displayHour = 12;
            }


            return String.format(
                    Locale.US,
                    "%02d:%02d %s",
                    displayHour,
                    minute,
                    amPm
            );

        } catch (Exception e) {

            return time;
        }
    }


    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safeString(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    // =====================================================
    // SCHEDULE MODEL
    // =====================================================

    private static class ScheduleItem {

        String documentId = "";

        String day = "";

        String startTime = "";

        String endTime = "";

        String subject = "";

        String type = "";

        String room = "";
    }


    // =====================================================
    // OVERRIDE MODEL
    // =====================================================

    private static class OverrideItem {

        String documentId = "";

        String day = "";

        String action = "";

        String startTime = "";

        String endTime = "";

        String status = "";

        String masterScheduleId = "";

        int slot = -1;
    }
}