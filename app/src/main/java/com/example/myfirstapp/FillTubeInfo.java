package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import static com.example.myfirstapp.MainActivity.SAMPLE_NAME;
import static com.example.myfirstapp.MainActivity.NUMB_TUBES;
import static com.example.myfirstapp.MainActivity.TUBE_DILUTIONS;

public class FillTubeInfo extends AppCompatActivity {

    int numbTubes;
    static final String TAG = "FillTubeInfo";
    Intent sabetiLaunchCameraActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_tube_info);


        sabetiLaunchCameraActivityIntent = new Intent(this, SabetiLaunchCameraAppActivity.class);

        String sampleName = getIntent().getStringExtra(SAMPLE_NAME);
        numbTubes = Integer.valueOf(getIntent().getStringExtra(NUMB_TUBES));

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.editTextGroupLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Resources.getSystem().getDisplayMetrics().widthPixels / 5,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < numbTubes; i++) {
            EditText editTextView = new EditText(this);
            editTextView.setGravity(Gravity.CENTER);
            int currId = View.generateViewId();
            ids.add(currId);
            editTextView.setId(currId);

            editTextView.setLayoutParams(params);
            editTextView.setHint(Integer.toString(numbTubes - i - 2));
            editTextView.setHintTextColor(getResources().getColor(R.color.colorAccent));
            editTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            editTextView.setBackgroundColor(Color.BLACK);
            editTextView.setAlpha(0.5f);
            if (i == numbTubes - 1) {
                editTextView.setText(Integer.toString(numbTubes - i - 2));
                editTextView.setBackgroundColor(Color.BLUE);
                editTextView.setEnabled(false);
            }
            linearLayout.addView(editTextView);
        }

        Button btn = new Button(this);
        btn.setBackgroundColor(Color.rgb(70, 80, 90));
        btn.setTextColor(getResources().getColor(R.color.colorAccent));
        btn.setText("Next");
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ArrayList<String> dilutions = new ArrayList<>();
                for (int id : ids) {
                    dilutions.add(((EditText) findViewById(id)).getText().toString());
                }

                sabetiLaunchCameraActivityIntent.putExtra(NUMB_TUBES, String.valueOf(numbTubes));
                sabetiLaunchCameraActivityIntent.putExtra(SAMPLE_NAME, sampleName);
                sabetiLaunchCameraActivityIntent.putStringArrayListExtra(TUBE_DILUTIONS, dilutions);
                startActivity(sabetiLaunchCameraActivityIntent);
            }
        });
        linearLayout.addView(btn, params);

    }

    public void activateCamera(View view) {
//        Intent intent = new Intent(this, CameraActivity.class);
        Intent intent = new Intent(this, SabetiLaunchCameraAppActivity.class);
//        Intent intent = new Intent(this, ImageViewStripsSelectActivity.class);

        // Add the number of tubes indicated by the radio button
        Log.v(TAG, "numbTubes: " + numbTubes);
        intent.putExtra(NUMB_TUBES, String.valueOf(numbTubes));

        EditText editText = (EditText) findViewById(R.id.numb_);
        String sampleName = editText.getText().toString();
        intent.putExtra(SAMPLE_NAME, sampleName);
        startActivity(intent);
    }

}
