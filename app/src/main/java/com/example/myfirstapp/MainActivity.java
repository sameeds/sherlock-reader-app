package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String SAMPLE_NAME = "com.example.myfirstapp.SAMPLE_NAME";
    public static final String NUMB_TUBES = "com.example.myfirstapp.NUMB_TUBES";
    public static final String TUBE_DILUTIONS = "com.example.myfirstapp.TUBE_DILUTIONS";
    public static int REQUEST_CODE_PERMISSIONS = 101;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE"};

    public static final String IMAGE_FILE_NAME = "IMAGE_FILE_NAME";
    public static final String CAMERA_DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String RESULTS_DIRECTORY = "/results";
    private static final String TAG = "MainActivity";
    private RadioGroup radioGroup;
    private int numbTubes;
    private boolean test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        radioGroup = (RadioGroup) findViewById(R.id.tubecountgroup);
        test = getTestVsTrain(radioGroup.getCheckedRadioButtonId());
        ((EditText) findViewById(R.id.sample_name)).setText(makeDefaultEditText(numbTubes));

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //reference radiogroup ID from layout file
        radioGroup.clearCheck();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("chk", "id" + checkedId);
                test = getTestVsTrain(checkedId);
                ((EditText) findViewById(R.id.sample_name)).setText(makeDefaultEditText(numbTubes));
            }

        });

        RadioButton rb = (RadioButton) findViewById(R.id.testTubes);
        rb.setChecked(true);
    }

    private Boolean getTestVsTrain(int checkedId) {
        switch (checkedId) {
            case R.id.testTubes:
                return true;
            case R.id.trainTubes:
                return false;
        }
        return true;
    }

    private String makeDefaultEditText(int numb_tubes) {
        String str = "sampleName";
//        str += ";[";
//        for (int i = 0; i < numb_tubes; i++){
//            str+= " ;";
//        }
//        str += "]";
        return str;
    }

    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.sample_name);
        String message = editText.getText().toString();
        intent.putExtra(SAMPLE_NAME, message);
        startActivity(intent);
    }


    /**
     * Called when the user taps the Take Picture button
     */
    public void activateCamera(View view) {
//        Intent intent = new Intent(this, SabetiLaunchCameraAppActivity.class);
        Intent intent;
        if (test) {
            intent = new Intent(this, SabetiLaunchCameraAppActivity.class);
            ArrayList<String> dilutions = new ArrayList<>();
            intent.putStringArrayListExtra(TUBE_DILUTIONS, dilutions);
        } else {
            intent = new Intent(this, FillTubeInfo.class);
        }
        // Add the number of tubes
        if ("".equals(((EditText) findViewById(R.id.tubeCount)).getText().toString())){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage("Please enter the tube count");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return;
        }

        numbTubes = Integer.parseInt(((EditText) findViewById(R.id.tubeCount)).getText().toString());
        Log.v(TAG, "numbTubes: " + numbTubes);
        intent.putExtra(NUMB_TUBES, String.valueOf(numbTubes));

        EditText editText = (EditText) findViewById(R.id.sample_name);
        String sampleName = editText.getText().toString();
        intent.putExtra(SAMPLE_NAME, sampleName);
        startActivity(intent);


    }

    /**
     * Called when the user taps the Take Picture button
     */
    public void goToResultsPage(View view) {
        Intent intent = new Intent(this, ViewPreviousTestsActivity.class);
        startActivity(intent);
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            Log.e(TAG, permission + " granted");
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }
}
