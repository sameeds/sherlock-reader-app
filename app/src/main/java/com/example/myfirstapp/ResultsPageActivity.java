package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.myfirstapp.ImageViewTubesSelectActivity.TUBE_COORDS;
import static com.example.myfirstapp.MainActivity.NUMB_TUBES;
import static com.example.myfirstapp.MainActivity.TUBE_DILUTIONS;
import static com.example.myfirstapp.SabetiLaunchCameraAppActivity.getCameraPhotoOrientation;

public class ResultsPageActivity extends AppCompatActivity {
    static final String TAG = "ResultsPageActivity";
    private String imageFilePath;
    private float mScaleFactor;
    private ArrayList<String> tubeDilutions;
    private int numbTubes;
    private String tubeCoords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_page);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        imageFilePath = intent.getStringExtra(MainActivity.IMAGE_FILE_NAME);
        mScaleFactor = intent.getFloatExtra(ImageViewBoxSelectActivity.M_Y_SCALE_FACTOR, 1);
        tubeDilutions = intent.getStringArrayListExtra(TUBE_DILUTIONS);
        numbTubes = Integer.parseInt(getIntent().getStringExtra(NUMB_TUBES));
        tubeCoords = intent.getStringExtra(TUBE_COORDS);


        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView2);
        textView.setText("");

        String resultsText = "";
        try {
            String response = uploadFile();

            if (response.charAt(0) != '[') {
                textView.setText(response);
            } else {
                JSONObject obj = new JSONObject(response);
                JSONArray scores = obj.getJSONArray("final_scores");
                JSONArray calls = obj.getJSONArray("calls");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    String htmlText = "<h2>Results</h2><br>";
                    for (int i = 0; i < calls.length(); i++) {
                        float score = Float.valueOf(scores.getString(i));
                        String res = calls.getBoolean(i) ? "Positive" : "Negative";
                        res = i != calls.length() - 1 ? res : "Control";
                        htmlText += "<p> Tube " + (i + 1) + ": " + res + " (" +
                                String.format("%.2f", score) + ")</p>";
                    }

                    textView.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    String htmlText = "<p>Results</p><p></p>";
                    for (int i = 0; i < calls.length(); i++) {
                        htmlText += "<p> Tube " + (i + 1) + ": " +
                                (calls.getBoolean(i) ? "Positive" : "Negative") + "</p>";
                    }
                    textView.setText(Html.fromHtml(htmlText));
                }

                // Create string to save
                StringBuilder resultsTextBuilder = new StringBuilder();
                for (int i = 0; i < calls.length(); i++) {
                    if (i == calls.length() - 1) {
                        resultsTextBuilder.append("C");
                    } else {
                        resultsTextBuilder.append(calls.getBoolean(i) ? "+," : "-,");
                    }
                }
                resultsText = resultsTextBuilder.toString();
            }
            Log.d(TAG, resultsText);
            saveResultsFile(imageFilePath, resultsText);

        } catch (Exception e) {
            //textView.setText(message + " upload failed");
            textView.setText(e.getMessage());
            Log.e(TAG, "exception", e);
        }
    }

    private void addCallText(Canvas canvas, Paint paint, String text, Integer left, Integer right,
                             Integer y) {
        Rect r = new Rect();
        canvas.getClipBounds(r);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        Log.e(TAG, "" + r.width());
        Integer x = left + (right - left) / 2 - r.width() / 2;
        canvas.drawText(text, x, y, paint);
    }

    public String uploadFile()
            throws Exception {
        String fileName = imageFilePath;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(imageFilePath);

        // open a URL connection to the Servlet
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        String requestAddress = "http://34.95.33.102:3002/upload";
        URL url = new URL(requestAddress);
        Gson gson = new Gson();
        Log.d(TAG, gson.toJson(tubeDilutions));

        // Open a HTTP  connection to  the URL
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        conn.setRequestProperty("upload", fileName);
        conn.setRequestProperty("mScaleFactor", String.valueOf(mScaleFactor)); // top-left pixel coordinate
        conn.setRequestProperty("tubeDilutions", gson.toJson(tubeDilutions));
        conn.setRequestProperty("numbTubes", String.valueOf(numbTubes));
        conn.setRequestProperty("tubeCoords", tubeCoords);
//        conn.setRequestProperty("tly-pixel", tlypixel);
//        conn.setRequestProperty("brx-pixel", brxpixel);
//        conn.setRequestProperty("bry-pixel", brypixel); // bottom right pixel coordinate


        dos = new DataOutputStream(conn.getOutputStream());

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"upload\";filename=\""
                + fileName + "\"" + lineEnd);

        dos.writeBytes(lineEnd);

        // create a buffer of  maximum size
        bytesAvailable = fileInputStream.available();

        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        // read file and write it into form...
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {

            dos.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        // send multipart form data necesssary after file data...
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        int responseCode = conn.getResponseCode();
        InputStream inputStream;
        // Responses from the server (code and message)
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = conn.getInputStream();
        } else {
            inputStream = conn.getErrorStream();
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        inputStream));
        StringBuilder response = new StringBuilder();
        String currentLine;
        while ((currentLine = in.readLine()) != null)
            response.append(currentLine);
        in.close();

        String serverResponseMessage = response.toString();
        Log.v("uploadFile", "HTTP Response is : "
                + serverResponseMessage);

        //close the streams
        fileInputStream.close();
        dos.flush();
        dos.close();

        return serverResponseMessage;
    }

    public Boolean saveResultsFile(String imagePath, String resultsText) {
        File image_file = new File(imagePath);
        String image_name = image_file.getName();
        String sample_name = image_name.substring(0, image_name.length() - 4);
        File results_file = new File(image_file.getParentFile().toString(), sample_name + ".txt");
        Log.d(TAG, "resultsText: " + resultsText);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(results_file), StandardCharsets.UTF_8))) {
            writer.write(resultsText);
        } catch (Exception e) {
            //textView.setText(message + " upload failed");
            Log.e(TAG, "exception", e);
        }

        return Boolean.TRUE;
    }
}
