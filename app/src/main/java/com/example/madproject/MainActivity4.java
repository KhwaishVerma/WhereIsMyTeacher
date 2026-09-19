package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity4 extends AppCompatActivity {

    Button b1;
    Spinner department, designation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        department = findViewById(R.id.editTextText13);
        designation = findViewById(R.id.editTextText14);

        String[] departments = {
                "Select Department",
                "STME",
                "SBM",
                "SOC",
                "SPTM",
                "SOL"
        };

        String[] designations = {
                "Select Designation",
                "Professor",
                "Associate Professor",
                "Assistant Professor",
                "Lecturer",
                "Visiting Faculty"
        };

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                departments
        );

        departmentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        department.setAdapter(departmentAdapter);

        ArrayAdapter<String> designationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                designations
        );

        designationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        designation.setAdapter(designationAdapter);

        b1 = findViewById(R.id.button7);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity4.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}