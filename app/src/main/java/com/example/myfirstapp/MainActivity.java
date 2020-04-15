package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        radioGroup = (RadioGroup) findViewById(R.id.tubecountgroup);
        numbTubes = getCheckedRadioNumber(radioGroup.getCheckedRadioButtonId());
        ((EditText) findViewById(R.id.numb_)).setText(makeDefaultEditText(numbTubes));

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //reference radiogroup ID from layout file
        radioGroup.clearCheck();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("chk", "id" + checkedId);
                numbTubes = getCheckedRadioNumber(checkedId);
                ((EditText) findViewById(R.id.numb_)).setText(makeDefaultEditText(numbTubes));
            }

        });

        RadioButton rb = (RadioButton) findViewById(R.id.tubecount8);
        rb.setChecked(true);
    }

    private int getCheckedRadioNumber(int checkedId){
        int tube_count = 0;
        switch (checkedId) {
            case R.id.tubecount2:
                tube_count = 2;
                break;
            case R.id.tubecount8:
                tube_count = 8;
                break;
            case R.id.tubecount12:
                tube_count = 12;
                break;
        }
        return tube_count;
    }

    private String makeDefaultEditText(int numb_tubes){
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
        EditText editText = (EditText) findViewById(R.id.numb_);
        String message = editText.getText().toString();
        intent.putExtra(SAMPLE_NAME, message);
        startActivity(intent);
    }


//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//        switch(view.getId()) {
//            case R.id.tubecount2:
//                if (checked)
//                    numbTubes = 2;
//                break;
//            case R.id.tubecount8:
//                if (checked)
//                    numbTubes = 8;
//                break;
//            case R.id.tubecount12:
//                if (checked)
//                    numbTubes = 12;
//                break;
//        }
//    }

    /**
     * Called when the user taps the Take Picture button
     */
    public void activateCamera(View view) {
//        Intent intent = new Intent(this, SabetiLaunchCameraAppActivity.class);
        Intent intent = new Intent(this, FillTubeInfo.class);

        // Add the number of tubes indicated by the radio button
        Log.v(TAG, "numbTubes: " + numbTubes);
        intent.putExtra(NUMB_TUBES, String.valueOf(numbTubes));

        EditText editText = (EditText) findViewById(R.id.numb_);
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
