package com.github.lindenb.android.apps.hello02;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.github.lindenb.android.apps.hello02.MESSAGE";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void  openGrid(final View view) {
        final  Intent intent = new Intent(MainActivity.this, DrawingGridActivity.class);
            startActivity(intent);
        
    }

   public void  sendMessage(final View view) {
      final  Intent intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
      final View editText = (View)findViewById(R.id.editTextTextPersonName);
       if(editText!=null && editText instanceof  TextView) {
           final String message = TextView.class.cast(editText).getText().toString();
           intent.putExtra(EXTRA_MESSAGE, message == null ? "ZOB" : message);
           startActivity(intent);
       }
    }
}