package com.github.lindenb.android.apps.drawinggrid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
    private final static int PICK_IMAGE=101;
    private final static String TAG="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // https://stackoverflow.com/questions/36236181
        try { this.getSupportActionBar().hide(); }  catch (Throwable e){   Log.w(TAG,e);  }

        final Button buttonOpenImage = findViewById(R.id.selFileButton);

        final CheckBox cboxSquare = findViewById(R.id.squareCbox);
        cboxSquare.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getDrawingArea().setSquareGrid(b);
            }
        });

        final SeekBar gridCountSeekBar = findViewById(R.id.gridCount);
        gridCountSeekBar.setMax(20);
        gridCountSeekBar.setProgress(8);
        gridCountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG,"Div:"+progress);
                cboxSquare.setText(""+progress);
                getDrawingArea().setNumDiv(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });



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
        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE ) {
                Log.d(TAG,"PickImage");
                final Uri pickedImage = data.getData();
                if(pickedImage!=null) {

                    Log.d(TAG,"PickImage is not null");
                    final String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            final String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                            Log.d(TAG,"imagePath:"+imagePath);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            final Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                            Log.d(TAG,"Update bit map:"+(bitmap==null?"null":"ok"));
                            getDrawingArea().setBitmap(bitmap);
                        }

                    } catch (final Throwable err) {
                        Log.d(TAG, "Cannot read image",err);
                    } finally {
                        if (cursor != null) cursor.close();
                    }
                }


            }
        }
    }


    private DrawGridView getDrawingArea() {
        return (DrawGridView)findViewById(R.id.drawingArea);
    }


    public void  chooseImage(final View view) {
        Log.d(TAG,"Select image");
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }


}