package com.example.madproject;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class upload_tt extends AppCompatActivity {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String teacherUid = "";
    private String teacherCode = "";
    private String teacherName = "";


    // =========================================================
    // UI
    // =========================================================

    private TextView teacherNameText;
    private TextView teacherCodeText;
    private TextView currentDayText;
    private TextView timetableStatus;

    private TableLayout timetableTable;

    private ProgressBar timetableProgress;

    private LinearLayout slotEditor;

    private EditText editorSubject;
    private EditText editorRoom;

    private Spinner editorDaySpinner;
    private Spinner editorStartTimeSpinner;
    private Spinner editorEndTimeSpinner;
    private Spinner editorTypeSpinner;
    private Spinner editorProgramSpinner;
    private Spinner editorSemesterSpinner;
    private Spinner editorSectionSpinner;
    private Spinner editorBatchSpinner;

    private Button dayMonButton;
    private Button dayTueButton;
    private Button dayWedButton;
    private Button dayThuButton;
    private Button dayFriButton;

    private Button cancelEditButton;
    private Button deleteSlotButton;
    private Button saveSlotButton;


    // =========================================================
    // DATA
    // =========================================================

    private final List<String> days = Arrays.asList(
            "Mon",
            "Tue",
            "Wed",
            "Thu",
            "Fri"
    );

    private final List<String> dayDisplayNames = Arrays.asList(
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday"
    );


    /*
     * Official institutional class slots.
     *
     * Breaks are intentionally NOT included.
     */

    private final String[][] officialSlots = {

            {"09:00", "10:00"},
            {"10:00", "11:00"},
            {"11:10", "12:10"},
            {"12:10", "13:10"},
            {"14:10", "15:10"},
            {"15:10", "16:10"}

    };


    private String selectedDay = "Mon";

    private DocumentSnapshot editingMasterSchedule = null;

    private String editingOverrideId = "";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_upload_tt
        );


        View root =
                findViewById(R.id.main);

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
                        R.id.teacherNameText
                );

        teacherCodeText =
                findViewById(
                        R.id.teacherCodeText
                );

        currentDayText =
                findViewById(
                        R.id.currentDayText
                );

        timetableTable =
                findViewById(
                        R.id.timetableTable
                );

        timetableProgress =
                findViewById(
                        R.id.timetableProgress
                );

        timetableStatus =
                findViewById(
                        R.id.timetableStatus
                );

        slotEditor =
                findViewById(
                        R.id.slotEditor
                );


        editorSubject =
                findViewById(
                        R.id.editorSubject
                );

        editorRoom =
                findViewById(
                        R.id.editorRoom
                );


        editorDaySpinner =
                findViewById(
                        R.id.editorDaySpinner
                );

        editorStartTimeSpinner =
                findViewById(
                        R.id.editorStartTimeSpinner
                );

        editorEndTimeSpinner =
                findViewById(
                        R.id.editorEndTimeSpinner
                );

        editorTypeSpinner =
                findViewById(
                        R.id.editorTypeSpinner
                );

        editorProgramSpinner =
                findViewById(
                        R.id.editorProgramSpinner
                );

        editorSemesterSpinner =
                findViewById(
                        R.id.editorSemesterSpinner
                );

        editorSectionSpinner =
                findViewById(
                        R.id.editorSectionSpinner
                );

        editorBatchSpinner =
                findViewById(
                        R.id.editorBatchSpinner
                );


        dayMonButton =
                findViewById(
                        R.id.dayMonButton
                );

        dayTueButton =
                findViewById(
                        R.id.dayTueButton
                );

        dayWedButton =
                findViewById(
                        R.id.dayWedButton
                );

        dayThuButton =
                findViewById(
                        R.id.dayThuButton
                );

        dayFriButton =
                findViewById(
                        R.id.dayFriButton
                );


        cancelEditButton =
                findViewById(
                        R.id.cancelEditButton
                );

        deleteSlotButton =
                findViewById(
                        R.id.deleteSlotButton
                );

        saveSlotButton =
                findViewById(
                        R.id.saveSlotButton
                );


        // =====================================================
        // INITIALIZE SPINNERS
        // =====================================================

        setupSpinners();


        // =====================================================
        // DAY BUTTONS
        // =====================================================

        dayMonButton.setOnClickListener(
                v -> selectDay("Mon")
        );

        dayTueButton.setOnClickListener(
                v -> selectDay("Tue")
        );

        dayWedButton.setOnClickListener(
                v -> selectDay("Wed")
        );

        dayThuButton.setOnClickListener(
                v -> selectDay("Thu")
        );

        dayFriButton.setOnClickListener(
                v -> selectDay("Fri")
        );


        // =====================================================
        // EDITOR BUTTONS
        // =====================================================

        cancelEditButton.setOnClickListener(
                v -> hideEditor()
        );


        saveSlotButton.setOnClickListener(
                v -> saveSlot()
        );


        deleteSlotButton.setOnClickListener(
                v -> deleteSlot()
        );


        // =====================================================
        // LOAD PROFILE
        // =====================================================

        loadTeacherProfile();
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

                                timetableStatus.setText(
                                        getString(
                                                R.string.profile_load_failed
                                        )
                                );

                                return;
                            }


                            String name =
                                    document.getString(
                                            "name"
                                    );

                            String code =
                                    document.getString(
                                            "teacherCode"
                                    );


                            teacherName =
                                    name == null
                                            ? ""
                                            : name.trim();


                            teacherCode =
                                    code == null
                                            ? ""
                                            : code.trim()
                                            .toUpperCase();


                            if (!teacherName.isEmpty()) {

                                teacherNameText.setText(
                                        teacherName
                                );
                            }


                            if (!teacherCode.isEmpty()) {

                                teacherCodeText.setText(
                                        "Teacher Code: " +
                                                teacherCode
                                );

                                loadTimetable();

                            } else {

                                teacherCodeText.setText(
                                        getString(
                                                R.string.teacher_code_not_found
                                        )
                                );

                                timetableStatus.setText(
                                        getString(
                                                R.string.teacher_code_not_found
                                        )
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            timetableStatus.setText(
                                    getString(
                                            R.string.profile_load_failed
                                    )
                            );

                            Toast.makeText(
                                    this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // LOAD TIMETABLE
    // =========================================================

    private void loadTimetable() {

        timetableProgress.setVisibility(
                View.VISIBLE
        );

        timetableStatus.setText(
                getString(
                        R.string.loading
                )
        );


        db.collection("masterSchedules")
                .whereArrayContains(
                        "teacherCodes",
                        teacherCode
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            timetableProgress.setVisibility(
                                    View.GONE
                            );

                            timetableStatus.setText(
                                    getString(
                                            R.string.timetable_loaded
                                    )
                            );

                            selectDay(
                                    selectedDay
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            timetableProgress.setVisibility(
                                    View.GONE
                            );

                            timetableStatus.setText(
                                    getString(
                                            R.string.timetable_load_failed
                                    )
                            );

                            Toast.makeText(
                                    this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // SELECT DAY
    // =========================================================

    private void selectDay(
            String day
    ) {

        selectedDay =
                day;


        currentDayText.setText(
                getDayDisplayName(day)
        );


        highlightSelectedDay(
                day
        );


        loadDaySchedule(
                day
        );
    }


    // =========================================================
    // LOAD DAY SCHEDULE
    // =========================================================

    private void loadDaySchedule(
            String day
    ) {

        timetableTable.removeAllViews();


        timetableTable.addView(
                createTableHeader()
        );


        db.collection("masterSchedules")
                .whereArrayContains(
                        "teacherCodes",
                        teacherCode
                )
                .whereEqualTo(
                        "day",
                        day
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            List<DocumentSnapshot> documents =
                                    new ArrayList<>(
                                            snapshot.getDocuments()
                                    );


                            Collections.sort(
                                    documents,
                                    Comparator.comparingInt(
                                            document ->
                                                    timeToMinutes(
                                                            document.getString(
                                                                    "startTime"
                                                            )
                                                    )
                                    )
                            );


                            for (
                                    String[] slot :
                                    officialSlots
                            ) {

                                addSlotRow(
                                        day,
                                        slot[0],
                                        slot[1],
                                        documents
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            timetableStatus.setText(
                                    getString(
                                            R.string.timetable_load_failed
                                    )
                            );
                        }
                );
    }


    // =========================================================
    // TABLE HEADER
    // =========================================================

    private TableRow createTableHeader() {

        TableRow row =
                new TableRow(this);

        row.setBackgroundColor(
                Color.parseColor(
                        "#E5E7EB"
                )
        );


        row.addView(
                createCell(
                        "Time",
                        true
                )
        );


        row.addView(
                createCell(
                        "Class",
                        true
                )
        );


        return row;
    }


    // =========================================================
    // ADD SLOT ROW
    // =========================================================

    private void addSlotRow(
            String day,
            String startTime,
            String endTime,
            List<DocumentSnapshot> documents
    ) {

        TableRow row =
                new TableRow(this);


        row.setPadding(
                0,
                1,
                0,
                1
        );


        TextView timeCell =
                createCell(
                        startTime +
                                " - " +
                                endTime,
                        false
                );


        row.addView(
                timeCell
        );


        DocumentSnapshot matching =
                findMatchingSchedule(
                        documents,
                        startTime,
                        endTime
                );


        TextView classCell;


        if (matching != null) {

            classCell =
                    createClassCell(
                            matching
                    );

        } else {

            classCell =
                    createCell(
                            getString(
                                    R.string.no_schedule
                            ),
                            false
                    );

            classCell.setTextColor(
                    Color.parseColor(
                            "#6B7280"
                    )
            );
        }


        row.addView(
                classCell
        );


        final DocumentSnapshot selected =
                matching;


        row.setOnClickListener(
                v -> {

                    if (selected != null) {

                        openEditor(
                                selected
                        );

                    } else {

                        openNewSlotEditor(
                                day,
                                startTime,
                                endTime
                        );
                    }
                }
        );


        timetableTable.addView(
                row
        );


        // -----------------------------------------------------
        // BREAK ROWS
        // -----------------------------------------------------

        if (
                endTime.equals("11:00")
        ) {

            addBreakRow(
                    getString(
                            R.string.short_break
                    ),
                    "11:00",
                    "11:10"
            );
        }


        if (
                endTime.equals("13:10")
        ) {

            addBreakRow(
                    getString(
                            R.string.lunch_break
                    ),
                    "13:10",
                    "14:10"
            );
        }
    }


    // =========================================================
    // BREAK ROW
    // =========================================================

    private void addBreakRow(
            String title,
            String start,
            String end
    ) {

        TableRow row =
                new TableRow(this);


        row.setBackgroundColor(
                Color.parseColor(
                        "#F3F4F6"
                )
        );


        TextView time =
                createCell(
                        start + " - " + end,
                        false
                );


        TextView breakText =
                createCell(
                        title,
                        false
                );


        breakText.setGravity(
                Gravity.CENTER
        );


        breakText.setTypeface(
                null,
                Typeface.BOLD
        );


        breakText.setTextColor(
                Color.parseColor(
                        "#6B7280"
                )
        );


        row.addView(
                time
        );

        row.addView(
                breakText
        );


        timetableTable.addView(
                row
        );
    }


    // =========================================================
    // FIND MATCHING SCHEDULE
    // =========================================================

    private DocumentSnapshot findMatchingSchedule(
            List<DocumentSnapshot> documents,
            String start,
            String end
    ) {

        for (
                DocumentSnapshot document :
                documents
        ) {

            String documentStart =
                    document.getString(
                            "startTime"
                    );

            String documentEnd =
                    document.getString(
                            "endTime"
                    );


            if (
                    normalizeTime(documentStart)
                            .equals(
                                    normalizeTime(start)
                            )
                            &&
                            normalizeTime(documentEnd)
                                    .equals(
                                            normalizeTime(end)
                                    )
            ) {

                return document;
            }
        }


        return null;
    }


    // =========================================================
    // CLASS CELL
    // =========================================================

    private TextView createClassCell(
            DocumentSnapshot document
    ) {

        String subject =
                safeString(
                        document,
                        "subject",
                        "Class"
                );

        String type =
                safeString(
                        document,
                        "type",
                        ""
                );

        String program =
                safeString(
                        document,
                        "program",
                        ""
                );

        String section =
                safeString(
                        document,
                        "section",
                        ""
                );

        String room =
                safeString(
                        document,
                        "room",
                        ""
                );


        StringBuilder text =
                new StringBuilder();


        text.append(
                subject
        );


        if (!type.isEmpty()) {

            text.append(
                    "\n"
            );

            text.append(
                    type
            );
        }


        if (!program.isEmpty()) {

            text.append(
                    " • "
            );

            text.append(
                    program
            );
        }


        if (!section.isEmpty()) {

            text.append(
                    " • Sec "
            );

            text.append(
                    section
            );
        }


        if (!room.isEmpty()) {

            text.append(
                    "\n"
            );

            text.append(
                    room
            );
        }


        TextView cell =
                createCell(
                        text.toString(),
                        false
                );


        cell.setTextColor(
                Color.parseColor(
                        "#111827"
                )
        );


        cell.setBackgroundColor(
                Color.parseColor(
                        "#FFFFFF"
                )
        );


        return cell;
    }


    // =========================================================
    // CREATE TABLE CELL
    // =========================================================

    private TextView createCell(
            String text,
            boolean header
    ) {

        TextView cell =
                new TextView(this);


        cell.setText(
                text
        );


        cell.setTextSize(
                header
                        ? 14
                        : 13
        );


        cell.setTextColor(
                Color.parseColor(
                        "#111827"
                )
        );


        cell.setGravity(
                Gravity.CENTER_VERTICAL
                        |
                        Gravity.START
        );


        cell.setPadding(
                18,
                16,
                18,
                16
        );


        cell.setMinWidth(
                header
                        ? 150
                        : 150
        );


        if (header) {

            cell.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }


        TableRow.LayoutParams params =
                new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );


        params.setMargins(
                1,
                1,
                1,
                1
        );


        cell.setLayoutParams(
                params
        );


        return cell;
    }


    // =========================================================
    // SETUP SPINNERS
    // =========================================================

    private void setupSpinners() {

        setSpinner(
                editorDaySpinner,
                dayDisplayNames
        );


        List<String> starts =
                new ArrayList<>();

        List<String> ends =
                new ArrayList<>();


        for (
                String[] slot :
                officialSlots
        ) {

            starts.add(
                    slot[0]
            );

            ends.add(
                    slot[1]
            );
        }


        setSpinner(
                editorStartTimeSpinner,
                starts
        );

        setSpinner(
                editorEndTimeSpinner,
                ends
        );


        setSpinner(
                editorTypeSpinner,
                Arrays.asList(
                        "THEORY",
                        "LAB"
                )
        );


        setSpinner(
                editorProgramSpinner,
                Arrays.asList(
                        "CE",
                        "AIDS",
                        "MT",
                        "MBA",
                        "CA"
                )
        );


        List<String> semesters =
                new ArrayList<>();

        semesters.add(
                "None"
        );

        for (
                int i = 1;
                i <= 10;
                i++
        ) {

            semesters.add(
                    String.valueOf(i)
            );
        }


        setSpinner(
                editorSemesterSpinner,
                semesters
        );


        setSpinner(
                editorSectionSpinner,
                Arrays.asList(
                        "None",
                        "A",
                        "B",
                        "AB"
                )
        );


        setSpinner(
                editorBatchSpinner,
                Arrays.asList(
                        "None",
                        "A1",
                        "A2",
                        "B1",
                        "B2",
                        "C1",
                        "C2"
                )
        );
    }


    // =========================================================
    // SPINNER HELPER
    // =========================================================

    private void setSpinner(
            Spinner spinner,
            List<String> values
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        values
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spinner.setAdapter(
                adapter
        );
    }


    // =========================================================
    // OPEN EXISTING EDITOR
    // =========================================================

    private void openEditor(
            DocumentSnapshot document
    ) {

        editingMasterSchedule =
                document;

        editingOverrideId =
                "";


        String day =
                safeString(
                        document,
                        "day",
                        selectedDay
                );


        String start =
                safeString(
                        document,
                        "startTime",
                        "09:00"
                );


        String end =
                safeString(
                        document,
                        "endTime",
                        "10:00"
                );


        String subject =
                safeString(
                        document,
                        "subject",
                        ""
                );


        String type =
                safeString(
                        document,
                        "type",
                        "THEORY"
                );


        String program =
                safeString(
                        document,
                        "program",
                        "CE"
                );


        String semester =
                getSemesterString(
                        document
                );


        String section =
                safeString(
                        document,
                        "section",
                        "None"
                );


        String batch =
                safeString(
                        document,
                        "batch",
                        "None"
                );


        String room =
                safeString(
                        document,
                        "room",
                        ""
                );


        setSpinnerValue(
                editorDaySpinner,
                getDayDisplayName(day)
        );

        setSpinnerValue(
                editorStartTimeSpinner,
                start
        );

        setSpinnerValue(
                editorEndTimeSpinner,
                end
        );

        setSpinnerValue(
                editorTypeSpinner,
                type
        );

        setSpinnerValue(
                editorProgramSpinner,
                program
        );

        setSpinnerValue(
                editorSemesterSpinner,
                semester
        );

        setSpinnerValue(
                editorSectionSpinner,
                section
        );

        setSpinnerValue(
                editorBatchSpinner,
                batch
        );


        editorSubject.setText(
                subject
        );

        editorRoom.setText(
                room
        );


        deleteSlotButton.setVisibility(
                View.VISIBLE
        );


        slotEditor.setVisibility(
                View.VISIBLE
        );
    }


    // =========================================================
    // OPEN NEW SLOT
    // =========================================================

    private void openNewSlotEditor(
            String day,
            String start,
            String end
    ) {

        editingMasterSchedule =
                null;

        editingOverrideId =
                "";


        setSpinnerValue(
                editorDaySpinner,
                getDayDisplayName(day)
        );

        setSpinnerValue(
                editorStartTimeSpinner,
                start
        );

        setSpinnerValue(
                editorEndTimeSpinner,
                end
        );


        editorSubject.setText(
                ""
        );

        editorRoom.setText(
                ""
        );


        setSpinnerValue(
                editorTypeSpinner,
                "THEORY"
        );

        setSpinnerValue(
                editorProgramSpinner,
                "CE"
        );

        setSpinnerValue(
                editorSemesterSpinner,
                "None"
        );

        setSpinnerValue(
                editorSectionSpinner,
                "None"
        );

        setSpinnerValue(
                editorBatchSpinner,
                "None"
        );


        deleteSlotButton.setVisibility(
                View.GONE
        );


        slotEditor.setVisibility(
                View.VISIBLE
        );
    }


    // =========================================================
    // SAVE SLOT
    // =========================================================

    private void saveSlot() {

        if (
                teacherCode == null
                        ||
                        teacherCode.isEmpty()
        ) {

            return;
        }


        String day =
                spinnerDayToCode(
                        editorDaySpinner
                                .getSelectedItem()
                                .toString()
                );


        String start =
                editorStartTimeSpinner
                        .getSelectedItem()
                        .toString();


        String end =
                editorEndTimeSpinner
                        .getSelectedItem()
                        .toString();


        String subject =
                editorSubject
                        .getText()
                        .toString()
                        .trim();


        String type =
                editorTypeSpinner
                        .getSelectedItem()
                        .toString();


        String program =
                editorProgramSpinner
                        .getSelectedItem()
                        .toString();


        String semesterText =
                editorSemesterSpinner
                        .getSelectedItem()
                        .toString();


        String section =
                editorSectionSpinner
                        .getSelectedItem()
                        .toString();


        String batch =
                editorBatchSpinner
                        .getSelectedItem()
                        .toString();


        String room =
                editorRoom
                        .getText()
                        .toString()
                        .trim();


        if (
                subject.isEmpty()
        ) {

            editorSubject.setError(
                    getString(
                            R.string.enter_subject
                    )
            );

            return;
        }


        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "teacherCode",
                teacherCode
        );

        data.put(
                "day",
                day
        );

        data.put(
                "startTime",
                start
        );

        data.put(
                "endTime",
                end
        );

        data.put(
                "action",
                "UPDATE"
        );

        data.put(
                "subject",
                subject
        );

        data.put(
                "type",
                type
        );

        data.put(
                "program",
                program
        );

        data.put(
                "section",
                section.equals("None")
                        ? ""
                        : section
        );

        data.put(
                "batch",
                batch.equals("None")
                        ? ""
                        : batch
        );

        data.put(
                "room",
                room
        );

        data.put(
                "updatedAt",
                Timestamp.now()
        );

        data.put(
                "updatedBy",
                teacherUid
        );


        if (
                !semesterText.equals("None")
        ) {

            try {

                data.put(
                        "semester",
                        Integer.parseInt(
                                semesterText
                        )
                );

            } catch (Exception ignored) {

                data.put(
                        "semester",
                        null
                );
            }

        } else {

            data.put(
                    "semester",
                    null
            );
        }


        /*
         * If this is an existing master timetable class,
         * preserve its schedule ID so we know which
         * master record this override belongs to.
         */

        if (
                editingMasterSchedule != null
        ) {

            data.put(
                    "masterScheduleId",
                    editingMasterSchedule.getId()
            );
        }


        String documentId;


        if (
                !editingOverrideId.isEmpty()
        ) {

            documentId =
                    editingOverrideId;

        } else {

            documentId =
                    day +
                            "_" +
                            start.replace(
                                    ":",
                                    ""
                            );
        }


        db.collection(
                        "teacherScheduleOverrides"
                )
                .document(
                        teacherCode
                )
                .collection(
                        "entries"
                )
                .document(
                        documentId
                )
                .set(
                        data
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    getString(
                                            R.string.slot_saved
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();


                            hideEditor();

                            loadDaySchedule(
                                    selectedDay
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    getString(
                                            R.string.save_failed
                                    ),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // DELETE SLOT
    // =========================================================

    private void deleteSlot() {

        if (
                editingMasterSchedule == null
        ) {

            return;
        }


        String day =
                safeString(
                        editingMasterSchedule,
                        "day",
                        selectedDay
                );


        String start =
                safeString(
                        editingMasterSchedule,
                        "startTime",
                        "09:00"
                );


        String overrideId =
                day +
                        "_" +
                        start.replace(
                                ":",
                                ""
                        );


        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "teacherCode",
                teacherCode
        );

        data.put(
                "day",
                day
        );

        data.put(
                "startTime",
                start
        );

        data.put(
                "action",
                "DELETE"
        );

        data.put(
                "masterScheduleId",
                editingMasterSchedule.getId()
        );

        data.put(
                "updatedAt",
                Timestamp.now()
        );

        data.put(
                "updatedBy",
                teacherUid
        );


        db.collection(
                        "teacherScheduleOverrides"
                )
                .document(
                        teacherCode
                )
                .collection(
                        "entries"
                )
                .document(
                        overrideId
                )
                .set(
                        data
                )
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    getString(
                                            R.string.slot_deleted
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();


                            hideEditor();

                            loadDaySchedule(
                                    selectedDay
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    getString(
                                            R.string.delete_failed
                                    ),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // HIDE EDITOR
    // =========================================================

    private void hideEditor() {

        slotEditor.setVisibility(
                View.GONE
        );

        editingMasterSchedule =
                null;

        editingOverrideId =
                "";
    }


    // =========================================================
    // HIGHLIGHT DAY
    // =========================================================

    private void highlightSelectedDay(
            String day
    ) {

        Button[] buttons = {
                dayMonButton,
                dayTueButton,
                dayWedButton,
                dayThuButton,
                dayFriButton
        };


        for (
                Button button :
                buttons
        ) {

            button.setAlpha(
                    0.55f
            );
        }


        switch (day) {

            case "Mon":
                dayMonButton.setAlpha(1f);
                break;

            case "Tue":
                dayTueButton.setAlpha(1f);
                break;

            case "Wed":
                dayWedButton.setAlpha(1f);
                break;

            case "Thu":
                dayThuButton.setAlpha(1f);
                break;

            case "Fri":
                dayFriButton.setAlpha(1f);
                break;
        }
    }


    // =========================================================
    // SPINNER VALUE
    // =========================================================

    private void setSpinnerValue(
            Spinner spinner,
            String value
    ) {

        ArrayAdapter adapter =
                (ArrayAdapter)
                        spinner.getAdapter();


        if (adapter == null) {

            return;
        }


        int position =
                adapter.getPosition(
                        value
                );


        if (position >= 0) {

            spinner.setSelection(
                    position
            );
        }
    }


    // =========================================================
    // SAFE FIRESTORE STRING
    // =========================================================

    private String safeString(
            DocumentSnapshot document,
            String field,
            String fallback
    ) {

        Object value =
                document.get(field);


        if (value == null) {

            return fallback;
        }


        String result =
                String.valueOf(
                        value
                ).trim();


        return result.isEmpty()
                ? fallback
                : result;
    }


    // =========================================================
    // SEMESTER
    // =========================================================

    private String getSemesterString(
            DocumentSnapshot document
    ) {

        Object value =
                document.get(
                        "semester"
                );


        if (value == null) {

            return "None";
        }


        if (
                value instanceof Number
        ) {

            return String.valueOf(
                    ((Number) value).intValue()
            );
        }


        String text =
                String.valueOf(
                        value
                ).trim();


        return text.isEmpty()
                ? "None"
                : text;
    }


    // =========================================================
    // NORMALIZE TIME
    // =========================================================

    private String normalizeTime(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value
                .trim()
                .replace(
                        " ",
                        ""
                )
                .toLowerCase();
    }


    // =========================================================
    // TIME → MINUTES
    // =========================================================

    private int timeToMinutes(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return Integer.MAX_VALUE;
        }


        try {

            String[] parts =
                    value
                            .trim()
                            .split(":");


            int hour =
                    Integer.parseInt(
                            parts[0]
                    );


            int minute =
                    Integer.parseInt(
                            parts[1]
                    );


            return hour * 60 + minute;

        } catch (Exception e) {

            return Integer.MAX_VALUE;
        }
    }


    // =========================================================
    // DAY DISPLAY
    // =========================================================

    private String getDayDisplayName(
            String day
    ) {

        switch (day) {

            case "Mon":
                return "Monday";

            case "Tue":
                return "Tuesday";

            case "Wed":
                return "Wednesday";

            case "Thu":
                return "Thursday";

            case "Fri":
                return "Friday";

            default:
                return day;
        }
    }


    // =========================================================
    // DISPLAY DAY → CODE
    // =========================================================

    private String spinnerDayToCode(
            String value
    ) {

        switch (value) {

            case "Monday":
                return "Mon";

            case "Tuesday":
                return "Tue";

            case "Wednesday":
                return "Wed";

            case "Thursday":
                return "Thu";

            case "Friday":
                return "Fri";

            default:
                return value;
        }
    }
}