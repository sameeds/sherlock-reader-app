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
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final String NUMB_STRIPS = "com.example.myfirstapp.NUMB_STRIPS";
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
    private int numb_tubes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        if(!allPermissionsGranted()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        radioGroup = (RadioGroup) findViewById(R.id.tubecountgroup);
        //reference radiogroup ID from layout file
        radioGroup.clearCheck();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("chk", "id" + checkedId);
                switch(checkedId) {
                    case R.id.tubecount2:
                            numb_tubes = 2;
                        break;
                    case R.id.tubecount8:
                            numb_tubes = 8;
                        break;
                    case R.id.tubecount12:
                            numb_tubes = 12;
                        break;
                }
            }

        });

        RadioButton rb = (RadioButton) findViewById(R.id.tubecount8);
        rb.setChecked(true);
    }

    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.numb_);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }


//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//        switch(view.getId()) {
//            case R.id.tubecount2:
//                if (checked)
//                    numb_tubes = 2;
//                break;
//            case R.id.tubecount8:
//                if (checked)
//                    numb_tubes = 8;
//                break;
//            case R.id.tubecount12:
//                if (checked)
//                    numb_tubes = 12;
//                break;
//        }
//    }

    /**
     * Called when the user taps the Take Picture button
     */
    public void activateCamera(View view) {
//        Intent intent = new Intent(this, CameraActivity.class);
//        Intent intent = new Intent(this, SabetiLaunchCameraAppActivity.class);
        Intent intent = new Intent(this, ImageViewStripsSelectActivity.class);

        // Add the number of tubes indicated by the radio button
        Log.v(TAG, "numb_tubes: " + numb_tubes);
        intent.putExtra(NUMB_STRIPS, String.valueOf(numb_tubes));

        EditText editText = (EditText) findViewById(R.id.numb_);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
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
}
