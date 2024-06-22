package com.example.sudoku;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button startGameButton = findViewById(R.id.startGameButton);
        Button aboutButton = findViewById(R.id.aboutButton);
        Button exitButton = findViewById(R.id.exitButton);

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, LevelActivity.class);
                startActivity(intent);
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
                builder.setTitle("About");
                builder.setMessage("This Sudoku game app was created by Yacine Kermame.\n\n" +
                        "I am 3rd year informatique student in Mustapha Stambouli University with grade License\n\n" +
                        "Sudoku is a logic-based combinatorial number-placement puzzle. The objective is to fill a 9x9 grid " +
                        "with digits so that each column, each row, and each of the nine 3x3 subgrids that compose the grid " +
                        "contain all of the digits from 1 to 9.\n\n" +
                        "© 2024 Yacine Kermame. All rights reserved.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish(); // Close the activity
            }
        });
    }
}
