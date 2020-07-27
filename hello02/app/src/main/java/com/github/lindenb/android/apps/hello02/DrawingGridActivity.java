package com.github.lindenb.android.apps.hello02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class DrawingGridActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_grid);

        ConstraintLayout layout = findViewById(R.id.grid01);
        DrawingGridView gridView = new DrawingGridView(this);
        layout.addView(gridView);
    }
}