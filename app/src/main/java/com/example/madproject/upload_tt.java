package com.example.madproject;

import android.app.AlertDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class upload_tt extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private Button uploadButton;
    private Button importButton;

    private TextView fileNameText;
    private TextView selectedSheetText;
    private TextView facultyNameText;
    private TextView statusText;

    private TableLayout parsedTable;

    private ProgressBar progressBar;


    // =========================================================
    // FILE
    // =========================================================

    private Uri selectedFileUri;

    private String selectedFileName = "";

    private String selectedFileType = "";


    // =========================================================
    // WORKBOOK
    // =========================================================

    private Workbook workbook;

    private int selectedSheetIndex = -1;

    private String selectedSheetName = "";

    private String facultyName = "";


    // =========================================================
    // INTERNAL PARSED JSON
    // =========================================================

    private JSONObject finalJson;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;


    // =========================================================
    // THREAD
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


                        selectedSheetText.setText(
                                getString(
                                        R.string.no_sheet_selected
                                )
                        );


                        facultyNameText.setText(
                                getString(
                                        R.string.faculty_name_not_available
                                )
                        );


                        selectedSheetName = "";

                        facultyName = "";

                        finalJson = null;

                        parsedTable.removeAllViews();


                        if (
                                selectedFileType.equals("XLS")
                                        ||
                                        selectedFileType.equals("XLSX")
                                        ||
                                        selectedFileType.equals("CSV")
                        ) {

                            importButton.setEnabled(true);

                            statusText.setText(
                                    getString(
                                            R.string.ready_to_import
                                    )
                            );

                        } else {

                            importButton.setEnabled(false);

                            statusText.setText(
                                    getString(
                                            R.string.unsupported_file
                                    )
                            );
                        }
                    }
            );


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_upload_tt
        );

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        uploadButton =
                findViewById(
                        R.id.uploadButton
                );

        importButton =
                findViewById(
                        R.id.ocrButton
                );

        fileNameText =
                findViewById(
                        R.id.fileNameText
                );

        selectedSheetText =
                findViewById(
                        R.id.selectedSheetText
                );

        facultyNameText =
                findViewById(
                        R.id.facultyNameText
                );

        statusText =
                findViewById(
                        R.id.statusText
                );

        parsedTable =
                findViewById(
                        R.id.parsedTable
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );


        importButton.setEnabled(false);


        uploadButton.setOnClickListener(
                v -> filePicker.launch(
                        new String[]{
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        }
                )
        );


        importButton.setOnClickListener(
                v -> {

                    if (
                            selectedFileUri == null
                    ) {

                        Toast.makeText(
                                this,
                                getString(
                                        R.string.select_file_first
                                ),
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    importFile();
                }
        );
    }


    // =========================================================
    // IMPORT
    // =========================================================

    private void importFile() {

        importButton.setEnabled(false);

        progressBar.setVisibility(
                View.VISIBLE
        );


        statusText.setText(
                getString(
                        R.string.reading_timetable
                )
        );


        executor.execute(() -> {

            try {

                if (
                        selectedFileType.equals("CSV")
                ) {

                    showError(
                            getString(
                                    R.string.csv_requires_sheet
                            )
                    );

                    return;
                }


                loadWorkbook();


                runOnUiThread(
                        this::showSheetSelectionDialog
                );


            } catch (Exception e) {

                showError(
                        getString(
                                R.string.import_failed
                        )
                                + " "
                                + safeMessage(e)
                );
            }
        });
    }


    // =========================================================
    // LOAD WORKBOOK
    // =========================================================

    private void loadWorkbook()
            throws Exception {

        InputStream input =
                getContentResolver()
                        .openInputStream(
                                selectedFileUri
                        );


        if (input == null) {

            throw new Exception(
                    getString(
                            R.string.could_not_open_excel
                    )
            );
        }


        if (
                selectedFileType.equals("XLS")
        ) {

            workbook =
                    new HSSFWorkbook(input);

        } else {

            workbook =
                    new XSSFWorkbook(input);
        }


        input.close();


        if (
                workbook.getNumberOfSheets()
                        == 0
        ) {

            throw new Exception(
                    getString(
                            R.string.no_sheets_found
                    )
            );
        }
    }


    // =========================================================
    // SHEET SELECTOR
    // =========================================================

    private void showSheetSelectionDialog() {

        if (workbook == null) {

            showError(
                    getString(
                            R.string.no_workbook_loaded
                    )
            );

            return;
        }


        int sheetCount =
                workbook.getNumberOfSheets();


        String[] names =
                new String[sheetCount];


        for (
                int i = 0;
                i < sheetCount;
                i++
        ) {

            names[i] =
                    workbook
                            .getSheetAt(i)
                            .getSheetName();
        }


        new AlertDialog.Builder(this)
                .setTitle(
                        getString(
                                R.string.select_faculty_sheet
                        )
                )
                .setItems(
                        names,
                        (dialog, which) -> {

                            selectedSheetIndex =
                                    which;

                            selectedSheetName =
                                    names[which];


                            selectedSheetText.setText(
                                    selectedSheetName
                            );


                            executor.execute(() -> {

                                try {

                                    parseSelectedSheet();

                                    runOnUiThread(
                                            this::displayParsedTimetable
                                    );

                                } catch (Exception e) {

                                    showError(
                                            getString(
                                                    R.string.import_failed
                                            )
                                                    + " "
                                                    + safeMessage(e)
                                    );
                                }
                            });
                        }
                )
                .setNegativeButton(
                        android.R.string.cancel,
                        (dialog, which) -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            importButton.setEnabled(true);
                        }
                )
                .show();
    }


    // =========================================================
    // PARSE SELECTED SHEET
    // =========================================================

    private void parseSelectedSheet()
            throws Exception {

        Sheet sheet =
                workbook.getSheetAt(
                        selectedSheetIndex
                );


        facultyName =
                extractFacultyName(
                        sheet
                );


        int headerRow =
                findTimeHeaderRow(
                        sheet
                );


        if (
                headerRow == -1
        ) {

            throw new Exception(
                    getString(
                            R.string.time_header_not_found
                    )
            );
        }


        ArrayList<JSONObject> entries =
                parseSchedule(
                        sheet,
                        headerRow
                );


        JSONArray schedule =
                new JSONArray();


        for (
                JSONObject entry :
                entries
        ) {

            schedule.put(entry);
        }


        finalJson =
                new JSONObject();


        finalJson.put(
                "type",
                "timetable"
        );


        finalJson.put(
                "sourceFile",
                selectedFileName
        );


        finalJson.put(
                "facultySheet",
                selectedSheetName
        );


        finalJson.put(
                "facultyName",
                facultyName
        );


        finalJson.put(
                "schedule",
                schedule
        );
    }


    // =========================================================
    // FACULTY NAME
    // =========================================================

    private String extractFacultyName(
            Sheet sheet
    ) {

        DataFormatter formatter =
                new DataFormatter();


        for (
                int r = 0;
                r < Math.min(
                        5,
                        sheet.getLastRowNum() + 1
                );
                r++
        ) {

            Row row =
                    sheet.getRow(r);


            if (row == null) {
                continue;
            }


            for (
                    int c = 0;
                    c < Math.min(
                            row.getLastCellNum(),
                            5
                    );
                    c++
            ) {

                Cell cell =
                        row.getCell(c);


                if (cell == null) {
                    continue;
                }


                String value =
                        formatter
                                .formatCellValue(
                                        cell
                                )
                                .trim();


                if (
                        value.isEmpty()
                ) {

                    continue;
                }


                String lower =
                        value.toLowerCase(
                                Locale.US
                        );


                if (
                        lower.contains(
                                "time table"
                        )
                                ||
                                lower.contains(
                                        "academic"
                                )
                                ||
                                lower.contains(
                                        "wef"
                                )
                ) {

                    continue;
                }


                return value;
            }
        }


        return "";
    }


    // =========================================================
    // FIND TIME HEADER
    // =========================================================

    private int findTimeHeaderRow(
            Sheet sheet
    ) {

        DataFormatter formatter =
                new DataFormatter();


        for (
                int r = 0;
                r <= sheet.getLastRowNum();
                r++
        ) {

            Row row =
                    sheet.getRow(r);


            if (row == null) {
                continue;
            }


            int count = 0;


            for (
                    int c = 0;
                    c < row.getLastCellNum();
                    c++
            ) {

                Cell cell =
                        row.getCell(c);


                if (cell == null) {
                    continue;
                }


                String value =
                        formatter
                                .formatCellValue(
                                        cell
                                );


                if (
                        looksLikeTimeRange(
                                value
                        )
                ) {

                    count++;
                }
            }


            if (
                    count >= 2
            ) {

                return r;
            }
        }


        return -1;
    }


    // =========================================================
    // PARSE SCHEDULE
    // =========================================================

    private ArrayList<JSONObject> parseSchedule(
            Sheet sheet,
            int headerRow
    ) throws Exception {

        ArrayList<JSONObject> entries =
                new ArrayList<>();


        DataFormatter formatter =
                new DataFormatter();


        Row header =
                sheet.getRow(
                        headerRow
                );


        if (header == null) {
            return entries;
        }


        /*
         * Build time columns.
         *
         * Example:
         *
         * B = 9:00-10:00
         * C = 10:00-11:00
         * D = 11:00-11:10
         * E = 11:10-12:10
         *
         */

        ArrayList<TimeColumn> timeColumns =
                new ArrayList<>();


        for (
                int c = 0;
                c < header.getLastCellNum();
                c++
        ) {

            Cell cell =
                    header.getCell(c);


            if (cell == null) {
                continue;
            }


            String value =
                    formatter
                            .formatCellValue(
                                    cell
                            )
                            .trim();


            if (
                    !looksLikeTimeRange(
                            value
                    )
            ) {

                continue;
            }


            TimeRange range =
                    parseTimeRange(
                            value
                    );


            timeColumns.add(
                    new TimeColumn(
                            c,
                            range
                    )
            );
        }


        // =====================================================
        // DAY ROWS
        // =====================================================

        for (
                int r = headerRow + 1;
                r <= sheet.getLastRowNum();
                r++
        ) {

            Row row =
                    sheet.getRow(r);


            if (row == null) {
                continue;
            }


            String day =
                    formatter
                            .formatCellValue(
                                    row.getCell(0)
                            )
                            .trim();


            if (
                    !isDay(day)
            ) {

                continue;
            }


            // -------------------------------------------------
            // Avoid processing the same merged cell twice.
            // -------------------------------------------------

            Map<String, Boolean> processed =
                    new HashMap<>();


            for (
                    TimeColumn column :
                    timeColumns
            ) {

                int col =
                        column.column;


                String key =
                        r + ":" + col;


                if (
                        processed.containsKey(key)
                ) {

                    continue;
                }


                Cell actualCell =
                        row.getCell(col);


                if (actualCell == null) {
                    continue;
                }


                String rawText =
                        formatter
                                .formatCellValue(
                                        actualCell
                                )
                                .trim();


                if (
                        rawText.isEmpty()
                ) {

                    continue;
                }


                /*
                 * Find merged range containing this cell.
                 *
                 * If H5:I5 contains:
                 *
                 * L PH CEA
                 *
                 * this returns H5:I5.
                 */

                MergeInfo merge =
                        findMergedRange(
                                sheet,
                                r,
                                col
                        );


                int startColumn =
                        col;

                int endColumn =
                        col;


                if (
                        merge != null
                                &&
                                merge.firstRow == r
                ) {

                    startColumn =
                            merge.firstColumn;

                    endColumn =
                            merge.lastColumn;


                    for (
                            int rr =
                            merge.firstRow;
                            rr <= merge.lastRow;
                            rr++
                    ) {

                        for (
                                int cc =
                                merge.firstColumn;
                                cc <= merge.lastColumn;
                                cc++
                        ) {

                            processed.put(
                                    rr + ":" + cc,
                                    true
                            );
                        }
                    }
                }


                TimeRange time =
                        calculateMergedTime(
                                timeColumns,
                                startColumn,
                                endColumn
                        );


                JSONObject entry =
                        parseCell(
                                day,
                                time,
                                rawText
                        );


                if (
                        entry != null
                ) {

                    entries.add(entry);
                }
            }
        }


        return mergeConsecutiveLabs(
                entries
        );
    }


    // =========================================================
    // TIME COLUMN
    // =========================================================

    private static class TimeColumn {

        int column;

        TimeRange range;


        TimeColumn(
                int column,
                TimeRange range
        ) {

            this.column = column;

            this.range = range;
        }
    }


    // =========================================================
    // MERGED TIME
    // =========================================================

    private TimeRange calculateMergedTime(
            ArrayList<TimeColumn> columns,
            int firstColumn,
            int lastColumn
    ) {

        TimeRange first =
                null;

        TimeRange last =
                null;


        for (
                TimeColumn column :
                columns
        ) {

            if (
                    column.column >= firstColumn
                            &&
                            column.column <= lastColumn
            ) {

                if (
                        first == null
                ) {

                    first =
                            column.range;
                }


                last =
                        column.range;
            }
        }


        if (
                first == null
                        ||
                        last == null
        ) {

            return new TimeRange(
                    "",
                    ""
            );
        }


        return new TimeRange(
                first.start,
                last.end
        );
    }


    // =========================================================
    // MERGE INFO
    // =========================================================

    private static class MergeInfo {

        int firstRow;
        int lastRow;

        int firstColumn;
        int lastColumn;


        MergeInfo(
                int firstRow,
                int lastRow,
                int firstColumn,
                int lastColumn
        ) {

            this.firstRow = firstRow;
            this.lastRow = lastRow;

            this.firstColumn =
                    firstColumn;

            this.lastColumn =
                    lastColumn;
        }
    }


    // =========================================================
    // FIND MERGED RANGE
    // =========================================================

    private MergeInfo findMergedRange(
            Sheet sheet,
            int row,
            int column
    ) {

        for (
                org.apache.poi.ss.util.CellRangeAddress range :
                sheet.getMergedRegions()
        ) {

            if (
                    range.isInRange(
                            row,
                            column
                    )
            ) {

                return new MergeInfo(
                        range.getFirstRow(),
                        range.getLastRow(),
                        range.getFirstColumn(),
                        range.getLastColumn()
                );
            }
        }


        return null;
    }


    // =========================================================
    // PARSE CELL
    // =========================================================

    private JSONObject parseCell(
            String day,
            TimeRange columnTime,
            String rawText
    ) {

        try {

            String text =
                    normalizeSpaces(
                            rawText
                    );


            if (
                    text.isEmpty()
                            ||
                            text.equals("-")
            ) {

                return null;
            }


            JSONObject json =
                    new JSONObject();


            json.put(
                    "day",
                    normalizeDay(day)
            );


            json.put(
                    "rawText",
                    rawText
            );


            // =================================================
            // TIME
            // =================================================

            TimeRange cellTime =
                    extractCellTime(
                            text
                    );


            TimeRange finalTime =
                    cellTime != null
                            ? cellTime
                            : columnTime;


            json.put(
                    "startTime",
                    finalTime.start
            );


            json.put(
                    "endTime",
                    finalTime.end
            );


            // =================================================
            // BREAK
            // =================================================

            if (
                    isBreak(text)
            ) {

                json.put(
                        "subject",
                        text
                );

                json.put(
                        "type",
                        "BREAK"
                );

                return addNullAcademicFields(
                        json
                );
            }


            // =================================================
            // TYPE
            // =================================================

            boolean isLab =
                    containsLabMarker(
                            text
                    );


            json.put(
                    "type",
                    isLab
                            ? "LAB"
                            : "THEORY"
            );


            // =================================================
            // REMOVE TIME
            // =================================================

            String cleaned =
                    removeCellTime(
                            text
                    );


            // =================================================
            // REMOVE LAB MARKER
            // =================================================

            cleaned =
                    removeLabMarker(
                            cleaned
                    );


            // =================================================
            // ACADEMIC INFORMATION
            // =================================================

            CourseInfo course =
                    parseCourseInfo(
                            cleaned
                    );


            if (
                    course != null
            ) {

                json.put(
                        "program",
                        course.program
                );


                if (
                        course.semester > 0
                ) {

                    json.put(
                            "semester",
                            course.semester
                    );

                } else {

                    json.put(
                            "semester",
                            JSONObject.NULL
                    );
                }


                cleaned =
                        removeCourseToken(
                                cleaned,
                                course
                        );

            } else {

                json.put(
                        "program",
                        JSONObject.NULL
                );

                json.put(
                        "semester",
                        JSONObject.NULL
                );
            }


            // =================================================
            // COMBINED AUDIENCE
            // =================================================
            //
            // Example:
            // SUST 5AIDS AB_MT
            //
            // section      = AB
            // combinedWith = ["MT"]
            //
            // _MT must be removed before subject extraction so
            // the subject remains SUST rather than SUST_MT.
            // =================================================

            JSONArray combinedWith =
                    findCombinedWith(cleaned);

            json.put(
                    "combinedWith",
                    combinedWith
            );

            cleaned =
                    removeCombinedAudienceSuffix(
                            cleaned
                    );


            // =================================================
            // BATCH
            // =================================================

            String batch =
                    findBatch(
                            cleaned
                    );


            json.put(
                    "batch",
                    batch == null
                            ? JSONObject.NULL
                            : batch
            );


            // =================================================
            // SECTION
            // =================================================

            String section =
                    findSection(
                            cleaned,
                            batch
                    );


            json.put(
                    "section",
                    section == null
                            ? JSONObject.NULL
                            : section
            );


            cleaned =
                    removeBatchAndSection(
                            cleaned,
                            batch,
                            section
                    );


            // =================================================
            // ROOM
            // =================================================

            String room =
                    extractRoom(
                            cleaned
                    );


            json.put(
                    "room",
                    room == null
                            ? JSONObject.NULL
                            : room
            );


            // =================================================
            // SUBJECT
            // =================================================

            String subject =
                    cleanSubject(
                            cleaned
                    );


            json.put(
                    "subject",
                    subject.isEmpty()
                            ? JSONObject.NULL
                            : subject
            );


            return json;

        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // NULL ACADEMIC FIELDS
    // =========================================================

    private JSONObject addNullAcademicFields(
            JSONObject json
    ) throws Exception {

        json.put(
                "program",
                JSONObject.NULL
        );

        json.put(
                "semester",
                JSONObject.NULL
        );

        json.put(
                "section",
                JSONObject.NULL
        );

        json.put(
                "batch",
                JSONObject.NULL
        );

        json.put(
                "room",
                JSONObject.NULL
        );

        json.put(
                "combinedWith",
                new JSONArray()
        );


        return json;
    }


    // =========================================================
    // COURSE INFO
    // =========================================================

    private static class CourseInfo {

        String program;

        int semester;

        String token;


        CourseInfo(
                String program,
                int semester,
                String token
        ) {

            this.program =
                    program;

            this.semester =
                    semester;

            this.token =
                    token;
        }
    }


    // =========================================================
    // COURSE PARSER
    // =========================================================

    private CourseInfo parseCourseInfo(
            String text
    ) {

        /*
         * Recognised patterns:
         *
         * 5CE
         * 5 CE
         * CE 5
         * 5AIDS
         * AIDS 5
         * 5MT
         * MT 5
         * 5MT_AIDS
         * MT_AIDS
         * MBA IX
         * CE
         * AIDS
         * MT
         * CA
         * CEA
         */


        // -----------------------------------------------------
        // 5MT_AIDS
        // -----------------------------------------------------

        Pattern compound =
                Pattern.compile(
                        "\\b(\\d{1,2})\\s*"
                                +
                                "(MT[_\\s]*AIDS|AIDS[_\\s]*MT)"
                                +
                                "\\b",
                        Pattern.CASE_INSENSITIVE
                );


        Matcher m =
                compound.matcher(text);


        if (
                m.find()
        ) {

            return new CourseInfo(
                    "MT_AIDS",
                    Integer.parseInt(
                            m.group(1)
                    ),
                    m.group(0)
            );
        }


        // -----------------------------------------------------
        // NUMBER + PROGRAM
        // -----------------------------------------------------

        Pattern before =
                Pattern.compile(
                        "\\b(\\d{1,2})\\s*"
                                +
                                "(CE|AIDS|MT|MBA|CA|CEA)"
                                +
                                "\\b",
                        Pattern.CASE_INSENSITIVE
                );


        m =
                before.matcher(text);


        if (
                m.find()
        ) {

            return new CourseInfo(
                    m.group(2)
                            .toUpperCase(
                                    Locale.US
                            ),
                    Integer.parseInt(
                            m.group(1)
                    ),
                    m.group(0)
            );
        }


        // -----------------------------------------------------
        // PROGRAM + NUMBER
        // -----------------------------------------------------

        Pattern after =
                Pattern.compile(
                        "\\b"
                                +
                                "(CE|AIDS|MT|MBA|CA|CEA)"
                                +
                                "\\s*(\\d{1,2})"
                                +
                                "\\b",
                        Pattern.CASE_INSENSITIVE
                );


        m =
                after.matcher(text);


        if (
                m.find()
        ) {

            return new CourseInfo(
                    m.group(1)
                            .toUpperCase(
                                    Locale.US
                            ),
                    Integer.parseInt(
                            m.group(2)
                    ),
                    m.group(0)
            );
        }


        // -----------------------------------------------------
        // MBA ROMAN
        // -----------------------------------------------------

        Pattern mbaRoman =
                Pattern.compile(
                        "\\bMBA\\s*"
                                +
                                "(I|II|III|IV|V|VI|VII|VIII|IX|X)"
                                +
                                "\\b",
                        Pattern.CASE_INSENSITIVE
                );


        m =
                mbaRoman.matcher(text);


        if (
                m.find()
        ) {

            return new CourseInfo(
                    "MBA",
                    romanToNumber(
                            m.group(1)
                    ),
                    m.group(0)
            );
        }


        // -----------------------------------------------------
        // PROGRAM ONLY
        // -----------------------------------------------------

        Pattern programOnly =
                Pattern.compile(
                        "\\b(CE|AIDS|MT|MBA|CA|CEA)\\b",
                        Pattern.CASE_INSENSITIVE
                );


        m =
                programOnly.matcher(text);


        if (
                m.find()
        ) {

            return new CourseInfo(
                    m.group(1)
                            .toUpperCase(
                                    Locale.US
                            ),
                    -1,
                    m.group(0)
            );
        }


        return null;
    }

// =========================================================
// COMBINED AUDIENCE
// =========================================================

    private JSONArray findCombinedWith(String text) {

        JSONArray result = new JSONArray();

        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        // Examples supported:
        // AB_MT -> section AB + MBA Tech
        // A_MT  -> section A  + MBA Tech
        // B_MT  -> section B  + MBA Tech
        Pattern pattern = Pattern.compile(
                "(?i)(?<![A-Za-z0-9])(A|B|AB)_MT(?![A-Za-z0-9])"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            result.put("MT");
        }

        return result;
    }


    private String removeCombinedAudienceSuffix(String text) {

        if (text == null) {
            return "";
        }

        // SUST 5AIDS AB_MT -> SUST 5AIDS AB
        // Keep AB because it is the actual combined section.
        return normalizeSpaces(
                text.replaceAll(
                        "(?i)(?<![A-Za-z0-9])(A|B|AB)_MT(?![A-Za-z0-9])",
                        "$1"
                )
        );
    }


// =========================================================
// CLEAN SUBJECT
// =========================================================

    private String cleanSubject(String text) {

        if (text == null) {
            return "";
        }

        String result = normalizeSpaces(text);

        // Remove common separators left after parsing
        result = result
                .replace("|", " ")
                .replace(";", " ")
                .replace(",", " ");

        // Remove standalone room-like tokens for now.
        // Actual room parsing will be added once the
        // final room convention in the faculty timetable
        // is confirmed.
        result = result.replaceAll(
                "(?i)\\b(CR|CL|CC|LL)\\s*[-]?\\s*\\d+\\b",
                " "
        );

        // Remove standalone batch/section tokens
        result = result.replaceAll(
                "(?i)\\b[A-C][12]\\b",
                " "
        );

        result = result.replaceAll(
                "(?i)\\b[A-C]\\b",
                " "
        );

        return normalizeSpaces(result);
    }
    // =========================================================
    // REMOVE COURSE TOKEN
    // =========================================================

    private String removeCourseToken(
            String text,
            CourseInfo course
    ) {

        String result =
                text;


        if (
                course.token == null
                        ||
                        course.token.isEmpty()
        ) {

            return result;
        }


        result =
                result.replaceFirst(
                        "(?i)"
                                +
                                Pattern.quote(
                                        course.token
                                ),
                        " "
                );


        return normalizeSpaces(
                result
        );
    }


    // =========================================================
    // LAB MARKER
    // =========================================================

    private boolean containsLabMarker(
            String text
    ) {

        return Pattern.compile(
                        "(^|\\s)(L|LAB)(?=\\s|$)",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(text)
                .find();
    }


    private String removeLabMarker(
            String text
    ) {

        return normalizeSpaces(
                text.replaceAll(
                        "(?i)(^|\\s)(L|LAB)(?=\\s|$)",
                        " "
                )
        );
    }


    // =========================================================
    // BATCH
    // =========================================================

    private String findBatch(
            String text
    ) {

        Pattern pattern =
                Pattern.compile(
                        "(?<![A-Za-z0-9])"
                                +
                                "([ABCabc])"
                                +
                                "([12])"
                                +
                                "(?![A-Za-z0-9])"
                );


        Matcher matcher =
                pattern.matcher(text);


        if (
                matcher.find()
        ) {

            return matcher.group(1)
                    .toUpperCase(
                            Locale.US
                    )
                    +
                    matcher.group(2);
        }


        return null;
    }


    // =========================================================
    // SECTION
    // =========================================================

    private String findSection(
            String text,
            String batch
    ) {

        if (
                batch != null
        ) {

            return batch.substring(
                    0,
                    1
            );
        }


        Pattern pattern =
                Pattern.compile(
                        "(?<![A-Za-z0-9])"
                                +
                                "([ABCabc]{1,3})"
                                +
                                "(?![A-Za-z0-9])"
                );


        Matcher matcher =
                pattern.matcher(text);


        if (
                matcher.find()
        ) {

            return matcher.group(1)
                    .toUpperCase(
                            Locale.US
                    );
        }


        return null;
    }


    // =========================================================
    // REMOVE BATCH + SECTION
    // =========================================================

    private String removeBatchAndSection(
            String text,
            String batch,
            String section
    ) {

        String result =
                text;


        if (
                batch != null
        ) {

            result =
                    result.replaceAll(
                            "(?i)(?<![A-Za-z0-9])"
                                    +
                                    Pattern.quote(
                                            batch
                                    )
                                    +
                                    "(?![A-Za-z0-9])",
                            " "
                    );
        }


        if (
                section != null
                        &&
                        batch == null
        ) {

            result =
                    result.replaceAll(
                            "(?i)(?<![A-Za-z0-9])"
                                    +
                                    Pattern.quote(
                                            section
                                    )
                                    +
                                    "(?![A-Za-z0-9])",
                            " "
                    );
        }


        return normalizeSpaces(
                result
        );
    }


    // =========================================================
    // TIME RANGE
    // =========================================================

    private static class TimeRange {

        String start;

        String end;


        TimeRange(
                String start,
                String end
        ) {

            this.start = start;

            this.end = end;
        }
    }


    // =========================================================
    // PARSE TIME
    // =========================================================

    private TimeRange parseTimeRange(
            String value
    ) {

        String normalized =
                value
                        .replace(
                                "–",
                                "-"
                        )
                        .replace(
                                "—",
                                "-"
                        );


        Pattern pattern =
                Pattern.compile(
                        "(\\d{1,2}:\\d{2})\\s*"
                                +
                                "(am|pm)?\\s*"
                                +
                                "(?:to|-)\\s*"
                                +
                                "(\\d{1,2}:\\d{2})\\s*"
                                +
                                "(am|pm)?",
                        Pattern.CASE_INSENSITIVE
                );


        Matcher matcher =
                pattern.matcher(
                        normalized
                );


        if (
                !matcher.find()
        ) {

            return new TimeRange(
                    value,
                    ""
            );
        }


        String start =
                matcher.group(1);

        String startPeriod =
                matcher.group(2);


        String end =
                matcher.group(3);

        String endPeriod =
                matcher.group(4);


        if (
                startPeriod == null
        ) {

            startPeriod =
                    endPeriod;
        }


        if (
                endPeriod == null
        ) {

            endPeriod =
                    startPeriod;
        }


        if (
                startPeriod != null
        ) {

            start +=
                    " "
                            +
                            startPeriod
                                    .toLowerCase(
                                            Locale.US
                                    );
        }


        if (
                endPeriod != null
        ) {

            end +=
                    " "
                            +
                            endPeriod
                                    .toLowerCase(
                                            Locale.US
                                    );
        }


        return new TimeRange(
                start,
                end
        );
    }


    // =========================================================
    // CELL TIME OVERRIDE
    // =========================================================

    private TimeRange extractCellTime(
            String text
    ) {

        Pattern pattern =
                Pattern.compile(
                        "(\\d{1,2}(?::|\\.)\\d{1,2})\\s*"
                                +
                                "(am|pm)?\\s*"
                                +
                                "(?:to|-)\\s*"
                                +
                                "(\\d{1,2}(?::|\\.)\\d{1,2})\\s*"
                                +
                                "(am|pm)?",
                        Pattern.CASE_INSENSITIVE
                );


        Matcher matcher =
                pattern.matcher(text);


        if (
                !matcher.find()
        ) {

            return null;
        }


        String start =
                normalizeClockToken(
                        matcher.group(1)
                );


        String end =
                normalizeClockToken(
                        matcher.group(3)
                );


        String startPeriod =
                matcher.group(2);


        String endPeriod =
                matcher.group(4);


        if (
                startPeriod == null
        ) {

            startPeriod = "pm";
        }


        if (
                endPeriod == null
        ) {

            endPeriod =
                    startPeriod;
        }


        return new TimeRange(
                start
                        + " "
                        + startPeriod
                        .toLowerCase(
                                Locale.US
                        ),
                end
                        + " "
                        + endPeriod
                        .toLowerCase(
                                Locale.US
                        )
        );
    }


    // =========================================================
    // NORMALIZE CLOCK TOKEN
    // =========================================================

    private String normalizeClockToken(String value) {

        if (value == null) {
            return "";
        }

        String normalized =
                value.replace(".", ":");

        String[] parts =
                normalized.split(":");

        if (parts.length == 2 && parts[1].length() == 1) {
            return parts[0] + ":" + parts[1] + "0";
        }

        return normalized;
    }


    // =========================================================
    // REMOVE CELL TIME
    // =========================================================

    private String removeCellTime(
            String text
    ) {

        return normalizeSpaces(
                text.replaceAll(
                        "(?i)"
                                +
                                "\\d{1,2}(?::|\\.)\\d{1,2}"
                                +
                                "\\s*(am|pm)?"
                                +
                                "\\s*(?:to|-)"
                                +
                                "\\s*"
                                +
                                "\\d{1,2}(?::|\\.)\\d{1,2}"
                                +
                                "\\s*(am|pm)?",
                        " "
                )
        );
    }


    // =========================================================
    // ROOM
    // =========================================================

    private String extractRoom(
            String text
    ) {

        /*
         * Room syntax is deliberately not guessed yet.
         *
         * Example:
         * 316
         * CR 316
         * CL 206
         *
         * will be supported once the final room convention
         * is confirmed.
         */

        return null;
    }


    // =========================================================
    // MERGE CONSECUTIVE LABS
    // =========================================================

    private ArrayList<JSONObject> mergeConsecutiveLabs(
            ArrayList<JSONObject> entries
    ) {

        ArrayList<JSONObject> result =
                new ArrayList<>();


        for (
                JSONObject current :
                entries
        ) {

            if (
                    result.isEmpty()
            ) {

                result.add(current);

                continue;
            }


            JSONObject previous =
                    result.get(
                            result.size() - 1
                    );


            if (
                    canMergeLabs(
                            previous,
                            current
                    )
            ) {

                try {

                    previous.put(
                            "endTime",
                            current.optString(
                                    "endTime"
                            )
                    );

                } catch (Exception ignored) {
                }

            } else {

                result.add(current);
            }
        }


        return result;
    }


    // =========================================================
    // CAN MERGE LABS
    // =========================================================

    private boolean canMergeLabs(
            JSONObject first,
            JSONObject second
    ) {

        if (
                !first.optString(
                                "type"
                        )
                        .equals("LAB")
        ) {

            return false;
        }


        if (
                !second.optString(
                                "type"
                        )
                        .equals("LAB")
        ) {

            return false;
        }


        String[] fields = {
                "day",
                "subject",
                "program",
                "section",
                "batch",
                "combinedWith"
        };


        for (
                String field :
                fields
        ) {

            if (
                    !first.optString(
                                    field
                            )
                            .equalsIgnoreCase(
                                    second.optString(
                                            field
                                    )
                            )
            ) {

                return false;
            }
        }


        if (
                first.optInt(
                        "semester",
                        -1
                )
                        !=
                        second.optInt(
                                "semester",
                                -1
                        )
        ) {

            return false;
        }


        return normalizeTime(
                first.optString(
                        "endTime"
                )
        )
                .equals(
                        normalizeTime(
                                second.optString(
                                        "startTime"
                                )
                        )
                );
    }


    // =========================================================
    // DISPLAY PARSED TIMETABLE
    // =========================================================

    private void displayParsedTimetable() {

        progressBar.setVisibility(
                View.GONE
        );


        parsedTable.removeAllViews();


        if (
                finalJson == null
        ) {

            showError(
                    getString(
                            R.string.parsing_failed
                    )
            );

            return;
        }


        try {

            facultyNameText.setText(
                    facultyName.isEmpty()
                            ? getString(
                            R.string.faculty_name_not_available
                    )
                            : facultyName
            );


            JSONArray schedule =
                    finalJson.getJSONArray(
                            "schedule"
                    );


            addPreviewHeader();


            for (
                    int i = 0;
                    i < schedule.length();
                    i++
            ) {

                JSONObject entry =
                        schedule.getJSONObject(i);


                addPreviewRow(
                        entry
                );
            }


            statusText.setText(
                    getString(
                            R.string.timetable_imported
                    )
                            + " "
                            + schedule.length()
                            + " "
                            + getString(
                            R.string.schedule_entries
                    )
            );


            saveParsedTimetableToFirestore();


        } catch (Exception e) {

            showError(
                    getString(
                            R.string.parsing_failed
                    )
            );
        }
    }


    // =========================================================
    // PREVIEW HEADER
    // =========================================================

    private void addPreviewHeader() {

        TableRow row =
                new TableRow(this);


        String[] headers = {
                "Day",
                "Start",
                "End",
                "Subject",
                "Type",
                "Program",
                "Sem",
                "Section",
                "Batch",
                "Combined",
                "Room"
        };


        for (
                String header :
                headers
        ) {

            row.addView(
                    createPreviewCell(
                            header,
                            true
                    )
            );
        }


        parsedTable.addView(row);
    }


    // =========================================================
    // PREVIEW ROW
    // =========================================================

    private void addPreviewRow(
            JSONObject entry
    ) {

        TableRow row =
                new TableRow(this);


        String[] values = {
                entry.optString(
                        "day",
                        "-"
                ),

                entry.optString(
                        "startTime",
                        "-"
                ),

                entry.optString(
                        "endTime",
                        "-"
                ),

                displayNull(
                        entry,
                        "subject"
                ),

                entry.optString(
                        "type",
                        "-"
                ),

                displayNull(
                        entry,
                        "program"
                ),

                displaySemester(
                        entry
                ),

                displayNull(
                        entry,
                        "section"
                ),

                displayNull(
                        entry,
                        "batch"
                ),

                displayCombinedWith(
                        entry
                ),

                displayNull(
                        entry,
                        "room"
                )
        };


        for (
                String value :
                values
        ) {

            row.addView(
                    createPreviewCell(
                            value,
                            false
                    )
            );
        }


        parsedTable.addView(row);
    }


    // =========================================================
    // PREVIEW CELL
    // =========================================================

    private TextView createPreviewCell(
            String text,
            boolean header
    ) {

        TextView cell =
                new TextView(this);


        cell.setText(
                text == null
                        ||
                        text.trim().isEmpty()
                        ? "-"
                        : text
        );


        cell.setTextSize(
                header ? 13 : 12
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
                14,
                18,
                14
        );


        cell.setMinWidth(
                header
                        ? 130
                        : 120
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


        cell.setLayoutParams(params);


        return cell;
    }


    // =========================================================
    // DISPLAY COMBINED AUDIENCE
    // =========================================================

    private String displayCombinedWith(JSONObject object) {

        JSONArray values =
                object.optJSONArray(
                        "combinedWith"
                );

        if (values == null || values.length() == 0) {
            return "-";
        }

        ArrayList<String> items = new ArrayList<>();

        for (int i = 0; i < values.length(); i++) {
            String value = values.optString(i, "");
            if (!value.isEmpty()) {
                items.add(value);
            }
        }

        return items.isEmpty()
                ? "-"
                : android.text.TextUtils.join(", ", items);
    }


    // =========================================================
    // DISPLAY NULL
    // =========================================================

    private String displayNull(
            JSONObject object,
            String key
    ) {

        if (
                object.isNull(key)
        ) {

            return "-";
        }


        String value =
                object.optString(
                        key,
                        ""
                );


        return value.isEmpty()
                ? "-"
                : value;
    }


    // =========================================================
    // DISPLAY SEMESTER
    // =========================================================

    private String displaySemester(
            JSONObject object
    ) {

        if (
                object.isNull("semester")
        ) {

            return "-";
        }


        return String.valueOf(
                object.optInt(
                        "semester",
                        -1
                )
        );
    }


    // =========================================================
    // NORMALIZE SPACES
    // =========================================================

    private String normalizeSpaces(
            String value
    ) {

        if (
                value == null
        ) {

            return "";
        }


        return value
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }


    // =========================================================
    // BREAK
    // =========================================================

    private boolean isBreak(
            String text
    ) {

        String upper =
                text
                        .trim()
                        .toUpperCase(
                                Locale.US
                        );


        return upper.equals("BREAK")
                ||
                upper.equals("SHORT BREAK")
                ||
                upper.equals("LUNCH");
    }


    // =========================================================
    // DAY
    // =========================================================

    private boolean isDay(
            String value
    ) {

        if (
                value == null
        ) {

            return false;
        }


        String day =
                value
                        .trim()
                        .toLowerCase(
                                Locale.US
                        );


        return day.equals("mon")
                ||
                day.equals("monday")
                ||
                day.equals("tue")
                ||
                day.equals("tues")
                ||
                day.equals("tuesday")
                ||
                day.equals("wed")
                ||
                day.equals("wednesday")
                ||
                day.equals("thu")
                ||
                day.equals("thur")
                ||
                day.equals("thurs")
                ||
                day.equals("thus")
                ||
                day.equals("thursday")
                ||
                day.equals("fri")
                ||
                day.equals("friday")
                ||
                day.equals("sat")
                ||
                day.equals("saturday");
    }


    // =========================================================
    // NORMALIZE DAY
    // =========================================================

    private String normalizeDay(
            String value
    ) {

        String day =
                value
                        .trim()
                        .toLowerCase(
                                Locale.US
                        );


        if (
                day.startsWith("mon")
        ) {

            return "Monday";
        }


        if (
                day.startsWith("tue")
        ) {

            return "Tuesday";
        }


        if (
                day.startsWith("wed")
        ) {

            return "Wednesday";
        }


        if (
                day.startsWith("thu")
                        ||
                        day.startsWith("thus")
        ) {

            return "Thursday";
        }


        if (
                day.startsWith("fri")
        ) {

            return "Friday";
        }


        if (
                day.startsWith("sat")
        ) {

            return "Saturday";
        }


        return value;
    }


    // =========================================================
    // TIME HEADER DETECTION
    // =========================================================

    private boolean looksLikeTimeRange(
            String value
    ) {

        if (
                value == null
        ) {

            return false;
        }


        return value.matches(
                "(?i).*\\d{1,2}:\\d{2}.*"
                        +
                        "(?:to|-|–|—)"
                        +
                        ".*\\d{1,2}:\\d{2}.*"
        );
    }


    // =========================================================
    // NORMALIZE TIME
    // =========================================================

    private String normalizeTime(
            String value
    ) {

        if (
                value == null
        ) {

            return "";
        }


        return value
                .toLowerCase(
                        Locale.US
                )
                .replaceAll(
                        "\\s+",
                        ""
                );
    }


    // =========================================================
    // ROMAN
    // =========================================================

    private int romanToNumber(
            String roman
    ) {

        if (
                roman == null
        ) {

            return -1;
        }


        String value =
                roman.toUpperCase(
                        Locale.US
                );


        int result = 0;

        int previous = 0;


        for (
                int i =
                value.length() - 1;
                i >= 0;
                i--
        ) {

            int current;


            switch (
                    value.charAt(i)
            ) {

                case 'I':
                    current = 1;
                    break;

                case 'V':
                    current = 5;
                    break;

                case 'X':
                    current = 10;
                    break;

                default:
                    return -1;
            }


            if (
                    current < previous
            ) {

                result -= current;

            } else {

                result += current;

                previous = current;
            }
        }


        return result;
    }


    // =========================================================
    // FILE TYPE
    // =========================================================

    private String detectFileType(
            String filename
    ) {

        if (
                filename == null
        ) {

            return "UNKNOWN";
        }


        String name =
                filename.toLowerCase(
                        Locale.US
                );


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


        if (
                name.endsWith(".csv")
        ) {

            return "CSV";
        }


        return "UNKNOWN";
    }


    // =========================================================
    // FILE NAME
    // =========================================================

    private String getFileName(
            Uri uri
    ) {

        String result = null;


        try (
                Cursor cursor =
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
                                OpenableColumns.DISPLAY_NAME
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


        return result == null
                ? "timetable"
                : result;
    }


    // =========================================================
    // FIRESTORE SAVE
    // =========================================================

    private void saveParsedTimetableToFirestore() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {

            showError(
                    getString(
                            R.string.user_not_authenticated
                    )
            );

            return;
        }

        if (finalJson == null) {

            showError(
                    getString(
                            R.string.parsing_failed
                    )
            );

            return;
        }

        final String teacherUid =
                user.getUid();

        statusText.setText(
                getString(
                        R.string.saving_timetable
                )
        );

        firestore
                .collection("teachers")
                .document(teacherUid)
                .collection("schedule")
                .get()
                .addOnSuccessListener(snapshot -> {

                    try {

                        JSONArray schedule =
                                finalJson.getJSONArray(
                                        "schedule"
                                );

                        /*
                         * A timetable is normally far below Firestore's
                         * 500-operation batch limit. We reserve a few
                         * operations for metadata and fail explicitly if
                         * an unexpectedly huge sheet is supplied.
                         */
                        int operationCount =
                                snapshot.size()
                                        + schedule.length()
                                        + 1;

                        if (operationCount > 450) {

                            showError(
                                    getString(
                                            R.string.timetable_too_large
                                    )
                            );

                            return;
                        }

                        WriteBatch batch =
                                firestore.batch();

                        // Replace the teacher's previous imported schedule.
                        for (
                                DocumentSnapshot document :
                                snapshot.getDocuments()
                        ) {

                            batch.delete(
                                    document.getReference()
                            );
                        }

                        // Write the freshly parsed schedule.
                        for (
                                int i = 0;
                                i < schedule.length();
                                i++
                        ) {

                            JSONObject entry =
                                    schedule.getJSONObject(i);

                            Map<String, Object> data =
                                    jsonEntryToFirestoreMap(
                                            entry
                                    );

                            data.put(
                                    "teacherId",
                                    teacherUid
                            );

                            data.put(
                                    "facultySheet",
                                    selectedSheetName
                            );

                            data.put(
                                    "facultyName",
                                    facultyName
                            );

                            data.put(
                                    "sourceFile",
                                    selectedFileName
                            );

                            data.put(
                                    "importedAt",
                                    FieldValue.serverTimestamp()
                            );

                            batch.set(
                                    firestore
                                            .collection("teachers")
                                            .document(teacherUid)
                                            .collection("schedule")
                                            .document(),
                                    data
                            );
                        }

                        // Store import metadata on the teacher document.
                        Map<String, Object> metadata =
                                new HashMap<>();

                        metadata.put(
                                "lastTimetableImport",
                                FieldValue.serverTimestamp()
                        );

                        metadata.put(
                                "timetableSourceFile",
                                selectedFileName
                        );

                        metadata.put(
                                "timetableFacultySheet",
                                selectedSheetName
                        );

                        metadata.put(
                                "timetableFacultyName",
                                facultyName
                        );

                        metadata.put(
                                "scheduleEntryCount",
                                schedule.length()
                        );

                        batch.set(
                                firestore
                                        .collection("teachers")
                                        .document(teacherUid),
                                metadata,
                                com.google.firebase.firestore.SetOptions.merge()
                        );

                        batch.commit()
                                .addOnSuccessListener(unused -> {

                                    progressBar.setVisibility(
                                            View.GONE
                                    );

                                    importButton.setEnabled(
                                            true
                                    );

                                    statusText.setText(
                                            getString(
                                                    R.string.timetable_saved_to_firestore,
                                                    schedule.length()
                                            )
                                    );

                                    Toast.makeText(
                                            this,
                                            getString(
                                                    R.string.timetable_saved_successfully
                                            ),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                })
                                .addOnFailureListener(e ->

                                        showError(
                                                getString(
                                                        R.string.firestore_save_failed
                                                )
                                                        + " "
                                                        + safeMessage(
                                                        e instanceof Exception
                                                                ? (Exception) e
                                                                : new Exception(e)
                                                )
                                        )
                                );

                    } catch (Exception e) {

                        showError(
                                getString(
                                        R.string.firestore_save_failed
                                )
                                        + " "
                                        + safeMessage(e)
                        );
                    }
                })
                .addOnFailureListener(e ->

                        showError(
                                getString(
                                        R.string.firestore_read_failed
                                )
                                        + " "
                                        + safeMessage(
                                        e instanceof Exception
                                                ? (Exception) e
                                                : new Exception(e)
                                )
                        )
                );
    }


    // =========================================================
    // JSON -> FIRESTORE MAP
    // =========================================================

    private Map<String, Object> jsonEntryToFirestoreMap(
            JSONObject entry
    ) throws Exception {

        Map<String, Object> data =
                new HashMap<>();

        putJsonValue(
                data,
                entry,
                "day"
        );

        putJsonValue(
                data,
                entry,
                "startTime"
        );

        putJsonValue(
                data,
                entry,
                "endTime"
        );

        putJsonValue(
                data,
                entry,
                "subject"
        );

        putJsonValue(
                data,
                entry,
                "type"
        );

        putJsonValue(
                data,
                entry,
                "program"
        );

        putJsonValue(
                data,
                entry,
                "semester"
        );

        putJsonValue(
                data,
                entry,
                "section"
        );

        putJsonValue(
                data,
                entry,
                "batch"
        );

        putJsonValue(
                data,
                entry,
                "room"
        );

        putJsonValue(
                data,
                entry,
                "rawText"
        );

        JSONArray combined =
                entry.optJSONArray(
                        "combinedWith"
                );

        ArrayList<String> combinedWith =
                new ArrayList<>();

        if (combined != null) {

            for (
                    int i = 0;
                    i < combined.length();
                    i++
            ) {

                String value =
                        combined.optString(
                                i,
                                ""
                        );

                if (!value.isEmpty()) {
                    combinedWith.add(value);
                }
            }
        }

        data.put(
                "combinedWith",
                combinedWith
        );

        return data;
    }


    private void putJsonValue(
            Map<String, Object> destination,
            JSONObject source,
            String key
    ) throws Exception {

        if (
                !source.has(key)
                        ||
                        source.isNull(key)
        ) {

            destination.put(
                    key,
                    null
            );

            return;
        }

        Object value =
                source.get(key);

        destination.put(
                key,
                value
        );
    }


    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String message
    ) {

        runOnUiThread(() -> {

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
        });
    }


    // =========================================================
    // SAFE ERROR
    // =========================================================

    private String safeMessage(
            Exception e
    ) {

        if (
                e.getMessage() == null
        ) {

            return getString(
                    R.string.unknown_error
            );
        }


        return e.getMessage();
    }


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (
                workbook != null
        ) {

            try {

                workbook.close();

            } catch (Exception ignored) {
            }

            workbook = null;
        }


        executor.shutdown();


        super.onDestroy();
    }
}