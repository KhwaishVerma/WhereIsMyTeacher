package com.example.madproject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class upload_tt extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private Button uploadButton;
    private Button importButton;
    private Button downloadButton;

    private TextView fileNameText;
    private TextView statusText;
    private TextView jsonText;

    private TableLayout tableLayout;
    private ProgressBar progressBar;


    // =========================================================
    // FILE
    // =========================================================

    private Uri selectedFileUri;

    private String selectedFileName = "";

    private String selectedFileType = "";


    // =========================================================
    // TABLE DATA
    // =========================================================

    private final ArrayList<ArrayList<String>> tableData =
            new ArrayList<>();


    // =========================================================
    // JSON
    // =========================================================

    private JSONObject finalJson;


    // =========================================================
    // BACKGROUND THREAD
    // =========================================================

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    // =========================================================
    // FILE PICKER
    // =========================================================

    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {

                        if (uri == null) {
                            return;
                        }

                        selectedFileUri = uri;

                        selectedFileName =
                                getFileName(uri);

                        selectedFileType =
                                detectFileType(
                                        selectedFileName
                                );


                        fileNameText.setText(
                                selectedFileName
                        );


                        tableData.clear();

                        tableLayout.removeAllViews();

                        finalJson = null;

                        jsonText.setText(
                                "JSON output will appear here..."
                        );

                        downloadButton.setEnabled(
                                false
                        );


                        if (
                                selectedFileType.equals("CSV")
                                        ||
                                        selectedFileType.equals("XLS")
                                        ||
                                        selectedFileType.equals("XLSX")
                        ) {

                            importButton.setEnabled(
                                    true
                            );

                            statusText.setText(
                                    "Ready to import."
                            );

                        } else {

                            importButton.setEnabled(
                                    false
                            );

                            statusText.setText(
                                    "Unsupported file. Please select CSV, XLS, or XLSX."
                            );
                        }
                    }
            );


    // =========================================================
    // SAVE JSON
    // =========================================================

    private final ActivityResultLauncher<Intent> saveJsonLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (
                                result.getResultCode()
                                        != RESULT_OK
                                        ||
                                        result.getData() == null
                        ) {

                            return;
                        }


                        Uri uri =
                                result.getData()
                                        .getData();


                        if (uri != null) {

                            saveJson(uri);
                        }
                    }
            );


    // =========================================================
    // ON CREATE
    // =========================================================

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_upload_tt
        );


        // -----------------------------------------------------
        // Find Views
        // -----------------------------------------------------

        uploadButton =
                findViewById(
                        R.id.uploadButton
                );

        importButton =
                findViewById(
                        R.id.ocrButton
                );

        downloadButton =
                findViewById(
                        R.id.downloadButton
                );

        fileNameText =
                findViewById(
                        R.id.fileNameText
                );

        statusText =
                findViewById(
                        R.id.statusText
                );

        jsonText =
                findViewById(
                        R.id.jsonText
                );

        tableLayout =
                findViewById(
                        R.id.ocrTable
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );


        importButton.setEnabled(
                false
        );

        downloadButton.setEnabled(
                false
        );


        // -----------------------------------------------------
        // Select File
        // -----------------------------------------------------

        uploadButton.setOnClickListener(
                v -> {

                    filePicker.launch(
                            new String[]{
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "application/vnd.ms-excel",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            }
                    );
                }
        );


        // -----------------------------------------------------
        // Import
        // -----------------------------------------------------

        importButton.setOnClickListener(
                v -> {

                    if (selectedFileUri == null) {

                        Toast.makeText(
                                this,
                                "Please select a timetable file.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    importFile();
                }
        );


        // -----------------------------------------------------
        // Export JSON
        // -----------------------------------------------------

        downloadButton.setOnClickListener(
                v -> {

                    if (finalJson == null) {

                        Toast.makeText(
                                this,
                                "Import a timetable first.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    Intent intent =
                            new Intent(
                                    Intent.ACTION_CREATE_DOCUMENT
                            );


                    intent.setType(
                            "application/json"
                    );


                    intent.putExtra(
                            Intent.EXTRA_TITLE,
                            "timetable.json"
                    );


                    saveJsonLauncher.launch(
                            intent
                    );
                }
        );
    }


    // =========================================================
    // IMPORT FILE
    // =========================================================

    private void importFile() {

        importButton.setEnabled(
                false
        );

        downloadButton.setEnabled(
                false
        );

        progressBar.setVisibility(
                View.VISIBLE
        );

        tableLayout.removeAllViews();

        tableData.clear();

        finalJson = null;


        statusText.setText(
                "Reading timetable..."
        );


        executor.execute(
                () -> {

                    try {

                        if (
                                selectedFileType.equals("CSV")
                        ) {

                            readCSV();

                        } else if (
                                selectedFileType.equals("XLS")
                        ) {

                            readExcel(
                                    true
                            );

                        } else if (
                                selectedFileType.equals("XLSX")
                        ) {

                            readExcel(
                                    false
                            );
                        }


                        displayTable();


                    } catch (Exception e) {

                        showError(
                                "Import failed: " +
                                        e.getMessage()
                        );
                    }
                }
        );
    }


    // =========================================================
    // CSV
    // =========================================================

    private void readCSV()
            throws Exception {

        InputStream input =
                getContentResolver()
                        .openInputStream(
                                selectedFileUri
                        );


        if (input == null) {

            throw new Exception(
                    "Could not open CSV file."
            );
        }


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                StandardCharsets.UTF_8
                        )
                );


        String line;


        while (
                (line = reader.readLine())
                        != null
        ) {

            if (
                    line.trim().isEmpty()
            ) {

                continue;
            }


            tableData.add(
                    parseCSVLine(
                            line
                    )
            );
        }


        reader.close();
    }


    // =========================================================
    // CSV PARSER
    // =========================================================

    private ArrayList<String> parseCSVLine(
            String line
    ) {

        ArrayList<String> values =
                new ArrayList<>();


        StringBuilder current =
                new StringBuilder();


        boolean quoted =
                false;


        for (
                int i = 0;
                i < line.length();
                i++
        ) {

            char c =
                    line.charAt(i);


            if (c == '"') {

                if (
                        quoted
                                &&
                                i + 1 < line.length()
                                &&
                                line.charAt(i + 1) == '"'
                ) {

                    current.append(
                            '"'
                    );

                    i++;

                } else {

                    quoted =
                            !quoted;
                }


            } else if (
                    c == ','
                            &&
                            !quoted
            ) {

                values.add(
                        current
                                .toString()
                                .trim()
                );

                current.setLength(
                        0
                );


            } else {

                current.append(
                        c
                );
            }
        }


        values.add(
                current
                        .toString()
                        .trim()
        );


        return values;
    }


    // =========================================================
    // EXCEL
    // =========================================================

    private void readExcel(
            boolean oldFormat
    )
            throws Exception {

        InputStream input =
                getContentResolver()
                        .openInputStream(
                                selectedFileUri
                        );


        if (input == null) {

            throw new Exception(
                    "Could not open Excel file."
            );
        }


        Workbook workbook;


        if (oldFormat) {

            workbook =
                    new HSSFWorkbook(
                            input
                    );

        } else {

            workbook =
                    new XSSFWorkbook(
                            input
                    );
        }


        Sheet sheet =
                workbook.getSheetAt(
                        0
                );


        DataFormatter formatter =
                new DataFormatter();


        for (
                Row excelRow :
                sheet
        ) {

            ArrayList<String> row =
                    new ArrayList<>();


            int first =
                    excelRow
                            .getFirstCellNum();


            int last =
                    excelRow
                            .getLastCellNum();


            if (
                    first < 0
                            ||
                            last < 0
            ) {

                continue;
            }


            for (
                    int c = first;
                    c < last;
                    c++
            ) {

                Cell cell =
                        excelRow.getCell(
                                c
                        );


                String value =
                        cell == null
                                ? ""
                                : formatter
                                .formatCellValue(
                                        cell
                                );


                row.add(
                        value.trim()
                );
            }


            if (
                    !row.isEmpty()
            ) {

                tableData.add(
                        row
                );
            }
        }


        workbook.close();

        input.close();
    }


    // =========================================================
    // DISPLAY TABLE
    // =========================================================

    private void displayTable() {

        runOnUiThread(
                () -> {

                    progressBar.setVisibility(
                            View.GONE
                    );


                    tableLayout.removeAllViews();


                    if (
                            tableData.isEmpty()
                    ) {

                        statusText.setText(
                                "No table data found."
                        );

                        importButton.setEnabled(
                                true
                        );

                        return;
                    }


                    int maxColumns =
                            0;


                    for (
                            ArrayList<String> row :
                            tableData
                    ) {

                        maxColumns =
                                Math.max(
                                        maxColumns,
                                        row.size()
                                );
                    }


                    // -------------------------------------------------
                    // Build actual spreadsheet grid
                    // -------------------------------------------------

                    for (
                            int r = 0;
                            r < tableData.size();
                            r++
                    ) {

                        TableRow tableRow =
                                new TableRow(
                                        this
                                );


                        ArrayList<String> row =
                                tableData.get(
                                        r
                                );


                        for (
                                int c = 0;
                                c < maxColumns;
                                c++
                        ) {

                            String value =
                                    c < row.size()
                                            ? row.get(c)
                                            : "";


                            tableRow.addView(
                                    createCell(
                                            value,
                                            r == 0
                                    )
                            );
                        }


                        tableLayout.addView(
                                tableRow
                        );
                    }


                    // -------------------------------------------------
                    // JSON
                    // -------------------------------------------------

                    finalJson =
                            generateJSON();


                    jsonText.setText(
                            prettyJSON(
                                    finalJson
                            )
                    );


                    downloadButton.setEnabled(
                            true
                    );


                    importButton.setEnabled(
                            true
                    );


                    statusText.setText(
                            "Imported " +
                                    tableData.size() +
                                    " rows × " +
                                    maxColumns +
                                    " columns."
                    );
                }
        );
    }


    // =========================================================
    // CELL
    // =========================================================

    private TextView createCell(
            String text,
            boolean header
    ) {

        TextView cell =
                new TextView(
                        this
                );


        if (
                text == null
                        ||
                        text.trim().isEmpty()
        ) {

            text = "-";
        }


        cell.setText(
                text
        );


        cell.setTextSize(
                header ? 13 : 12
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
                180
        );


        cell.setTextColor(
                Color.parseColor(
                        "#111827"
                )
        );


        cell.setBackgroundColor(
                header
                        ? Color.parseColor(
                        "#E5E7EB"
                )
                        : Color.WHITE
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
    // GENERATE JSON
    // =========================================================
    private JSONObject parseTimetableCell(
            String time,
            String content
    ) {

        JSONObject result =
                new JSONObject();

        try {

            result.put(
                    "time",
                    time
            );

            // -----------------------------------------------------
            // Empty cell
            // -----------------------------------------------------

            if (
                    content == null ||
                            content.trim().isEmpty() ||
                            content.equals("-")
            ) {

                result.put(
                        "subject",
                        null
                );

                result.put(
                        "type",
                        "FREE"
                );

                result.put(
                        "batch",
                        null
                );

                result.put(
                        "faculty",
                        null
                );

                result.put(
                        "room",
                        null
                );

                return result;
            }


            String value =
                    content.trim();


            // -----------------------------------------------------
            // BREAK
            // -----------------------------------------------------

            String upper =
                    value.toUpperCase(
                            Locale.US
                    );

            if (
                    upper.equals("SHORT BREAK") ||
                            upper.equals("BREAK")
            ) {

                result.put(
                        "subject",
                        value
                );

                result.put(
                        "type",
                        "BREAK"
                );

                result.put(
                        "batch",
                        null
                );

                result.put(
                        "faculty",
                        null
                );

                result.put(
                        "room",
                        null
                );

                return result;
            }


            // -----------------------------------------------------
            // LUNCH
            // -----------------------------------------------------

            if (
                    upper.equals("LUNCH")
            ) {

                result.put(
                        "subject",
                        "LUNCH"
                );

                result.put(
                        "type",
                        "BREAK"
                );

                result.put(
                        "batch",
                        null
                );

                result.put(
                        "faculty",
                        null
                );

                result.put(
                        "room",
                        null
                );

                return result;
            }


            // -----------------------------------------------------
            // Split the imported cell
            //
            // Example:
            //
            // Artificial Intelligence |
            // Lab |
            // Batch 01 |
            // VK |
            // CL 206
            // -----------------------------------------------------

            String[] parts =
                    value.split(
                            "\\|"
                    );


            ArrayList<String> cleanParts =
                    new ArrayList<>();


            for (String part :
                    parts) {

                String cleaned =
                        part.trim();

                if (!cleaned.isEmpty()) {

                    cleanParts.add(
                            cleaned
                    );
                }
            }


            // -----------------------------------------------------
            // SUBJECT
            // -----------------------------------------------------

            String subject =
                    cleanParts.size() > 0
                            ? cleanParts.get(0)
                            : null;


            // -----------------------------------------------------
            // TYPE
            // -----------------------------------------------------

            String type =
                    null;


            for (String part :
                    cleanParts) {

                String p =
                        part.toUpperCase(
                                Locale.US
                        );

                if (
                        p.equals("THEORY") ||
                                p.equals("LAB")
                ) {

                    type = p;

                    break;
                }
            }


            // -----------------------------------------------------
            // BATCH
            // -----------------------------------------------------

            String batch =
                    null;


            for (String part :
                    cleanParts) {

                String p =
                        part.trim();


                if (
                        p.toLowerCase(
                                Locale.US
                        ).startsWith(
                                "batch"
                        )
                ) {

                    batch = p;

                    break;
                }
            }


            // -----------------------------------------------------
            // ROOM
            // -----------------------------------------------------

            String room =
                    null;


            for (String part :
                    cleanParts) {

                String p =
                        part.trim()
                                .toUpperCase(
                                        Locale.US
                                );


                /*
                 * Known room/lab formats:
                 *
                 * CR 311
                 * CL 206
                 * CC 402
                 * LL 201
                 */

                if (
                        p.matches(
                                "(CR|CL|CC|LL)\\s*\\d+"
                        )
                ) {

                    room = part.trim();

                    break;
                }
            }


            // -----------------------------------------------------
            // FACULTY
            // -----------------------------------------------------

            String faculty =
                    null;


            /*
             * Faculty is normally the remaining
             * short token after removing:
             *
             * subject
             * type
             * batch
             * room
             */

            for (String part :
                    cleanParts) {

                String p =
                        part.trim();


                if (
                        p.equals(subject)
                ) {
                    continue;
                }


                if (
                        type != null &&
                                p.equalsIgnoreCase(type)
                ) {
                    continue;
                }


                if (
                        batch != null &&
                                p.equalsIgnoreCase(batch)
                ) {
                    continue;
                }


                if (
                        room != null &&
                                p.equalsIgnoreCase(room)
                ) {
                    continue;
                }


                /*
                 * Faculty codes in your timetable
                 * are short identifiers such as:
                 *
                 * JP
                 * VK
                 * RD
                 * PS
                 * DS
                 * NiG
                 * RGM
                 * PST
                 * AS
                 */

                if (
                        p.matches(
                                "[A-Za-z]{1,5}"
                        )
                ) {

                    faculty = p;

                    break;
                }
            }


            // -----------------------------------------------------
            // DEFAULT TYPE
            // -----------------------------------------------------

            if (type == null) {

                type = "OTHER";
            }


            // -----------------------------------------------------
            // FINAL OBJECT
            // -----------------------------------------------------

            result.put(
                    "subject",
                    subject
            );

            result.put(
                    "type",
                    type
            );

            result.put(
                    "batch",
                    batch
            );

            result.put(
                    "faculty",
                    faculty
            );

            result.put(
                    "room",
                    room
            );

        } catch (Exception e) {

            e.printStackTrace();
        }


        return result;
    }
    private JSONObject generateJSON() {

        JSONObject root = new JSONObject();

        try {

            root.put(
                    "type",
                    "timetable"
            );

            root.put(
                    "sourceFile",
                    selectedFileName
            );

            root.put(
                    "sourceType",
                    selectedFileType
            );

            JSONArray schedule =
                    new JSONArray();

            if (tableData.isEmpty()) {
                root.put("schedule", schedule);
                return root;
            }

            // -----------------------------------------------------
            // First row = column headers
            // -----------------------------------------------------

            ArrayList<String> headers =
                    tableData.get(0);

            // -----------------------------------------------------
            // Every remaining row = one day
            // -----------------------------------------------------

            for (int r = 1;
                 r < tableData.size();
                 r++) {

                ArrayList<String> row =
                        tableData.get(r);

                if (row.isEmpty()) {
                    continue;
                }

                JSONObject dayObject =
                        new JSONObject();

                // First column = Day

                String day =
                        row.get(0)
                                .trim();

                dayObject.put(
                        "day",
                        day
                );

                JSONArray slots =
                        new JSONArray();

                // -------------------------------------------------
                // Remaining columns = time slots
                // -------------------------------------------------

                for (int c = 1;
                     c < headers.size();
                     c++) {

                    String time =
                            headers.get(c)
                                    .trim();

                    String content = "";

                    if (c < row.size()) {

                        content =
                                row.get(c)
                                        .trim();
                    }

                    JSONObject slot =
                            parseTimetableCell(
                                    time,
                                    content
                            );

                    slots.put(
                            slot
                    );
                }

                dayObject.put(
                        "slots",
                        slots
                );

                schedule.put(
                        dayObject
                );
            }

            root.put(
                    "schedule",
                    schedule
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        return root;
    }


    // =========================================================
    // FILE TYPE
    // =========================================================

    private String detectFileType(
            String filename
    ) {

        if (filename == null) {

            return "UNKNOWN";
        }


        String name =
                filename.toLowerCase(
                        Locale.US
                );


        if (
                name.endsWith(".csv")
        ) {

            return "CSV";
        }


        if (
                name.endsWith(".xlsx")
        ) {

            return "XLSX";
        }


        if (
                name.endsWith(".xls")
        ) {

            return "XLS";
        }


        return "UNKNOWN";
    }


    // =========================================================
    // FILE NAME
    // =========================================================

    private String getFileName(
            Uri uri
    ) {

        String result =
                null;


        try (
                android.database.Cursor cursor =
                        getContentResolver()
                                .query(
                                        uri,
                                        null,
                                        null,
                                        null,
                                        null
                                )
        ) {

            if (
                    cursor != null
                            &&
                            cursor.moveToFirst()
            ) {

                int index =
                        cursor.getColumnIndex(
                                android.provider.OpenableColumns.DISPLAY_NAME
                        );


                if (
                        index >= 0
                ) {

                    result =
                            cursor.getString(
                                    index
                            );
                }
            }

        } catch (Exception ignored) {
        }


        if (
                result == null
        ) {

            result =
                    uri.getLastPathSegment();
        }


        return result != null
                ? result
                : "timetable";
    }


    // =========================================================
    // PRETTY JSON
    // =========================================================

    private String prettyJSON(
            JSONObject object
    ) {

        if (object == null) {

            return "No JSON generated.";
        }


        try {

            return object.toString(
                    4
            );

        } catch (Exception e) {

            return object.toString();
        }
    }


    // =========================================================
    // SAVE JSON
    // =========================================================

    private void saveJson(
            Uri uri
    ) {

        try {

            OutputStream output =
                    getContentResolver()
                            .openOutputStream(
                                    uri
                            );


            if (output == null) {

                throw new Exception(
                        "Could not open destination."
                );
            }


            output.write(
                    prettyJSON(
                            finalJson
                    )
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );


            output.close();


            Toast.makeText(
                    this,
                    "JSON exported successfully.",
                    Toast.LENGTH_LONG
            ).show();


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Export failed: " +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String message
    ) {

        runOnUiThread(
                () -> {

                    progressBar.setVisibility(
                            View.GONE
                    );


                    statusText.setText(
                            message
                    );


                    importButton.setEnabled(
                            true
                    );


                    Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();
    }
}