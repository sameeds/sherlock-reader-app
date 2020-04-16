package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.myfirstapp.MainActivity.SAMPLE_NAME;
import static com.example.myfirstapp.MainActivity.NUMB_TUBES;
import static com.example.myfirstapp.MainActivity.TUBE_DILUTIONS;

public class SabetiLaunchCameraAppActivity extends AppCompatActivity {
    private ImageView imageView;
    File photoFile = null;
    String numb_tubes;
    private Boolean cameraAccessed = false;
    private ArrayList<String> dilutions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sabeti_launch_camera_app);

        imageView = (ImageView) findViewById(R.id.cameraCaptureimageView);

        // Get the Intent that started this activity and extract messages
        Intent intent = getIntent();
        String message = intent.getStringExtra(SAMPLE_NAME);
        numb_tubes = intent.getStringExtra(NUMB_TUBES);
        dilutions = intent.getStringArrayListExtra(TUBE_DILUTIONS);

        if (allPermissionsGranted()) {
            dispatchTakePictureIntent(message);
        } else {
            ActivityCompat.requestPermissions(this,
                    MainActivity.REQUIRED_PERMISSIONS,
                    MainActivity.REQUEST_CODE_PERMISSIONS);
        }
    }

    public void onResume() {
        super.onResume();
        if (cameraAccessed) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent(String sampleName) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile(sampleName);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myfirstapp.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap sourceImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            Matrix rotationMatrix = new Matrix();
            rotationMatrix.postRotate(getCameraPhotoOrientation(this,
                    FileProvider.getUriForFile(this,
                            "com.example.myfirstapp.provider",
                            new File(photoFile.getAbsolutePath())),
                    photoFile.getAbsolutePath()));
            Bitmap b = Bitmap.createBitmap(sourceImage, 0, 0,
                    sourceImage.getWidth(), sourceImage.getHeight(), rotationMatrix, true);
            if (photoFile.exists ()) photoFile.delete ();
            try {
                FileOutputStream out = new FileOutputStream(photoFile);
                b.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this, ImageViewTubesSelectActivity.class);
            intent.putExtra(SAMPLE_NAME, photoFile.getAbsolutePath());
            intent.putExtra(NUMB_TUBES, numb_tubes);
            intent.putStringArrayListExtra(TUBE_DILUTIONS, dilutions);
            startActivity(intent);
//
//            Log.d("SabetiLaunchCameraAp...", "setting ImageView");
//            if (photoFile != null && photoFile.exists()) {
//                Bitmap sourceImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
//                Matrix rotationMatrix = new Matrix();
//                rotationMatrix.postRotate(getCameraPhotoOrientation(this,
//                        FileProvider.getUriForFile(this,
//                        "com.example.myfirstapp.provider",
//                        photoFile),
//                        photoFile.getAbsolutePath()));
//                imageView.setImageBitmap(Bitmap.createBitmap(sourceImage, 0, 0,
//                        sourceImage.getWidth(), sourceImage.getHeight(), rotationMatrix, true));
////                imageView.setImageBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
////                imageView.setImageURI(Uri.fromFile(photoFile));
////                imageView.setRotation(getCameraPhotoOrientation(this,
////                        FileProvider.getUriForFile(this,
////                        "com.example.myfirstapp.provider",
////                        photoFile),
////                        photoFile.getAbsolutePath()));
//            }

        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    public static int getCameraPhotoOrientation(Context context, Uri imageUri,
                                                String imagePath) {
        // source: https://stackoverflow.com/a/36995847
        // MIT license (https://meta.stackexchange.com/questions/271080)
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            boolean forcePortraitMode = true;
            if (forcePortraitMode) {
                switch (orientation) { // this switch forces portrait mode.
                    case ExifInterface.ORIENTATION_ROTATE_270:
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                    case ExifInterface.ORIENTATION_NORMAL:
                        rotate = 90;
                        break;
                }
            } else { // use default orientations of captured picture
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                }
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }


    private File createImageFile(String sampleName) throws IOException {
        // Create an image file name
        String timeStamp =
                new SimpleDateFormat(MainActivity.CAMERA_DATE_FORMAT,
                        Locale.getDefault()).format(new Date());
        File storageDir =
                getExternalFilesDir(null);
        String imageFileName = "IMG_" + sampleName;
        File outputDirectory = new File(storageDir, MainActivity.RESULTS_DIRECTORY + "/IMG_" + timeStamp);
//                String fileName = storageDir + "/results/" + imageFileName + ".jpg";
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                Log.e("SabetiLaunchcameraAp...",
                        "Failed to create directory: " + outputDirectory.getAbsolutePath());
                outputDirectory = null;
            }
        }
        return new File(outputDirectory, imageFileName + ".jpg");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MainActivity.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                dispatchTakePictureIntent(getIntent().getStringExtra(SAMPLE_NAME));
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }


    public boolean allPermissionsGranted() {

        for (String permission : MainActivity.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
