package com.example.madproject;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class student_requests extends AppCompatActivity {

    // ============================================================
    // FIREBASE
    // ============================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // ============================================================
    // UI
    // ============================================================

    private LinearLayout pendingCard;
    private LinearLayout confirmedCard;

    private TextView pendingTeacher;
    private TextView pendingDate;
    private TextView pendingTime;
    private TextView pendingStatus;

    private TextView confirmedTeacher;
    private TextView confirmedDate;
    private TextView confirmedTime;
    private TextView confirmedLocation;
    private TextView confirmedStatus;


    // ============================================================
    // LIFECYCLE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_student_requests);

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
        // FIREBASE INITIALIZATION
        // ========================================================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // ========================================================
        // FIND VIEWS
        // ========================================================

        pendingCard =
                findViewById(R.id.pendingCard);

        confirmedCard =
                findViewById(R.id.confirmedCard);

        pendingTeacher =
                findViewById(R.id.pendingTeacher);

        pendingDate =
                findViewById(R.id.pendingDate);

        pendingTime =
                findViewById(R.id.pendingTime);

        pendingStatus =
                findViewById(R.id.pendingStatus);


        confirmedTeacher =
                findViewById(R.id.confirmedTeacher);

        confirmedDate =
                findViewById(R.id.confirmedDate);

        confirmedTime =
                findViewById(R.id.confirmedTime);

        confirmedLocation =
                findViewById(R.id.confirmedLocation);

        confirmedStatus =
                findViewById(R.id.confirmedStatus);


        // ========================================================
        // BACK BUTTON
        // ========================================================

        TextView backButton =
                findViewById(R.id.backButton);

        backButton.setOnClickListener(
                v -> finish()
        );


        // ========================================================
        // DEFAULT STATE
        // ========================================================

        pendingCard.setVisibility(View.GONE);
        confirmedCard.setVisibility(View.GONE);


        // ========================================================
        // LOAD REQUESTS
        // ========================================================

        loadStudentRequests();
    }


    // ============================================================
    // LOAD STUDENT REQUESTS
    // ============================================================

    private void loadStudentRequests() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            showNoPendingRequest();
            showNoConfirmedRequest();

            return;
        }

        String studentUid =
                currentUser.getUid();


        /*
         * IMPORTANT:
         *
         * We identify the current student using Firebase Auth UID.
         *
         * We do NOT use:
         *
         * teacherUid
         * teacherId
         * studentId
         *
         * The meeting request schema uses:
         *
         * studentUid
         * teacherCode
         */

        db.collection("meetingRequests")
                .whereEqualTo(
                        "studentUid",
                        studentUid
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            List<DocumentSnapshot> requests =
                                    new ArrayList<>(
                                            querySnapshot.getDocuments()
                                    );


                            // =================================================
                            // SORT LOCALLY BY CREATED TIME
                            // =================================================

                            Collections.sort(
                                    requests,
                                    new Comparator<DocumentSnapshot>() {

                                        @Override
                                        public int compare(
                                                DocumentSnapshot a,
                                                DocumentSnapshot b
                                        ) {

                                            Timestamp timeA =
                                                    a.getTimestamp(
                                                            "createdAt"
                                                    );

                                            Timestamp timeB =
                                                    b.getTimestamp(
                                                            "createdAt"
                                                    );


                                            if (timeA == null
                                                    && timeB == null) {

                                                return 0;
                                            }

                                            if (timeA == null) {

                                                return 1;
                                            }

                                            if (timeB == null) {

                                                return -1;
                                            }


                                            return timeB.compareTo(
                                                    timeA
                                            );
                                        }
                                    }
                            );


                            // =================================================
                            // FIND LATEST REQUEST OF EACH TYPE
                            // =================================================

                            DocumentSnapshot pendingRequest =
                                    null;

                            DocumentSnapshot acceptedRequest =
                                    null;

                            DocumentSnapshot rejectedRequest =
                                    null;


                            for (
                                    DocumentSnapshot request
                                    : requests
                            ) {

                                String status =
                                        getString(
                                                request,
                                                "status"
                                        );


                                if (
                                        status.equalsIgnoreCase(
                                                "PENDING"
                                        )
                                                &&
                                                pendingRequest == null
                                ) {

                                    pendingRequest =
                                            request;
                                }


                                if (
                                        status.equalsIgnoreCase(
                                                "ACCEPTED"
                                        )
                                                &&
                                                acceptedRequest == null
                                ) {

                                    acceptedRequest =
                                            request;
                                }


                                if (
                                        status.equalsIgnoreCase(
                                                "REJECTED"
                                        )
                                                &&
                                                rejectedRequest == null
                                ) {

                                    rejectedRequest =
                                            request;
                                }
                            }


                            // =================================================
                            // DISPLAY PENDING
                            // =================================================

                            if (pendingRequest != null) {

                                displayPendingRequest(
                                        pendingRequest
                                );

                            } else {

                                showNoPendingRequest();
                            }


                            // =================================================
                            // DISPLAY ACCEPTED
                            // =================================================

                            if (acceptedRequest != null) {

                                displayAcceptedRequest(
                                        acceptedRequest
                                );

                            } else {

                                /*
                                 * If there is no accepted request,
                                 * don't show a fake confirmed card.
                                 *
                                 * If a rejected request exists, we can
                                 * use the confirmed card area to show
                                 * the latest rejected result.
                                 */

                                if (
                                        rejectedRequest != null
                                ) {

                                    displayRejectedRequest(
                                            rejectedRequest
                                    );

                                } else {

                                    showNoConfirmedRequest();
                                }
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showNoPendingRequest();
                            showNoConfirmedRequest();

                            android.util.Log.e(
                                    "WIMT_STUDENT_REQUESTS",
                                    "Failed to load meeting requests",
                                    e
                            );
                        }
                );
    }


    // ============================================================
    // DISPLAY PENDING REQUEST
    // ============================================================

    private void displayPendingRequest(
            DocumentSnapshot request
    ) {

        pendingCard.setVisibility(
                View.VISIBLE
        );


        String teacherName =
                getString(
                        request,
                        "teacherName"
                );

        String teacherCode =
                getString(
                        request,
                        "teacherCode"
                );

        String date =
                getString(
                        request,
                        "date"
                );

        String startTime =
                getString(
                        request,
                        "startTime"
                );

        String endTime =
                getString(
                        request,
                        "endTime"
                );


        if (teacherName.isEmpty()) {

            teacherName =
                    "Teacher";
        }


        if (!teacherCode.isEmpty()) {

            pendingTeacher.setText(
                    teacherName
                            + "\nTeacher Code: "
                            + teacherCode
            );

        } else {

            pendingTeacher.setText(
                    teacherName
            );
        }


        pendingDate.setText(
                "Date: "
                        + date
        );


        pendingTime.setText(
                "Time: "
                        + startTime
                        + " - "
                        + endTime
        );


        pendingStatus.setText(
                "Status: PENDING"
        );

        pendingStatus.setTextColor(
                0xFFFF9800
        );
    }


    // ============================================================
    // DISPLAY ACCEPTED REQUEST
    // ============================================================

    private void displayAcceptedRequest(
            DocumentSnapshot request
    ) {

        confirmedCard.setVisibility(
                View.VISIBLE
        );


        String teacherName =
                getString(
                        request,
                        "teacherName"
                );

        String teacherCode =
                getString(
                        request,
                        "teacherCode"
                );

        String date =
                getString(
                        request,
                        "date"
                );

        String startTime =
                getString(
                        request,
                        "startTime"
                );

        String endTime =
                getString(
                        request,
                        "endTime"
                );


        if (teacherName.isEmpty()) {

            teacherName =
                    "Teacher";
        }


        if (!teacherCode.isEmpty()) {

            confirmedTeacher.setText(
                    teacherName
                            + "\nTeacher Code: "
                            + teacherCode
            );

        } else {

            confirmedTeacher.setText(
                    teacherName
            );
        }


        confirmedDate.setText(
                "Date: "
                        + date
        );


        confirmedTime.setText(
                "Time: "
                        + startTime
                        + " - "
                        + endTime
        );


        /*
         * Current meetingRequests schema does not contain
         * a classroom/room field.
         *
         * Therefore we should NOT invent a location.
         */

        confirmedLocation.setText(
                "Location: To be confirmed"
        );


        confirmedStatus.setText(
                "Status: ACCEPTED"
        );

        confirmedStatus.setTextColor(
                0xFF07CC07
        );
    }


    // ============================================================
    // DISPLAY REJECTED REQUEST
    // ============================================================

    private void displayRejectedRequest(
            DocumentSnapshot request
    ) {

        confirmedCard.setVisibility(
                View.VISIBLE
        );


        String teacherName =
                getString(
                        request,
                        "teacherName"
                );

        String teacherCode =
                getString(
                        request,
                        "teacherCode"
                );

        String date =
                getString(
                        request,
                        "date"
                );

        String startTime =
                getString(
                        request,
                        "startTime"
                );

        String endTime =
                getString(
                        request,
                        "endTime"
                );

        String response =
                getString(
                        request,
                        "responseMessage"
                );


        if (teacherName.isEmpty()) {

            teacherName =
                    "Teacher";
        }


        if (!teacherCode.isEmpty()) {

            confirmedTeacher.setText(
                    teacherName
                            + "\nTeacher Code: "
                            + teacherCode
            );

        } else {

            confirmedTeacher.setText(
                    teacherName
            );
        }


        confirmedDate.setText(
                "Date: "
                        + date
        );


        confirmedTime.setText(
                "Time: "
                        + startTime
                        + " - "
                        + endTime
        );


        if (!response.isEmpty()) {

            confirmedLocation.setText(
                    "Teacher Response: "
                            + response
            );

        } else {

            confirmedLocation.setText(
                    "Teacher Response: No response message"
            );
        }


        confirmedStatus.setText(
                "Status: REJECTED"
        );

        confirmedStatus.setTextColor(
                0xFFFF0000
        );
    }


    // ============================================================
    // EMPTY STATES
    // ============================================================

    private void showNoPendingRequest() {

        pendingCard.setVisibility(
                View.VISIBLE
        );


        pendingTeacher.setText(
                "No Pending Request"
        );

        pendingDate.setText(
                "You currently have no pending meeting request."
        );

        pendingTime.setText(
                ""
        );

        pendingStatus.setText(
                "Status: NONE"
        );

        pendingStatus.setTextColor(
                0xFF777777
        );
    }


    private void showNoConfirmedRequest() {

        confirmedCard.setVisibility(
                View.VISIBLE
        );


        confirmedTeacher.setText(
                "No Confirmed Meeting"
        );

        confirmedDate.setText(
                "You currently have no accepted meeting."
        );

        confirmedTime.setText(
                ""
        );

        confirmedLocation.setText(
                ""
        );

        confirmedStatus.setText(
                "Status: NONE"
        );

        confirmedStatus.setTextColor(
                0xFF777777
        );
    }


    // ============================================================
    // FIRESTORE STRING HELPER
    // ============================================================

    private String getString(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(field);

        return value == null
                ? ""
                : value.trim();
    }
}