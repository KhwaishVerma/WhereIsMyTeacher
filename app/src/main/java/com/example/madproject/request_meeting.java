package com.example.madproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class request_meeting extends AppCompatActivity {

    // ============================================================
    // VIEWS
    // ============================================================

    private TextView facultyNameTextView;
    private TextView facultyDepartmentTextView;
    private TextView slotStatusTextView;

    private Button dateTextView;
    private Button buttonMeetingTime;
    private Button buttonSendRequest;

    private EditText messageEditText;


    // ============================================================
    // FIREBASE
    // ============================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // ============================================================
    // USER / TEACHER DATA
    // ============================================================

    // Student UID is still required.
    private String studentUid;

    /*
     * IMPORTANT:
     *
     * There is NO teacherUid.
     *
     * Teacher is identified exclusively by teacherCode.
     */
    private String teacherCode;
    private String teacherName;
    private String teacherDepartment = "";


    // ============================================================
    // SELECTED DATE / SLOT
    // ============================================================

    private String selectedDate = "";
    private String selectedDay = "";

    private String selectedStartTime = "";
    private String selectedEndTime = "";

    private boolean loadingSlots = false;


    // ============================================================
    // OFFICIAL WIMT CLASS SLOTS
    // ============================================================

    private final String[] SLOT_STARTS = {
            "09:00",
            "10:00",
            "11:10",
            "12:10",
            "14:10",
            "15:10"
    };

    private final String[] SLOT_ENDS = {
            "10:00",
            "11:00",
            "12:10",
            "13:10",
            "15:10",
            "16:10"
    };

    private final String[] SLOT_LABELS = {
            "09:00 - 10:00",
            "10:00 - 11:00",
            "11:10 - 12:10",
            "12:10 - 13:10",
            "14:10 - 15:10",
            "15:10 - 16:10"
    };


    // ============================================================
    // AVAILABLE SLOT INDEXES
    // ============================================================

    private final List<Integer> availableSlotIndexes =
            new ArrayList<>();


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_request_meeting
        );


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // ========================================================
        // FIND VIEWS
        // ========================================================

        facultyNameTextView =
                findViewById(
                        R.id.facultyNameTextView
                );

        facultyDepartmentTextView =
                findViewById(
                        R.id.facultyDepartmentTextView
                );

        dateTextView =
                findViewById(
                        R.id.dateTextView
                );

        buttonMeetingTime =
                findViewById(
                        R.id.buttonMeetingTime
                );

        buttonSendRequest =
                findViewById(
                        R.id.button9
                );

        messageEditText =
                findViewById(
                        R.id.editTextText18
                );

        slotStatusTextView =
                findViewById(
                        R.id.slotStatusTextView
                );


        // ========================================================
        // EDGE TO EDGE
        // ========================================================

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


        // ========================================================
        // CHECK STUDENT LOGIN
        // ========================================================

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        studentUid =
                auth.getCurrentUser().getUid();


        // ========================================================
        // GET TEACHER INFORMATION
        // ========================================================

        teacherCode =
                getIntent().getStringExtra(
                        "teacherCode"
                );

        teacherName =
                getIntent().getStringExtra(
                        "teacherName"
                );


        /*
         * Teacher Code is the ONLY teacher identifier.
         */
        if (TextUtils.isEmpty(teacherCode)) {

            Toast.makeText(
                    this,
                    "Teacher information is missing.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        // ========================================================
        // INITIAL TEACHER NAME
        // ========================================================

        if (!TextUtils.isEmpty(teacherName)) {

            facultyNameTextView.setText(
                    teacherName
            );
        }


        // ========================================================
        // LOAD TEACHER PROFILE FROM teacherDirectory
        // ========================================================

        loadTeacherProfile();


        // ========================================================
        // DATE PICKER
        // ========================================================

        dateTextView.setOnClickListener(
                v -> showDatePicker()
        );


        // ========================================================
        // SLOT SELECTOR
        // ========================================================

        buttonMeetingTime.setOnClickListener(
                v -> showAvailableSlotDialog()
        );


        // ========================================================
        // SEND REQUEST
        // ========================================================

        buttonSendRequest.setOnClickListener(
                v -> submitMeetingRequest()
        );
    }


    // ============================================================
    // LOAD TEACHER PROFILE
    // ============================================================

    private void loadTeacherProfile() {

        /*
         * IMPORTANT:
         *
         * We now read teacher information from:
         *
         * teacherDirectory
         *
         * using teacherCode.
         *
         * No teacher UID.
         */
        db.collection("teacherDirectory")
                .whereEqualTo(
                        "teacherCode",
                        teacherCode
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            if (querySnapshot.isEmpty()) {

                                facultyNameTextView.setText(
                                        TextUtils.isEmpty(teacherName)
                                                ? "Faculty"
                                                : teacherName
                                );

                                facultyDepartmentTextView.setText(
                                        "Department not available"
                                );

                                return;
                            }


                            DocumentSnapshot document =
                                    querySnapshot
                                            .getDocuments()
                                            .get(0);


                            // ------------------------------------------------
                            // NAME
                            // ------------------------------------------------

                            String name =
                                    document.getString(
                                            "name"
                                    );


                            if (!TextUtils.isEmpty(name)) {

                                teacherName =
                                        name;

                                facultyNameTextView.setText(
                                        name
                                );
                            }


                            // ------------------------------------------------
                            // DEPARTMENT
                            // ------------------------------------------------

                            String department =
                                    document.getString(
                                            "department"
                                    );


                            if (!TextUtils.isEmpty(department)) {

                                teacherDepartment =
                                        department;

                                facultyDepartmentTextView.setText(
                                        department
                                );

                            } else {

                                facultyDepartmentTextView.setText(
                                        "Department not available"
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            /*
                             * Do NOT say "Could not load teacher profile"
                             * because teacher profile is no longer loaded
                             * through users/{teacherUid}.
                             */

                            facultyDepartmentTextView.setText(
                                    "Department not available"
                            );

                            Toast.makeText(
                                    request_meeting.this,
                                    "Could not load teacher directory.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // ============================================================
    // DATE PICKER
    // ============================================================

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();


        int year =
                calendar.get(
                        Calendar.YEAR
                );

        int month =
                calendar.get(
                        Calendar.MONTH
                );

        int day =
                calendar.get(
                        Calendar.DAY_OF_MONTH
                );


        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,

                        (
                                view,
                                selectedYear,
                                selectedMonth,
                                selectedDayOfMonth
                        ) -> {

                            Calendar selectedCalendar =
                                    Calendar.getInstance();


                            selectedCalendar.set(
                                    selectedYear,
                                    selectedMonth,
                                    selectedDayOfMonth
                            );


                            // ================================================
                            // FIRESTORE DATE
                            // ================================================

                            SimpleDateFormat databaseFormat =
                                    new SimpleDateFormat(
                                            "yyyy-MM-dd",
                                            Locale.getDefault()
                                    );


                            selectedDate =
                                    databaseFormat.format(
                                            selectedCalendar.getTime()
                                    );


                            // ================================================
                            // DISPLAY DATE
                            // ================================================

                            SimpleDateFormat displayFormat =
                                    new SimpleDateFormat(
                                            "dd MMMM yyyy",
                                            Locale.getDefault()
                                    );


                            dateTextView.setText(
                                    displayFormat.format(
                                            selectedCalendar.getTime()
                                    )
                            );


                            // ================================================
                            // DAY
                            // ================================================

                            selectedDay = getFirestoreDayName(selectedCalendar);


                            // ================================================
                            // RESET SLOT
                            // ================================================

                            selectedStartTime = "";
                            selectedEndTime = "";


                            buttonMeetingTime.setText(
                                    "Select Meeting Slot"
                            );


                            buttonMeetingTime.setEnabled(
                                    false
                            );


                            // ================================================
                            // LOAD AVAILABILITY
                            // ================================================

                            loadAvailableSlots();
                        },

                        year,
                        month,
                        day
                );


        /*
         * Do not allow past dates.
         */
        dialog.getDatePicker()
                .setMinDate(
                        System.currentTimeMillis()
                );


        dialog.show();
    }


    // ============================================================
    // FIRESTORE DAY NAME
    // ============================================================

    private String getFirestoreDayName(
            Calendar calendar
    ) {

        int day =
                calendar.get(
                        Calendar.DAY_OF_WEEK
                );


        switch (day) {

            case Calendar.MONDAY:
                return "Mon";

            case Calendar.TUESDAY:
                return "Tues";

            case Calendar.WEDNESDAY:
                return "Wed";

            case Calendar.THURSDAY:
                return "Thurs";

            case Calendar.FRIDAY:
                return "Fri";

            case Calendar.SATURDAY:
                return "Sat";

            case Calendar.SUNDAY:
                return "Sun";

            default:
                return "";
        }
    }


    // ============================================================
    // LOAD AVAILABLE SLOTS
    // ============================================================

    private void loadAvailableSlots() {

        if (TextUtils.isEmpty(selectedDay)) {
            return;
        }


        loadingSlots = true;

        availableSlotIndexes.clear();


        slotStatusTextView.setText(
                "Checking faculty availability..."
        );


        buttonMeetingTime.setEnabled(
                false
        );


        /*
         * ========================================================
         * MASTER TIMETABLE
         * ========================================================
         *
         * Teacher identified by teacherCode.
         */

        db.collection("masterSchedules")
                .whereArrayContains(
                        "teacherCodes",
                        teacherCode
                )
                .whereEqualTo(
                        "day",
                        selectedDay
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            Set<String> occupiedSlots =
                                    new HashSet<>();


                            // ================================================
                            // MASTER SCHEDULE OCCUPANCY
                            // ================================================

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                String startTime =
                                        document.getString(
                                                "startTime"
                                        );

                                String endTime =
                                        document.getString(
                                                "endTime"
                                        );


                                markOverlappingSlots(
                                        startTime,
                                        endTime,
                                        occupiedSlots
                                );
                            }


                            // ================================================
                            // TEACHER OVERRIDES
                            // ================================================

                            loadOverridesForSelectedDay(
                                    occupiedSlots
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            loadingSlots = false;


                            slotStatusTextView.setText(
                                    "Could not load faculty availability."
                            );


                            Toast.makeText(
                                    request_meeting.this,
                                    "Failed to load timetable.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // ============================================================
    // LOAD TEACHER OVERRIDES
    // ============================================================

    private void loadOverridesForSelectedDay(
            Set<String> occupiedSlots
    ) {

        /*
         * Overrides are already keyed by teacherCode.
         *
         * teacherScheduleOverrides/{teacherCode}/entries
         */
        db.collection("teacherScheduleOverrides")
                .document(teacherCode)
                .collection("entries")
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                String day =
                                        document.getString(
                                                "day"
                                        );


                                if (!selectedDay.equals(day)) {
                                    continue;
                                }


                                String action =
                                        document.getString(
                                                "action"
                                        );


                                Long slotLong =
                                        document.getLong(
                                                "slot"
                                        );


                                // ============================================
                                // SLOT-BASED OVERRIDE
                                // ============================================

                                if (slotLong != null) {

                                    int slot =
                                            slotLong.intValue();


                                    if (
                                            slot >= 0
                                                    && slot < SLOT_STARTS.length
                                    ) {

                                        String key =
                                                SLOT_STARTS[slot];


                                        if (
                                                "DELETE".equals(
                                                        action
                                                )
                                        ) {

                                            occupiedSlots.remove(
                                                    key
                                            );

                                        } else {

                                            occupiedSlots.add(
                                                    key
                                            );
                                        }
                                    }


                                } else {

                                    // ========================================
                                    // TIME-BASED OVERRIDE
                                    // ========================================

                                    String startTime =
                                            document.getString(
                                                    "startTime"
                                            );

                                    String endTime =
                                            document.getString(
                                                    "endTime"
                                            );


                                    if (
                                            !TextUtils.isEmpty(
                                                    startTime
                                            )
                                    ) {

                                        if (
                                                "DELETE".equals(
                                                        action
                                                )
                                        ) {

                                            occupiedSlots.remove(
                                                    startTime
                                            );

                                        } else {

                                            markOverlappingSlots(
                                                    startTime,
                                                    endTime,
                                                    occupiedSlots
                                            );
                                        }
                                    }
                                }
                            }


                            // ================================================
                            // EXISTING MEETINGS
                            // ================================================

                            loadExistingMeetingRequests(
                                    occupiedSlots
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            loadingSlots = false;


                            slotStatusTextView.setText(
                                    "Could not load faculty availability."
                            );
                        }
                );
    }


    // ============================================================
    // EXISTING MEETING REQUESTS
    // ============================================================

    private void loadExistingMeetingRequests(
            Set<String> occupiedSlots
    ) {

        /*
         * IMPORTANT:
         *
         * Search by teacherCode, NOT teacherUid.
         */
        db.collection("meetingRequests")
                .whereEqualTo(
                        "teacherCode",
                        teacherCode
                )
                .whereEqualTo(
                        "date",
                        selectedDate
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                String status =
                                        document.getString(
                                                "status"
                                        );


                                /*
                                 * Only ACCEPTED requests block
                                 * a meeting slot.
                                 *
                                 * PENDING requests do not permanently
                                 * consume the slot.
                                 */
                                if (
                                        "ACCEPTED".equals(
                                                status
                                        )
                                ) {

                                    String startTime =
                                            document.getString(
                                                    "startTime"
                                            );

                                    String endTime =
                                            document.getString(
                                                    "endTime"
                                            );


                                    markOverlappingSlots(
                                            startTime,
                                            endTime,
                                            occupiedSlots
                                    );
                                }
                            }


                            buildAvailableSlotList(
                                    occupiedSlots
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            loadingSlots = false;


                            slotStatusTextView.setText(
                                    "Could not check existing meetings."
                            );
                        }
                );
    }


    // ============================================================
    // MARK OVERLAPPING SLOTS
    // ============================================================

    private void markOverlappingSlots(
            String startTime,
            String endTime,
            Set<String> occupiedSlots
    ) {

        if (
                TextUtils.isEmpty(startTime)
                        || TextUtils.isEmpty(endTime)
        ) {
            return;
        }


        int start =
                convertTimeToMinutes(
                        startTime
                );

        int end =
                convertTimeToMinutes(
                        endTime
                );


        if (start < 0 || end < 0) {
            return;
        }


        for (
                int i = 0;
                i < SLOT_STARTS.length;
                i++
        ) {

            int slotStart =
                    convertTimeToMinutes(
                            SLOT_STARTS[i]
                    );

            int slotEnd =
                    convertTimeToMinutes(
                            SLOT_ENDS[i]
                    );


            /*
             * Two intervals overlap when:
             *
             * start < slotEnd
             * AND
             * end > slotStart
             */
            if (
                    start < slotEnd
                            && end > slotStart
            ) {

                occupiedSlots.add(
                        SLOT_STARTS[i]
                );
            }
        }
    }


    // ============================================================
    // BUILD AVAILABLE SLOT LIST
    // ============================================================

    private void buildAvailableSlotList(
            Set<String> occupiedSlots
    ) {

        availableSlotIndexes.clear();


        for (
                int i = 0;
                i < SLOT_STARTS.length;
                i++
        ) {

            if (
                    !occupiedSlots.contains(
                            SLOT_STARTS[i]
                    )
            ) {

                availableSlotIndexes.add(
                        i
                );
            }
        }


        loadingSlots = false;


        // ========================================================
        // NO SLOTS
        // ========================================================

        if (availableSlotIndexes.isEmpty()) {

            slotStatusTextView.setText(
                    "No meeting slots are available on this date."
            );


            buttonMeetingTime.setText(
                    "No Slots Available"
            );


            buttonMeetingTime.setEnabled(
                    false
            );


            return;
        }


        // ========================================================
        // SLOTS AVAILABLE
        // ========================================================

        slotStatusTextView.setText(
                availableSlotIndexes.size()
                        + " meeting slot(s) available."
        );


        buttonMeetingTime.setText(
                "Select Meeting Slot"
        );


        buttonMeetingTime.setEnabled(
                true
        );
    }


    // ============================================================
    // SLOT DIALOG
    // ============================================================

    private void showAvailableSlotDialog() {

        if (loadingSlots) {
            return;
        }


        if (availableSlotIndexes.isEmpty()) {

            Toast.makeText(
                    this,
                    "No available slots.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String[] labels =
                new String[
                        availableSlotIndexes.size()
                        ];


        for (
                int i = 0;
                i < availableSlotIndexes.size();
                i++
        ) {

            int slotIndex =
                    availableSlotIndexes.get(i);


            labels[i] =
                    SLOT_LABELS[slotIndex];
        }


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Select Meeting Slot"
                        )
                        .setSingleChoiceItems(
                                labels,
                                -1,
                                (
                                        dialogInterface,
                                        which
                                ) -> {

                                    int slotIndex =
                                            availableSlotIndexes
                                                    .get(which);


                                    selectedStartTime =
                                            SLOT_STARTS[
                                                    slotIndex
                                                    ];


                                    selectedEndTime =
                                            SLOT_ENDS[
                                                    slotIndex
                                                    ];


                                    buttonMeetingTime.setText(
                                            SLOT_LABELS[
                                                    slotIndex
                                                    ]
                                    );


                                    dialogInterface.dismiss();
                                }
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();


        dialog.show();
    }


    // ============================================================
    // SUBMIT MEETING REQUEST
    // ============================================================

    private void submitMeetingRequest() {

        // ========================================================
        // DATE CHECK
        // ========================================================

        if (TextUtils.isEmpty(selectedDate)) {

            Toast.makeText(
                    this,
                    "Please select a meeting date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ========================================================
        // SLOT CHECK
        // ========================================================

        if (
                TextUtils.isEmpty(
                        selectedStartTime
                )
                        || TextUtils.isEmpty(
                        selectedEndTime
                )
        ) {

            Toast.makeText(
                    this,
                    "Please select an available meeting slot.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ========================================================
        // MESSAGE
        // ========================================================

        String message =
                messageEditText
                        .getText()
                        .toString()
                        .trim();


        // ========================================================
        // REQUEST ID
        // ========================================================

        String requestId =
                "REQ-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(
                                0,
                                8
                        )
                        .toUpperCase();


        // ========================================================
        // REQUEST OBJECT
        // ========================================================

        Map<String, Object> request =
                new HashMap<>();


        request.put(
                "requestId",
                requestId
        );


        // ========================================================
        // STUDENT
        // ========================================================

        request.put(
                "studentUid",
                studentUid
        );


        request.put(
                "studentName",
                ""
        );


        request.put(
                "studentEmail",
                auth.getCurrentUser()
                        .getEmail()
        );


        // ========================================================
        // TEACHER
        // ========================================================

        /*
         * NO teacherUid.
         *
         * Teacher is identified using teacherCode.
         */
        request.put(
                "teacherCode",
                teacherCode
        );


        request.put(
                "teacherName",
                teacherName
        );


        // ========================================================
        // DATE / TIME
        // ========================================================

        request.put(
                "date",
                selectedDate
        );


        request.put(
                "startTime",
                selectedStartTime
        );


        request.put(
                "endTime",
                selectedEndTime
        );


        // ========================================================
        // MESSAGE
        // ========================================================

        request.put(
                "message",
                message
        );


        // ========================================================
        // STATUS
        // ========================================================

        request.put(
                "status",
                "PENDING"
        );


        // ========================================================
        // RESPONSE FIELDS
        // ========================================================

        request.put(
                "responseMessage",
                ""
        );


        request.put(
                "respondedAt",
                null
        );


        request.put(
                "respondedBy",
                ""
        );


        // ========================================================
        // TIMESTAMPS
        // ========================================================

        Timestamp now =
                Timestamp.now();


        request.put(
                "createdAt",
                now
        );


        request.put(
                "updatedAt",
                now
        );


        // ========================================================
        // DISABLE BUTTON
        // ========================================================

        buttonSendRequest.setEnabled(
                false
        );


        buttonSendRequest.setText(
                "Sending..."
        );


        // ========================================================
        // LOAD LOGGED-IN STUDENT PROFILE
        // ========================================================

        /*
         * This is completely fine.
         *
         * We are loading the CURRENT student's profile,
         * not another teacher's profile.
         */
        db.collection("users")
                .document(studentUid)
                .get()
                .addOnSuccessListener(
                        studentDocument -> {

                            String studentName =
                                    studentDocument.getString(
                                            "name"
                                    );


                            if (
                                    TextUtils.isEmpty(
                                            studentName
                                    )
                            ) {

                                studentName =
                                        "Student";
                            }


                            request.put(
                                    "studentName",
                                    studentName
                            );


                            createRequestDocument(
                                    requestId,
                                    request
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            buttonSendRequest.setEnabled(
                                    true
                            );


                            buttonSendRequest.setText(
                                    getString(
                                            R.string.a3
                                    )
                            );


                            Toast.makeText(
                                    request_meeting.this,
                                    "Could not load student profile.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // ============================================================
    // CREATE FIRESTORE REQUEST
    // ============================================================

    private void createRequestDocument(
            String requestId,
            Map<String, Object> request
    ) {

        db.collection("meetingRequests")
                .document(requestId)
                .set(request)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    request_meeting.this,
                                    "Meeting request sent successfully.",
                                    Toast.LENGTH_LONG
                            ).show();


                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            buttonSendRequest.setEnabled(
                                    true
                            );


                            buttonSendRequest.setText(
                                    getString(
                                            R.string.a3
                                    )
                            );


                            Toast.makeText(
                                    request_meeting.this,
                                    "Failed to send request: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // ============================================================
    // TIME CONVERSION
    // ============================================================

    private int convertTimeToMinutes(
            String time
    ) {

        if (TextUtils.isEmpty(time)) {
            return -1;
        }


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


            return hour * 60 + minute;

        } catch (Exception e) {

            return -1;
        }
    }
}