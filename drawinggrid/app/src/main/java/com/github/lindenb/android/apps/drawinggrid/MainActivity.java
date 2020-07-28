package com.github.lindenb.android.apps.drawinggrid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {
    private final static int PICK_IMAGE=1;
    private Uri currImageURI= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button buttonOpenImage = (Button) findViewById(R.id.selFileButton);
        buttonOpenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            });
        final Button showGridButton =  (Button) findViewById(R.id.showGridButton);
        final CheckBox cboxSquare = (CheckBox)findViewById(R.id.rotateCbox);


        // recovering the instance state
        if (savedInstanceState != null) {

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE) {
                currImageURI = data.getData();
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }


}