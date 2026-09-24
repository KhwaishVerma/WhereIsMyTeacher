package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity6 extends AppCompatActivity {

    // ======================================================
    // VIEWS
    // ======================================================

    private AutoCompleteTextView searchTeacher;

    private LinearLayout teacherContentContainer;
    private LinearLayout teacherScheduleContainer;

    private TextView teacherNameText;
    private TextView teacherDepartmentText;

    private TextView currentStatusText;
    private TextView currentLocationText;
    private TextView currentTimeText;

    private TextView scheduleLoadingText;

    private Button availabilityButton;
    private Button requestMeetingButton;


    // ======================================================
    // FIREBASE
    // ======================================================

    private FirebaseFirestore db;


    // ======================================================
    // TEACHERS
    // ======================================================

    private final List<TeacherInfo> teachers =
            new ArrayList<>();

    private TeacherInfo selectedTeacher;


    // ======================================================
    // LIFECYCLE
    // ======================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_main6
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


        // ==================================================
        // INITIALIZE VIEWS
        // ==================================================

        searchTeacher =
                findViewById(
                        R.id.editTextSearchTeacher
                );

        teacherContentContainer =
                findViewById(
                        R.id.teacherContentContainer
                );

        teacherScheduleContainer =
                findViewById(
                        R.id.teacherScheduleContainer
                );

        teacherNameText =
                findViewById(
                        R.id.textViewTeacherName
                );

        teacherDepartmentText =
                findViewById(
                        R.id.textViewTeacherDepartment
                );

        currentStatusText =
                findViewById(
                        R.id.textViewCurrentStatus
                );

        currentLocationText =
                findViewById(
                        R.id.textViewLocation
                );

        currentTimeText =
                findViewById(
                        R.id.textViewCurrentTime
                );

        scheduleLoadingText =
                findViewById(
                        R.id.scheduleLoadingText
                );

        availabilityButton =
                findViewById(
                        R.id.availabilityButton
                );

        requestMeetingButton =
                findViewById(
                        R.id.buttonRequestMeeting
                );


        // ==================================================
        // FIREBASE
        // ==================================================

        db =
                FirebaseFirestore.getInstance();


        // ==================================================
        // LOAD TEACHER DIRECTORY
        // ==================================================

        loadTeacherDirectory();


        // ==================================================
        // SEARCH / SELECT TEACHER
        // ==================================================

        searchTeacher.setOnItemClickListener(
                (parent, view, position, id) -> {

                    TeacherInfo teacher =
                            (TeacherInfo)
                                    parent.getItemAtPosition(
                                            position
                                    );

                    selectTeacher(
                            teacher
                    );
                }
        );


        // ==================================================
        // FULL WEEK AVAILABILITY
        // ==================================================

        availabilityButton.setOnClickListener(
                v -> {

                    if (selectedTeacher == null) {

                        Toast.makeText(
                                MainActivity6.this,
                                getString(
                                        R.string.select_teacher_first
                                ),
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    Intent intent =
                            new Intent(
                                    MainActivity6.this,
                                    MainActivity7.class
                            );

                    /*
                     * Teacher is identified ONLY by teacherCode.
                     */
                    intent.putExtra(
                            "teacherCode",
                            selectedTeacher.teacherCode
                    );

                    intent.putExtra(
                            "teacherName",
                            selectedTeacher.name
                    );

                    startActivity(intent);
                }
        );


        // ==================================================
        // REQUEST MEETING
        // ==================================================

        requestMeetingButton.setOnClickListener(
                v -> {

                    if (selectedTeacher == null) {

                        Toast.makeText(
                                MainActivity6.this,
                                getString(
                                        R.string.select_teacher_first
                                ),
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    Intent intent =
                            new Intent(
                                    MainActivity6.this,
                                    request_meeting.class
                            );

                    /*
                     * IMPORTANT:
                     *
                     * NO teacherUid.
                     *
                     * teacherCode is the teacher identifier.
                     */
                    intent.putExtra(
                            "teacherCode",
                            selectedTeacher.teacherCode
                    );

                    intent.putExtra(
                            "teacherName",
                            selectedTeacher.name
                    );

                    startActivity(intent);
                }
        );
    }


    // ======================================================
    // LOAD TEACHER DIRECTORY
    // ======================================================

    private void loadTeacherDirectory() {

        db.collection("teacherDirectory")
                .whereEqualTo(
                        "active",
                        true
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            teachers.clear();


                            for (DocumentSnapshot document :
                                    querySnapshot.getDocuments()) {

                                String code =
                                        document.getString(
                                                "teacherCode"
                                        );

                                String name =
                                        document.getString(
                                                "name"
                                        );

                                /*
                                 * Department is optional.
                                 *
                                 * If you later add:
                                 *
                                 * department: "STME"
                                 *
                                 * to teacherDirectory, it will
                                 * automatically be displayed.
                                 */
                                String department =
                                        document.getString(
                                                "department"
                                        );


                                if (code == null
                                        || name == null) {

                                    continue;
                                }


                                teachers.add(
                                        new TeacherInfo(
                                                code.trim(),
                                                name.trim(),
                                                department == null
                                                        ? ""
                                                        : department.trim()
                                        )
                                );
                            }


                            Collections.sort(
                                    teachers,
                                    Comparator.comparing(
                                            teacher ->
                                                    teacher.name
                                                            .toLowerCase(
                                                                    Locale.US
                                                            )
                                    )
                            );


                            ArrayAdapter<TeacherInfo> adapter =
                                    new ArrayAdapter<>(
                                            MainActivity6.this,
                                            android.R.layout.simple_dropdown_item_1line,
                                            teachers
                                    );


                            searchTeacher.setAdapter(
                                    adapter
                            );

                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    MainActivity6.this,
                                    getString(
                                            R.string.teacher_directory_load_failed
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // ======================================================
    // SELECT TEACHER
    // ======================================================

    private void selectTeacher(
            TeacherInfo teacher
    ) {

        selectedTeacher =
                teacher;


        teacherContentContainer.setVisibility(
                View.VISIBLE
        );


        teacherNameText.setText(
                teacher.name
                        + " ("
                        + teacher.teacherCode
                        + ")"
        );


        // --------------------------------------------------
        // DEPARTMENT
        // --------------------------------------------------

        if (teacher.department != null
                && !teacher.department.isEmpty()) {

            teacherDepartmentText.setText(
                    teacher.department
            );

        } else {

            teacherDepartmentText.setText(
                    getString(
                            R.string.department_not_available
                    )
            );
        }


        // --------------------------------------------------
        // INITIAL STATUS
        // --------------------------------------------------

        currentStatusText.setText(
                getString(
                        R.string.loading
                )
        );

        currentLocationText.setText(
                getString(
                        R.string.loading
                )
        );

        currentTimeText.setText(
                getString(
                        R.string.loading
                )
        );


        // --------------------------------------------------
        // LOAD TODAY
        // --------------------------------------------------

        loadTodaySchedule(
                teacher
        );
    }


    // ======================================================
    // LOAD TODAY'S SCHEDULE
    // ======================================================

    private void loadTodaySchedule(
            TeacherInfo teacher
    ) {

        scheduleLoadingText.setVisibility(
                View.VISIBLE
        );

        scheduleLoadingText.setText(
                getString(
                        R.string.loading
                )
        );


        while (teacherScheduleContainer
                .getChildCount() > 1) {

            teacherScheduleContainer.removeViewAt(
                    1
            );
        }


        String today =
                new SimpleDateFormat(
                        "EEE",
                        Locale.US
                ).format(
                        Calendar.getInstance()
                                .getTime()
                );


        String normalizedToday =
                normalizeDay(
                        today
                );


        db.collection("masterSchedules")
                .whereArrayContains(
                        "teacherCodes",
                        teacher.teacherCode
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            List<DocumentSnapshot> todaySchedules =
                                    new ArrayList<>();


                            for (DocumentSnapshot document :
                                    querySnapshot.getDocuments()) {

                                String day =
                                        document.getString(
                                                "day"
                                        );

                                if (day == null) {
                                    continue;
                                }


                                if (normalizeDay(day)
                                        .equals(
                                                normalizedToday
                                        )) {

                                    todaySchedules.add(
                                            document
                                    );
                                }
                            }


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


                            displayTodaySchedule(
                                    todaySchedules
                            );


                            updateCurrentStatus(
                                    todaySchedules
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            scheduleLoadingText.setVisibility(
                                    View.VISIBLE
                            );

                            scheduleLoadingText.setText(
                                    getString(
                                            R.string.schedule_load_failed
                                    )
                            );

                            currentStatusText.setText(
                                    getString(
                                            R.string.schedule_unavailable
                                    )
                            );

                            currentLocationText.setText(
                                    getString(
                                            R.string.location_unavailable
                                    )
                            );

                            currentTimeText.setText(
                                    ""
                            );
                        }
                );
    }


    // ======================================================
    // DISPLAY TODAY'S SCHEDULE
    // ======================================================

    private void displayTodaySchedule(
            List<DocumentSnapshot> schedules
    ) {

        while (teacherScheduleContainer
                .getChildCount() > 1) {

            teacherScheduleContainer.removeViewAt(
                    1
            );
        }


        if (schedules == null
                || schedules.isEmpty()) {

            scheduleLoadingText.setVisibility(
                    View.VISIBLE
            );

            scheduleLoadingText.setText(
                    getString(
                            R.string.no_scheduled_classes_today
                    )
            );

            return;
        }


        scheduleLoadingText.setVisibility(
                View.GONE
        );


        for (DocumentSnapshot document :
                schedules) {

            addScheduleCard(
                    document
            );
        }
    }


    // ======================================================
    // ADD SCHEDULE CARD
    // ======================================================

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
                16,
                14,
                16,
                14
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
                8,
                0,
                8
        );

        card.setLayoutParams(
                cardParams
        );

        card.setElevation(
                2f
        );


        String subject =
                getStringValue(
                        document,
                        "subject"
                );


        String start =
                getStringValue(
                        document,
                        "startTime"
                );


        String end =
                getStringValue(
                        document,
                        "endTime"
                );


        String room =
                getStringValue(
                        document,
                        "room"
                );


        String type =
                getStringValue(
                        document,
                        "type"
                );


        String batch =
                cleanBatch(
                        getStringValue(
                                document,
                                "batch"
                        )
                );


        String section =
                getFirstSection(
                        document
                );


        TextView subjectText =
                createScheduleText(
                        subject,
                        17,
                        true
                );


        TextView timeText =
                createScheduleText(
                        start
                                + " - "
                                + end,
                        14,
                        false
                );


        TextView detailsText =
                createScheduleText(
                        buildDetailsText(
                                document,
                                section,
                                batch
                        ),
                        13,
                        false
                );


        TextView roomText =
                createScheduleText(
                        room.isEmpty()
                                ? getString(
                                R.string.room_not_available
                        )
                                : getString(
                                R.string.room_format,
                                room
                        ),
                        13,
                        false
                );


        TextView typeText =
                createScheduleText(
                        type,
                        12,
                        false
                );


        card.addView(
                subjectText
        );

        card.addView(
                timeText
        );

        card.addView(
                detailsText
        );

        card.addView(
                roomText
        );

        card.addView(
                typeText
        );


        teacherScheduleContainer.addView(
                card
        );
    }


    // ======================================================
    // CURRENT STATUS
    // ======================================================

    private void updateCurrentStatus(
            List<DocumentSnapshot> schedules
    ) {

        int currentMinutes =
                getCurrentMinutes();


        DocumentSnapshot current =
                null;

        DocumentSnapshot next =
                null;


        int nextStart =
                Integer.MAX_VALUE;


        for (DocumentSnapshot document :
                schedules) {

            int start =
                    convertTimeToMinutes(
                            document.getString(
                                    "startTime"
                            )
                    );

            int end =
                    convertTimeToMinutes(
                            document.getString(
                                    "endTime"
                            )
                    );


            if (start < 0 || end < 0) {
                continue;
            }


            if (currentMinutes >= start
                    && currentMinutes < end) {

                current =
                        document;

                break;
            }


            if (start > currentMinutes
                    && start < nextStart) {

                nextStart =
                        start;

                next =
                        document;
            }
        }


        // --------------------------------------------------
        // CURRENT CLASS
        // --------------------------------------------------

        if (current != null) {

            String subject =
                    getStringValue(
                            current,
                            "subject"
                    );

            String room =
                    getStringValue(
                            current,
                            "room"
                    );

            String end =
                    getStringValue(
                            current,
                            "endTime"
                    );


            currentStatusText.setText(
                    subject
            );


            currentLocationText.setText(
                    room.isEmpty()
                            ? getString(
                            R.string.room_not_available
                    )
                            : getString(
                            R.string.room_format,
                            room
                    )
            );


            currentTimeText.setText(
                    getString(
                            R.string.until_format,
                            formatDisplayTime(end)
                    )
            );

            return;
        }


        // --------------------------------------------------
        // NO CURRENT CLASS, NEXT CLASS EXISTS
        // --------------------------------------------------

        if (next != null) {

            String nextStartTime =
                    getStringValue(
                            next,
                            "startTime"
                    );


            currentStatusText.setText(
                    getString(
                            R.string.available
                    )
            );


            currentLocationText.setText(
                    getString(
                            R.string.no_current_class
                    )
            );


            currentTimeText.setText(
                    getString(
                            R.string.next_class_at,
                            formatDisplayTime(
                                    nextStartTime
                            )
                    )
            );

            return;
        }


        // --------------------------------------------------
        // NO MORE CLASSES
        // --------------------------------------------------

        currentStatusText.setText(
                getString(
                        R.string.no_more_classes_today
                )
        );


        currentLocationText.setText(
                getString(
                        R.string.no_scheduled_classes_today
                )
        );


        currentTimeText.setText(
                ""
        );
    }


    // ======================================================
    // BUILD DETAILS
    // ======================================================

    private String buildDetailsText(
            DocumentSnapshot document,
            String section,
            String batch
    ) {

        String program =
                getStringValue(
                        document,
                        "programs"
                );


        String semester =
                getValue(
                        document,
                        "semester",
                        ""
                );


        StringBuilder result =
                new StringBuilder();


        if (!program.isEmpty()) {

            result.append(
                    "Program: "
            );

            result.append(
                    program
            );
        }


        if (!semester.isEmpty()) {

            appendSeparator(
                    result
            );

            result.append(
                    "Sem "
            );

            result.append(
                    semester
            );
        }


        if (!section.isEmpty()) {

            appendSeparator(
                    result
            );

            result.append(
                    "Section: "
            );

            result.append(
                    section
            );
        }


        if (!batch.isEmpty()) {

            appendSeparator(
                    result
            );

            result.append(
                    "Batch: "
            );

            result.append(
                    batch
            );
        }


        return result.toString();
    }


    // ======================================================
    // FIRST SECTION
    // ======================================================

    private String getFirstSection(
            DocumentSnapshot document
    ) {

        Object value =
                document.get(
                        "sections"
                );


        if (!(value instanceof List)) {
            return "";
        }


        List<?> list =
                (List<?>) value;


        if (list.isEmpty()) {
            return "";
        }


        Object first =
                list.get(0);


        if (first == null) {
            return "";
        }


        return cleanSection(
                String.valueOf(first)
        );
    }


    // ======================================================
    // CLEAN SECTION
    // ======================================================

    private String cleanSection(
            String section
    ) {

        if (section == null) {
            return "";
        }


        section =
                section.trim()
                        .toUpperCase(
                                Locale.US
                        );


        String[] parts =
                section.split(
                        "\\s+"
                );


        if (parts.length > 0) {

            String last =
                    parts[
                            parts.length - 1
                            ];


            if (last.matches(
                    "[A-Z]+"
            )) {

                return last;
            }
        }


        return section;
    }


    // ======================================================
    // CLEAN BATCH
    // ======================================================

    private String cleanBatch(
            String batch
    ) {

        if (batch == null
                || batch.trim().isEmpty()) {

            return "";
        }


        String digits =
                batch
                        .trim()
                        .replaceAll(
                                "[^0-9]",
                                ""
                        );


        if (!digits.isEmpty()) {

            try {

                return String.valueOf(
                        Integer.parseInt(
                                digits
                        )
                );

            } catch (
                    NumberFormatException ignored
            ) {

                return digits;
            }
        }


        return batch.trim();
    }


    // ======================================================
    // GENERIC FIRESTORE VALUE
    // ======================================================

    private String getValue(
            DocumentSnapshot document,
            String field,
            String defaultValue
    ) {

        Object value =
                document.get(
                        field
                );


        if (value == null) {
            return defaultValue;
        }


        if (value instanceof List) {

            List<?> list =
                    (List<?>) value;


            List<String> values =
                    new ArrayList<>();


            for (Object item :
                    list) {

                if (item != null) {

                    values.add(
                            String.valueOf(
                                    item
                            )
                    );
                }
            }


            return joinList(
                    values
            );
        }


        return String.valueOf(
                value
        ).trim();
    }


    // ======================================================
    // STRING VALUE
    // ======================================================

    private String getStringValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(
                        field
                );


        if (value == null) {
            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }


    // ======================================================
    // JOIN LIST
    // ======================================================

    private String joinList(
            List<String> values
    ) {

        StringBuilder result =
                new StringBuilder();


        for (String value :
                values) {

            if (value == null
                    || value.trim().isEmpty()) {

                continue;
            }


            if (result.length() > 0) {

                result.append(
                        ", "
                );
            }


            result.append(
                    value.trim()
            );
        }


        return result.toString();
    }


    // ======================================================
    // SEPARATOR
    // ======================================================

    private void appendSeparator(
            StringBuilder builder
    ) {

        if (builder.length() > 0) {

            builder.append(
                    " • "
            );
        }
    }


    // ======================================================
    // CREATE SCHEDULE TEXT
    // ======================================================

    private TextView createScheduleText(
            String text,
            int size,
            boolean bold
    ) {

        TextView view =
                new TextView(
                        this
                );

        view.setText(
                text
        );

        view.setTextColor(
                0xFF555B65
        );

        view.setTextSize(
                size
        );


        if (bold) {

            view.setTextColor(
                    0xFF202124
            );

            view.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                5,
                0,
                0
        );

        view.setLayoutParams(
                params
        );


        return view;
    }


    // ======================================================
    // NORMALIZE DAY
    // ======================================================

    private String normalizeDay(
            String day
    ) {

        if (day == null) {
            return "";
        }


        String normalized =
                day.trim()
                        .toLowerCase(
                                Locale.US
                        );


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


    // ======================================================
    // CURRENT TIME
    // ======================================================

    private int getCurrentMinutes() {

        Calendar calendar =
                Calendar.getInstance();


        return calendar.get(
                Calendar.HOUR_OF_DAY
        ) * 60
                + calendar.get(
                Calendar.MINUTE
        );
    }


    // ======================================================
    // CONVERT TIME TO MINUTES
    // ======================================================

    private int convertTimeToMinutes(
            String time
    ) {

        if (time == null
                || time.trim().isEmpty()) {

            return -1;
        }


        try {

            String[] parts =
                    time.trim()
                            .split(
                                    ":"
                            );


            if (parts.length != 2) {
                return -1;
            }


            int hour =
                    Integer.parseInt(
                            parts[0]
                    );

            int minute =
                    Integer.parseInt(
                            parts[1]
                    );


            return hour * 60 + minute;

        } catch (
                NumberFormatException e
        ) {

            return -1;
        }
    }


    // ======================================================
    // DISPLAY TIME
    // ======================================================

    private String formatDisplayTime(
            String time
    ) {

        if (time == null
                || time.trim().isEmpty()) {

            return "";
        }


        try {

            SimpleDateFormat input =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.US
                    );

            SimpleDateFormat output =
                    new SimpleDateFormat(
                            "hh:mm a",
                            Locale.US
                    );


            return output.format(
                    input.parse(
                            time
                    )
            );

        } catch (Exception e) {

            return time;
        }
    }


    // ======================================================
    // TEACHER MODEL
    // ======================================================

    private static class TeacherInfo {

        String teacherCode;
        String name;
        String department;


        TeacherInfo(
                String teacherCode,
                String name,
                String department
        ) {

            this.teacherCode =
                    teacherCode;

            this.name =
                    name;

            this.department =
                    department;
        }


        @Override
        public String toString() {

            return name
                    + " ("
                    + teacherCode
                    + ")";
        }
    }
}