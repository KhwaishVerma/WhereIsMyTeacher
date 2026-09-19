package com.example.madproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class request_meeting extends AppCompatActivity {

    private TextView dateTextView;
    private Button buttonMeetingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_request_meeting);

        dateTextView = findViewById(R.id.dateTextView);
        buttonMeetingTime = findViewById(R.id.buttonMeetingTime);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        dateTextView.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            request_meeting.this,

                            (view, selectedYear, selectedMonth, selectedDay) -> {

                                String selectedDate =
                                        selectedDay + "/" +
                                                (selectedMonth + 1) + "/" +
                                                selectedYear;

                                dateTextView.setText(selectedDate);
                            },

                            year,
                            month,
                            day
                    );

            datePickerDialog.show();
        });

        buttonMeetingTime.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog =
                    new TimePickerDialog(
                            request_meeting.this,

                            (view, selectedHour, selectedMinute) -> {

                                String amPm;

                                if (selectedHour >= 12) {
                                    amPm = "PM";
                                } else {
                                    amPm = "AM";
                                }

                                int displayHour = selectedHour % 12;

                                if (displayHour == 0) {
                                    displayHour = 12;
                                }

                                String selectedTime =
                                        String.format(
                                                "%02d:%02d %s",
                                                displayHour,
                                                selectedMinute,
                                                amPm
                                        );

                                buttonMeetingTime.setText(selectedTime);
                            },

                            hour,
                            minute,
                            false
                    );

            timePickerDialog.show();
        });
    }
}