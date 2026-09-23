package com.example.madproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity5 extends AppCompatActivity {

    private Button findteacher;
    private Button myrequests;

    private TextView textViewWelcome;
    private TextView textViewDepartment;
    private TextView textViewFavourite;

    private View teacherCard1;
    private View teacherCard2;

    private TextView textViewTeacher1;
    private TextView textViewSubject1;
    private TextView textViewRoom1;
    private TextView textViewAvailable1;

    private TextView textViewTeacher2;
    private TextView textViewSubject2;
    private TextView textViewRoom2;
    private TextView textViewAvailable2;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String studentUid;

    /*
     * teacherCode -> teacher document
     */
    private final Map<String, TeacherInfo> teacherDirectory =
            new HashMap<>();

    /*
     * The student's currently saved favourite teacher codes.
     */
    private final List<String> favouriteTeacherCodes =
            new ArrayList<>();

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

        textViewFavourite =
                findViewById(R.id.textViewFavourite);

        findteacher =
                findViewById(R.id.buttonFindTeacher);

        myrequests =
                findViewById(R.id.buttonMyRequests);

        teacherCard1 =
                findViewById(R.id.teacherCard1);

        teacherCard2 =
                findViewById(R.id.teacherCard2);

        textViewTeacher1 =
                findViewById(R.id.textViewTeacher1);

        textViewSubject1 =
                findViewById(R.id.textViewSubject1);

        textViewRoom1 =
                findViewById(R.id.textViewRoom1);

        textViewAvailable1 =
                findViewById(R.id.textViewAvailable1);

        textViewTeacher2 =
                findViewById(R.id.textViewTeacher2);

        textViewSubject2 =
                findViewById(R.id.textViewSubject2);

        textViewRoom2 =
                findViewById(R.id.textViewRoom2);

        textViewAvailable2 =
                findViewById(R.id.textViewAvailable2);


        // --------------------------------------------------
        // FIREBASE
        // --------------------------------------------------

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // --------------------------------------------------
        // LOAD STUDENT
        // --------------------------------------------------

        loadStudentProfile();


        // --------------------------------------------------
        // FAVOURITE TEACHERS
        // --------------------------------------------------

        textViewFavourite.setOnClickListener(
                v -> openFavouriteTeacherDialog()
        );


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

            hideFavouriteCards();

            return;
        }


        studentUid =
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

                        hideFavouriteCards();

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
                                getString(
                                        R.string.student_greeting
                                )
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


                    // --------------------------------------------------
                    // LOAD SAVED FAVOURITES
                    // --------------------------------------------------

                    loadSavedFavouriteTeachers(document);

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

                    hideFavouriteCards();

                    Toast.makeText(
                            MainActivity5.this,
                            getString(
                                    R.string.profile_load_failed
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ======================================================
    // LOAD SAVED FAVOURITE TEACHERS
    // ======================================================

    private void loadSavedFavouriteTeachers(
            DocumentSnapshot document
    ) {

        favouriteTeacherCodes.clear();

        List<String> savedFavourites =
                (List<String>) document.get("favouriteTeachers");


        if (savedFavourites != null) {

            for (String code : savedFavourites) {

                if (code != null
                        && !code.trim().isEmpty()) {

                    favouriteTeacherCodes.add(
                            code.trim()
                    );
                }
            }
        }


        /*
         * Load the current teacher directory after retrieving
         * the student's saved teacher codes.
         */
        loadTeacherDirectory();
    }


    // ======================================================
    // LOAD TEACHER DIRECTORY
    // ======================================================

    private void loadTeacherDirectory() {

        db.collection("teacherDirectory")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    teacherDirectory.clear();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String teacherCode =
                                document.getString("teacherCode");

                        String teacherName =
                                document.getString("name");

                        String xlsxSheet =
                                document.getString("xlsxSheet");


                        if (teacherCode == null
                                || teacherCode.trim().isEmpty()) {

                            continue;
                        }


                        TeacherInfo teacher =
                                new TeacherInfo();

                        teacher.teacherCode =
                                teacherCode.trim();

                        teacher.name =
                                teacherName != null
                                        ? teacherName.trim()
                                        : getString(
                                        R.string.faculty_name_not_available
                                );

                        teacher.xlsxSheet =
                                xlsxSheet != null
                                        ? xlsxSheet.trim()
                                        : "";


                        teacherDirectory.put(
                                teacher.teacherCode,
                                teacher
                        );
                    }


                    displayFavouriteTeachers();

                })
                .addOnFailureListener(e -> {

                    hideFavouriteCards();

                    Toast.makeText(
                            MainActivity5.this,
                            getString(
                                    R.string.teacher_directory_load_failed
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ======================================================
    // OPEN FAVOURITE TEACHER SELECTION
    // ======================================================

    private void openFavouriteTeacherDialog() {

        if (studentUid == null
                || studentUid.trim().isEmpty()) {

            Toast.makeText(
                    MainActivity5.this,
                    getString(
                            R.string.user_not_authenticated
                    ),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        /*
         * Make sure we have the latest directory before
         * showing the selection dialog.
         */
        db.collection("teacherDirectory")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    teacherDirectory.clear();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String teacherCode =
                                document.getString("teacherCode");

                        if (teacherCode == null
                                || teacherCode.trim().isEmpty()) {

                            continue;
                        }

                        TeacherInfo teacher =
                                new TeacherInfo();

                        teacher.teacherCode =
                                teacherCode.trim();

                        String teacherName =
                                document.getString("name");

                        teacher.name =
                                teacherName != null
                                        ? teacherName.trim()
                                        : getString(
                                        R.string.faculty_name_not_available
                                );

                        String xlsxSheet =
                                document.getString("xlsxSheet");

                        teacher.xlsxSheet =
                                xlsxSheet != null
                                        ? xlsxSheet.trim()
                                        : "";

                        teacherDirectory.put(
                                teacher.teacherCode,
                                teacher
                        );
                    }


                    showTeacherSelectionDialog();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MainActivity5.this,
                            getString(
                                    R.string.teacher_directory_load_failed
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ======================================================
    // TEACHER SELECTION DIALOG
    // ======================================================

    private void showTeacherSelectionDialog() {

        if (teacherDirectory.isEmpty()) {

            Toast.makeText(
                    MainActivity5.this,
                    getString(
                            R.string.no_teachers_available
                    ),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        List<String> teacherCodes =
                new ArrayList<>(
                        teacherDirectory.keySet()
                );


        Collections.sort(
                teacherCodes,
                String.CASE_INSENSITIVE_ORDER
        );


        String[] teacherLabels =
                new String[teacherCodes.size()];

        boolean[] checkedItems =
                new boolean[teacherCodes.size()];


        Set<String> savedCodes =
                new HashSet<>(
                        favouriteTeacherCodes
                );


        for (int i = 0;
             i < teacherCodes.size();
             i++) {

            String code =
                    teacherCodes.get(i);

            TeacherInfo teacher =
                    teacherDirectory.get(code);

            teacherLabels[i] =
                    teacher.name
                            + " (" + code + ")";

            checkedItems[i] =
                    savedCodes.contains(code);
        }


        AlertDialog dialog =
                new AlertDialog.Builder(
                        MainActivity5.this
                )
                        .setTitle(
                                getString(
                                        R.string.select_favourite_teachers
                                )
                        )
                        .setMultiChoiceItems(
                                teacherLabels,
                                checkedItems,
                                (dialogInterface, which, isChecked) -> {

                                    /*
                                     * Count the currently checked
                                     * teachers.
                                     */
                                    int selectedCount = 0;

                                    for (boolean checked :
                                            checkedItems) {

                                        if (checked) {
                                            selectedCount++;
                                        }
                                    }


                                    /*
                                     * Maximum of two favourites.
                                     */
                                    if (selectedCount > 2) {

                                        checkedItems[which] =
                                                false;

                                        ((android.content.DialogInterface)
                                                dialogInterface)
                                                .dismiss();

                                        Toast.makeText(
                                                MainActivity5.this,
                                                getString(
                                                        R.string.maximum_two_favourite_teachers
                                                ),
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        /*
                                         * Reopen the dialog with
                                         * the previous valid state.
                                         */
                                        showTeacherSelectionDialog();

                                    } else {

                                        /*
                                         * Keep the temporary
                                         * selection alive until
                                         * SAVE is pressed.
                                         */
                                        checkedItems[which] =
                                                isChecked;
                                    }
                                }
                        )
                        .setNegativeButton(
                                android.R.string.cancel,
                                null
                        )
                        .setPositiveButton(
                                android.R.string.ok,
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                dialogInterface -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(v -> {

                        List<String> selectedCodes =
                                new ArrayList<>();


                        for (int i = 0;
                             i < teacherCodes.size();
                             i++) {

                            if (checkedItems[i]) {

                                selectedCodes.add(
                                        teacherCodes.get(i)
                                );
                            }
                        }


                        /*
                         * Final safety check.
                         */
                        if (selectedCodes.size() > 2) {

                            Toast.makeText(
                                    MainActivity5.this,
                                    getString(
                                            R.string.maximum_two_favourite_teachers
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }


                        saveFavouriteTeachers(
                                selectedCodes
                        );

                        dialog.dismiss();
                    });
                });


        dialog.show();
    }


    // ======================================================
    // SAVE FAVOURITE TEACHERS
    // ======================================================

    private void saveFavouriteTeachers(
            List<String> selectedCodes
    ) {

        if (studentUid == null
                || studentUid.trim().isEmpty()) {

            return;
        }


        db.collection("users")
                .document(studentUid)
                .update(
                        "favouriteTeachers",
                        selectedCodes
                )
                .addOnSuccessListener(unused -> {

                    favouriteTeacherCodes.clear();

                    favouriteTeacherCodes.addAll(
                            selectedCodes
                    );


                    displayFavouriteTeachers();


                    Toast.makeText(
                            MainActivity5.this,
                            getString(
                                    R.string.favourite_teachers_saved
                            ),
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MainActivity5.this,
                            getString(
                                    R.string.favourite_teachers_save_failed
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ======================================================
    // DISPLAY FAVOURITE TEACHERS
    // ======================================================

    private void displayFavouriteTeachers() {

        hideFavouriteCards();


        if (favouriteTeacherCodes.isEmpty()) {

            return;
        }


        /*
         * Only two teachers can exist.
         */
        if (favouriteTeacherCodes.size() >= 1) {

            String code =
                    favouriteTeacherCodes.get(0);

            TeacherInfo teacher =
                    teacherDirectory.get(code);


            if (teacher != null) {

                teacherCard1.setVisibility(
                        View.VISIBLE
                );

                displayTeacherCard(
                        teacher,
                        textViewTeacher1,
                        textViewSubject1,
                        textViewRoom1,
                        textViewAvailable1
                );
            }
        }


        if (favouriteTeacherCodes.size() >= 2) {

            String code =
                    favouriteTeacherCodes.get(1);

            TeacherInfo teacher =
                    teacherDirectory.get(code);


            if (teacher != null) {

                teacherCard2.setVisibility(
                        View.VISIBLE
                );

                displayTeacherCard(
                        teacher,
                        textViewTeacher2,
                        textViewSubject2,
                        textViewRoom2,
                        textViewAvailable2
                );
            }
        }
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

    // ======================================================
    // DISPLAY ONE TEACHER CARD
    // ======================================================

    private void displayTeacherCard(
            TeacherInfo teacher,
            TextView teacherText,
            TextView subjectText,
            TextView roomText,
            TextView availabilityText
    ) {

        teacherText.setText(
                teacher.name
                        + " ("
                        + teacher.teacherCode
                        + ")"
        );

        subjectText.setText(
                getString(R.string.loading)
        );

        roomText.setText(
                getString(R.string.loading)
        );

        availabilityText.setText(
                getString(R.string.loading)
        );


        // --------------------------------------------------
        // TODAY
        // --------------------------------------------------

        String today =
                new SimpleDateFormat(
                        "EEE",
                        Locale.ENGLISH
                ).format(
                        Calendar.getInstance().getTime()
                );


        String normalizedToday =
                normalizeDay(today);


        /*
         * Get ALL schedules for this teacher first.
         *
         * We deliberately do not use:
         *
         * whereEqualTo("day", today)
         *
         * because Firestore contains values such as
         * "Tues" and "Thurs", while Android generates
         * "Tue" and "Thu".
         */
        db.collection("masterSchedules")
                .whereArrayContains(
                        "teacherCodes",
                        teacher.teacherCode
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<DocumentSnapshot> schedules =
                            new ArrayList<>();


                    // --------------------------------------------------
                    // NORMALIZE DAY AND KEEP TODAY'S SCHEDULES
                    // --------------------------------------------------

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String scheduleDay =
                                document.getString("day");

                        if (scheduleDay == null) {
                            continue;
                        }


                        String normalizedScheduleDay =
                                normalizeDay(scheduleDay);


                        if (normalizedScheduleDay.equals(
                                normalizedToday
                        )) {

                            schedules.add(
                                    document
                            );
                        }
                    }


                    // --------------------------------------------------
                    // SORT BY START TIME
                    // --------------------------------------------------

                    Collections.sort(
                            schedules,
                            Comparator.comparing(
                                    document ->
                                            getStringValue(
                                                    document,
                                                    "startTime"
                                            )
                            )
                    );


                    // --------------------------------------------------
                    // UPDATE CARD
                    // --------------------------------------------------

                    updateTeacherCardSchedule(
                            schedules,
                            subjectText,
                            roomText,
                            availabilityText
                    );

                })
                .addOnFailureListener(e -> {

                    subjectText.setText(
                            getString(
                                    R.string.schedule_unavailable
                            )
                    );

                    roomText.setText(
                            getString(
                                    R.string.room_unavailable
                            )
                    );

                    availabilityText.setText(
                            getString(
                                    R.string.schedule_load_failed
                            )
                    );
                });
    }


    // ======================================================
    // UPDATE TEACHER CARD WITH CURRENT / NEXT CLASS
    // ======================================================

    private void updateTeacherCardSchedule(
            List<DocumentSnapshot> schedules,
            TextView subjectText,
            TextView roomText,
            TextView availabilityText
    ) {

        if (schedules.isEmpty()) {

            subjectText.setText(
                    getString(
                            R.string.no_classes_today
                    )
            );

            roomText.setText(
                    getString(
                            R.string.room_not_available
                    )
            );

            availabilityText.setText(
                    getString(
                            R.string.teacher_no_classes_today
                    )
            );

            return;
        }


        int currentMinutes =
                getCurrentMinutes();


        DocumentSnapshot currentClass =
                null;

        DocumentSnapshot nextClass =
                null;


        for (DocumentSnapshot document :
                schedules) {

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


            int startMinutes =
                    convertTimeToMinutes(start);

            int endMinutes =
                    convertTimeToMinutes(end);


            if (startMinutes <= currentMinutes
                    && currentMinutes < endMinutes) {

                currentClass =
                        document;

            } else if (startMinutes > currentMinutes
                    && nextClass == null) {

                nextClass =
                        document;
            }
        }


        // --------------------------------------------------
        // CURRENT CLASS
        // --------------------------------------------------

        if (currentClass != null) {

            String subject =
                    getStringValue(
                            currentClass,
                            "subject"
                    );

            String start =
                    getStringValue(
                            currentClass,
                            "startTime"
                    );

            String end =
                    getStringValue(
                            currentClass,
                            "endTime"
                    );

            String room =
                    getStringValue(
                            currentClass,
                            "room"
                    );


            subjectText.setText(
                    getString(
                            R.string.current_class_format,
                            subject
                    )
            );

            roomText.setText(
                    getString(
                            R.string.room_format,
                            room
                    ));


            if (nextClass != null) {

                String nextSubject =
                        getStringValue(
                                nextClass,
                                "subject"
                        );

                String nextStart =
                        getStringValue(
                                nextClass,
                                "startTime"
                        );

                availabilityText.setText(
                        getString(
                                R.string.next_class_format,
                                nextSubject,
                                nextStart
                        )
                );

            } else {

                availabilityText.setText(
                        getString(
                                R.string.no_more_classes_today
                        )
                );
            }

            return;
        }


        // --------------------------------------------------
        // NO CURRENT CLASS
        // --------------------------------------------------

        if (nextClass != null) {

            String nextSubject =
                    getStringValue(
                            nextClass,
                            "subject"
                    );

            String nextStart =
                    getStringValue(
                            nextClass,
                            "startTime"
                    );

            String nextEnd =
                    getStringValue(
                            nextClass,
                            "endTime"
                    );

            String nextRoom =
                    getStringValue(
                            nextClass,
                            "room"
                    );


            subjectText.setText(
                    getString(
                            R.string.next_class_format,
                            nextSubject,
                            nextStart
                    )
            );

            roomText.setText(
                    getString(
                            R.string.room_format,
                            nextRoom
                    )
            );

            availabilityText.setText(
                    getString(
                            R.string.next_class_time_format,
                            nextStart,
                            nextEnd
                    )
            );

            return;
        }


        // --------------------------------------------------
        // ALL CLASSES FINISHED
        // --------------------------------------------------

        subjectText.setText(
                getString(
                        R.string.no_more_classes_today
                )
        );

        roomText.setText(
                getString(
                        R.string.room_not_available
                )
        );

        availabilityText.setText(
                getString(
                        R.string.teacher_finished_for_today
                )
        );
    }


    // ======================================================
    // HIDE FAVOURITE CARDS
    // ======================================================

    private void hideFavouriteCards() {

        teacherCard1.setVisibility(
                View.GONE
        );

        teacherCard2.setVisibility(
                View.GONE
        );
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
    // CONVERT HH:MM TO MINUTES
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
                    time.trim().split(":");

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


    // ======================================================
    // SAFE FIRESTORE STRING
    // ======================================================

    private String getStringValue(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(field);


        if (value == null) {
            return "";
        }


        return String.valueOf(value);
    }


    // ======================================================
    // TEACHER INFO MODEL
    // ======================================================

    private static class TeacherInfo {

        String teacherCode;
        String name;
        String xlsxSheet;
    }
}