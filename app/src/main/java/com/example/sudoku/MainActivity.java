package com.example.sudoku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private int[][] grid = new int[9][9];
    private int[][] solutionGrid = new int[9][9]; // Store the solution for comparison
    private int attempts = 0; // Track the number of attempts made by the user in solve button
    private int attemptt = 8; // Number of attempts remaining for number clicking
    private TextView timerText;
    private Button playButton, pauseButton, restartButton;
    private TextView attemptsText;
    private boolean timerRunning = false; // Flag to track if the timer is running

    private long timeElapsedInMillis = 0; // Elapsed time in milliseconds
    private Handler handler; // Handler to update timer display

    // Declare a boolean variable to track if the dialog is shown
    private boolean isDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize views
        timerText = findViewById(R.id.timerText);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        restartButton = findViewById(R.id.restartButton);

        // Initialize handler
        handler = new Handler(Looper.getMainLooper());

        // Set up buttons click listeners
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartTimer();
            }
        });

        // Retrieve the difficulty level passed from LevelActivity
        String difficulty = getIntent().getStringExtra("difficulty");
        attemptsText = findViewById(R.id.attemptsText);

        // Set the level text based on the selected difficulty
        TextView levelText = findViewById(R.id.levelText);
        levelText.setText(difficulty + " Level");

        // Adjust the board layout based on the selected difficulty
        GridLayout board = findViewById(R.id.board);
        int rowCount = 9;
        int columnCount = 9;

        // Generate a random solution for the Sudoku puzzle
        generateRandomSolution();

        Button backButton = findViewById(R.id.but_back);
        backButton.setVisibility(View.GONE);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                EditText editText = new EditText(this);

                // Set input type to allow only one digit
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

                // Set background drawable
                editText.setBackgroundResource(R.drawable.edittext_bg);
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);

                // Set input type to null to disable soft keyboard
                editText.setInputType(InputType.TYPE_NULL);
                // Set layout parameters to match constraints with weight
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.width = 0;
                layoutParams.height = 0;
                layoutParams.columnSpec = GridLayout.spec(j, 1f);
                layoutParams.rowSpec = GridLayout.spec(i, 1f);
                editText.setLayoutParams(layoutParams);

                // Set gravity to center
                editText.setGravity(Gravity.CENTER);

                // Hide cursor
                editText.setCursorVisible(false);

                // Set text size
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
                editText.setTextColor(Color.WHITE);
                // Add EditText to the GridLayout
                board.addView(editText);

            }
        }

        fillNumbersBasedOnDifficulty(difficulty);
        attachOnClickListenerToCells();
    }

    private void attachOnClickListenerToCells() {
        GridLayout board = findViewById(R.id.board);
        final int rowCount = board.getRowCount(); // Get the number of rows
        final int columnCount = board.getColumnCount(); // Get the number of columns

        for (int i = 0; i < board.getChildCount(); i++) {
            final EditText editText = (EditText) board.getChildAt(i);
            final int index = i; // Store the index of the current EditText

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        // Set background color to yellow with alpha 0.4 when focused
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setColor(Color.argb(162, 255, 255, 30)); // Yellow with alpha 0.4
                        drawable.setStroke(2, Color.WHITE); // White border
                        editText.setBackground(drawable);

                        // Calculate row and column indices using the stored index
                        int row = index / columnCount;
                        int col = index % columnCount;

                        // Highlight the row
                        for (int j = 0; j < columnCount; j++) {
                            EditText rowEditText = (EditText) board.getChildAt(row * columnCount + j);
                            rowEditText.setBackgroundColor(Color.argb(20, 255, 249, 222));
                        }

                        // Highlight the column
                        for (int j = 0; j < rowCount; j++) {
                            EditText colEditText = (EditText) board.getChildAt(j * columnCount + col);
                            colEditText.setBackgroundColor(Color.argb(20, 255, 249, 222));
                        }
                    } else {
                        // Reset background color to default when focus lost
                        editText.setBackgroundResource(R.drawable.edittext_bg);

                        // Calculate row and column indices using the stored index
                        int row = index / columnCount;
                        int col = index % columnCount;

                        // Reset row background color
                        for (int j = 0; j < columnCount; j++) {
                            EditText rowEditText = (EditText) board.getChildAt(row * columnCount + j);
                            rowEditText.setBackgroundResource(R.drawable.edittext_bg);
                        }

                        // Reset column background color
                        for (int j = 0; j < rowCount; j++) {
                            EditText colEditText = (EditText) board.getChildAt(j * columnCount + col);
                            colEditText.setBackgroundResource(R.drawable.edittext_bg);
                        }
                    }
                }
            });
        }
    }

    // Method to generate a random solution for the Sudoku puzzle
    private void generateRandomSolution() {
        solveSudoku(solutionGrid);
    }

    // Method to solve the Sudoku puzzle
    private boolean solveSudoku(int[][] grid) {
        // Shuffle the order in which we try digits 1 through 9
        List<Integer> digits = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            digits.add(i);
        }
        Collections.shuffle(digits);

        // Find an empty cell
        int row = -1;
        int col = -1;
        boolean isEmpty = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (grid[i][j] == 0) {
                    row = i;
                    col = j;
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                break;
            }
        }

        // If there are no empty cells, puzzle is solved
        if (isEmpty) {
            return true;
        }

        // Try digits 1 through 9 in random order
        for (int num : digits) {
            if (isValidPlacement(grid, row, col, num)) {
                // Place the number if it's valid
                grid[row][col] = num;
                // Recursively try to solve the rest of the puzzle
                if (solveSudoku(grid)) {
                    return true;
                }
                // If placing the number doesn't lead to a solution, backtrack
                grid[row][col] = 0;
            }
        }

        // No solution found
        return false;
    }

    // Method to check if a number can be placed in a given position
    private boolean isValidPlacement(int[][] grid, int row, int col, int num) {
        // Check if num is not already in the row
        for (int x = 0; x < 9; x++) {
            if (grid[row][x] == num) {
                return false;
            }
        }
        // Check if num is not already in the column
        for (int x = 0; x < 9; x++) {
            if (grid[x][col] == num) {
                return false;
            }
        }
        // Check if num is not already in the 3x3 grid
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i + startRow][j + startCol] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    // Method to fill numbers in the Sudoku grid based on the difficulty level
    private void fillNumbersBasedOnDifficulty(String difficulty) {
        // Calculate the number of cells to fill based on difficulty
        int cellsToFill;
        switch (difficulty) {
            case "Easy":
                cellsToFill = 45;
                break;
            case "Normal":
                cellsToFill = 30;
                break;
            case "Hard":
                cellsToFill = 15;
                break;
            default:
                cellsToFill = 40; // Default to normal difficulty
                break;
        }

        // Reset the grid to all zeros
        resetGrid();

        // Fill the specified percentage of cells with numbers from the solution
        int filledCells = 0;
        while (filledCells < cellsToFill) {
            int row = (int) (Math.random() * 9);
            int col = (int) (Math.random() * 9);
            if (grid[row][col] == 0) {
                grid[row][col] = solutionGrid[row][col]; // Fill with number from solution
                filledCells++;
            }
        }

        // Update the UI to reflect the filled cells
        updateGridUI(grid);
    }

    // Method to reset the grid to all zeros
    private void resetGrid() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                grid[i][j] = 0;
            }
        }
    }

    public void showSolutionButton(View v) {
        Button showButton = findViewById(R.id.ShowSolutionButton);
        Button backButton = findViewById(R.id.but_back);
        Button solveButton = findViewById(R.id.but_check);
        backButton.setVisibility(View.VISIBLE);
        showButton.setVisibility(View.GONE);
        solveButton.setVisibility(View.GONE);
        showSolution();
    }

    public void onBackButtonClick(View view) {
        // Go back to LevelActivity
        onBackPressed();
    }
    private void disableTimerButtons() {
        playButton.setEnabled(false);
        pauseButton.setEnabled(false);
        restartButton.setEnabled(false);

    }
    // Method to show the solution in the Sudoku grid
    private void showSolution() {
        // Update the grid with the solution
        updateGridUI(solutionGrid);
        // Disable editing for all cells
        disableEditing();
        // Pause the timer
        pauseTimer();
        // Disable timer buttons
        disableTimerButtons();
    }

    // Method to disable editing for all cells in the Sudoku grid
    private void disableEditing() {
        GridLayout board = findViewById(R.id.board);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                EditText editText = (EditText) board.getChildAt(i * 9 + j);
                if (editText != null) {
                    editText.setEnabled(false);
                }
            }
        }
    }

    // Method to update the UI with the current state of the grid
    private void updateGridUI(int[][] grid) {
        // Your implementation to update the UI goes here
        GridLayout board = findViewById(R.id.board);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                EditText editText = (EditText) board.getChildAt(i * 9 + j);
                if (editText != null) {
                    if (grid[i][j] != 0) {
                        editText.setText(String.valueOf(grid[i][j]));
                        editText.setTextColor(Color.GRAY);
                        editText.setEnabled(false); // Make it not editable
                    } else {
                        editText.setText("");
                        editText.setTextColor(Color.BLACK);
                        editText.setEnabled(true);
                    }
                }
            }
        }
    }

    // Method to check if a string represents a valid Sudoku number (1 to 9)
    private boolean isValidNumber(String number) {
        return number.matches("[1-9]");
    }

    public void onNumberButtonClick(View view) {

        Button clickedButton = (Button) view;
        String number = clickedButton.getText().toString();
        EditText focusedEditText = (EditText) getCurrentFocus();
        // Start the timer if it's not running and the dialog is not shown
        if (!timerRunning && isValidNumber(number) && !isDialogShown) {
            startTimer();
        } else if (timerRunning && isValidNumber(number) && !isDialogShown) {
            // Continue the timer if it's already running and the dialog is not shown
            updateTimer();
        }

        if (focusedEditText != null && isValidNumber(number)) {
            int row = -1;
            int col = -1;
            GridLayout board = findViewById(R.id.board);
            // Find the position of the focused EditText
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    EditText editText = (EditText) board.getChildAt(i * 9 + j);
                    if (editText == focusedEditText) {
                        row = i;
                        col = j;
                        break;
                    }
                }
                if (row != -1 && col != -1) {
                    break;
                }
            }
            if (row != -1 && col != -1) {
                // Check if the cell is empty or already contains the same number
                if (focusedEditText.getText().toString().isEmpty() || !focusedEditText.getText().toString().equals(number)) {
                    // Check if the entered number is valid in the row
                    boolean validInRow = true;
                    for (int j = 0; j < 9; j++) {
                        EditText editText = (EditText) board.getChildAt(row * 9 + j);
                        if (editText != null && !editText.getText().toString().isEmpty() && editText.getText().toString().equals(number)) {
                            validInRow = false;
                            break;
                        }
                    }
                    // Check if the entered number is valid in the column
                    boolean validInColumn = true;
                    for (int i = 0; i < 9; i++) {
                        EditText editText = (EditText) board.getChildAt(i * 9 + col);
                        if (editText != null && !editText.getText().toString().isEmpty() && editText.getText().toString().equals(number)) {
                            validInColumn = false;
                            break;
                        }
                    }
                    if (validInRow && validInColumn) {
                        // If the entered number is valid in both row and column, set it and update UI
                        focusedEditText.setText(number);
                        focusedEditText.setTextColor(Color.WHITE);
                        checkGameStatus(); // Check if the game is won
                    } else {
                        // If the entered number is invalid, display it in red and decrement attempts
                        focusedEditText.setText(number);
                        focusedEditText.setTextColor(Color.RED);

                        attemptt--;
                        // Check if attempts reached zero
                        if (attemptt < 0) {

                            // Hide buttons
                            Button solveButton = findViewById(R.id.but_check);
                            Button back = findViewById(R.id.but_back);
                            Button showSolutionButton = findViewById(R.id.ShowSolutionButton);
                            solveButton.setVisibility(View.GONE);
                            showSolutionButton.setVisibility(View.GONE);
                            back.setVisibility(View.VISIBLE);
                            // Disable all buttons
                            clickedButton.setEnabled(false);
                            // Show solution
                            showSolution();
                            // Display dialog indicating loss
                            isDialogShown = true;
                            showDialog("Game Over", "You've run out of attempts. Better luck next time!");
                        } else {
                            // Update attempts display
                            updateAttemptsDisplay();
                        }
                    }
                }
            }
        }
    }

    // Method to check if the game is won
    private void checkGameStatus() {
        int successPercentage = checkSuccessPercentage();
        if (successPercentage == 100) {
            // Show congratulations dialog
            isDialogShown = true;
            showDialog("Congratulations!", "You have successfully completed the Sudoku puzzle!");
        }
    }

    // Method to check the success percentage
    private int checkSuccessPercentage() {
        int correctCount = 0;
        int totalCount = 0;
        GridLayout board = findViewById(R.id.board);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                EditText editText = (EditText) board.getChildAt(i * 9 + j);
                if (editText != null && editText.getText().length() > 0) {
                    String enteredValue = editText.getText().toString();
                    totalCount++;
                    if (enteredValue.equals(String.valueOf(solutionGrid[i][j]))) {
                        correctCount++;
                    }
                }
            }
        }

        int successPercentage = (int) ((double) correctCount / 81 * 100);
        return successPercentage;
    }

    public void onSolveButtonClick(View view) {
        attempts++;
        if (attempts > 3) {
            Button solveButton = findViewById(R.id.but_check);
            Button back = findViewById(R.id.but_back);
            Button showSolutionButton = findViewById(R.id.ShowSolutionButton);
            solveButton.setVisibility(View.GONE);
            showSolutionButton.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
            // Show solution
            showSolution();
            // Display dialog indicating loss
            showDialog("Game Over", "You've run out of attempts. Better luck next time!");
            isDialogShown = true;
        } else {
            Toast.makeText(this, "Success Percentage: " + checkSuccessPercentage() + "%", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show a dialog with the given title and message
    private void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null); // No action on OK button
        AlertDialog dialog = builder.create();
        dialog.show();
        pauseTimer(); // Pause the timer
        disableTimerButtons(); // Disable timer buttons
        isDialogShown = true;


    }

    public void onClearButtonClick(View view) {
        EditText focusedEditText = (EditText) getCurrentFocus();
        if (focusedEditText != null) {
            focusedEditText.setText("");
        }
    }
    // Start the timer
    public void startTimer() {
        // Create and start a Runnable to increment the timer value
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeElapsedInMillis += 1000; // Increment time by 1 second
                updateTimer(); // Update the timer display
                // Schedule the Runnable to run again after 1 second
                handler.postDelayed(this, 1000);
            }
        });
        timerRunning = true; // Set timer running flag
    }

    // Pause the timer
    public void pauseTimer() {
        // Remove any pending Runnables from the handler
        handler.removeCallbacksAndMessages(null);
        timerRunning = false; // Set timer running flag
    }

    // Restart the timer
    public void restartTimer() {
        timeElapsedInMillis = 0; // Reset elapsed time
        updateTimer(); // Update the timer display
    }

    // Update the timer display
    public void updateTimer() {
        int seconds = (int) (timeElapsedInMillis / 1000) % 60;
        int minutes = (int) ((timeElapsedInMillis / (1000 * 60)) % 60);


        String timeElapsedFormatted = String.format(Locale.getDefault(), "%02d:%02d",  minutes, seconds);
        timerText.setText(timeElapsedFormatted);
    }


    // Update attempts display
    public void updateAttemptsDisplay() {
        attemptsText.setText("Attempts:"+attemptt + "/8");
    }

}
