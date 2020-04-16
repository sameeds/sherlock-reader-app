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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

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

import static com.example.myfirstapp.MainActivity.NUMB_TUBES;
import static com.example.myfirstapp.MainActivity.TUBE_DILUTIONS;
import static com.example.myfirstapp.SabetiLaunchCameraAppActivity.getCameraPhotoOrientation;

public class ResultsPageActivity extends AppCompatActivity {
    static final String TAG = "ResultsPageActivity";
    private String imageFilePath;
    private float mScaleFactor;
    private ArrayList<String> tubeDilutions;
    private int numbTubes;

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

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView2);
        textView.setText("");

        String resultsText = "";
        try {
            String response = uploadFile();


            ImageView imageView = (ImageView) findViewById(R.id.processedImage);
            Log.d(TAG, "imageFilePath: " + imageFilePath);
            File imageFile = new File(imageFilePath);
            if (!imageFile.exists()) {
                Log.d(TAG, "DOES NOT EXIST: " + imageFilePath);

            }
            Bitmap sourceImage = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Matrix rotationMatrix = new Matrix();
            rotationMatrix.postRotate(getCameraPhotoOrientation(this,
                    FileProvider.getUriForFile(this,
                            "com.example.myfirstapp.provider",
                            new File(imageFilePath)),
                    imageFilePath));

            Bitmap bitmap = Bitmap.createBitmap(sourceImage, 0, 0,
                    sourceImage.getWidth(), sourceImage.getHeight(), rotationMatrix, true);
            imageView.setImageBitmap(bitmap);

            // set up Paint object
            imageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            Log.e(TAG, "" + imageView.getMeasuredWidth());
            Integer stroke_width = (Integer) imageView.getMeasuredWidth() / 135;
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.BLACK);
            p.setTextSize(40 * getResources().getDisplayMetrics().density);
            p.setStrokeWidth(stroke_width);

            // Create Temp bitmap
            Bitmap tBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.RGB_565);
            // Create a new canvas and add Bitmap into it
            Canvas tCanvas = new Canvas(tBitmap);
            //Draw the image bitmap into the canvas
            tCanvas.drawBitmap(bitmap, 0, 0, null);
            // Draw a rectangle over canvas

//            String response = "OK - " + message;
            if (response.charAt(0) != '[') {
                textView.setText(response);
            } else {
                // The results string has the following format:
                // [{(x1,y1):(x2,y2):(x3,y3):(x4,y4);(x1,y1):(x2,y2):(x3,y3):(x4,y4)}
                //      <results>POSITIVE,CONTROL]
                // where the coordnates are bottom left, top left, top right, and bottom right
                // coordinates of each strip.
                String full_response = response.substring(1, response.length() - 1);
                String results_cue = "<results>";
                String signal_response = full_response.substring(full_response.indexOf(results_cue)
                        + results_cue.length(), full_response.length() - 1);
                ArrayList<String> results = new ArrayList<>(Arrays.asList(
                        signal_response.split(",")));
                String strips_info = full_response.substring(1,
                        full_response.indexOf(results_cue) - 1);
                Log.d(TAG, strips_info);
                ArrayList<String> strip_positions = new ArrayList<>(Arrays.asList(
                        strips_info.split(";")));
                // strip_positions should look like
                // (x1,y1):(x2,y2):(x3,y3):(x4,y4)
                if (true) {
                    for (int i = 0; i < results.size(); i++) {
                        ArrayList<String> corners = new ArrayList<>(Arrays.asList(
                                strip_positions.get(i).split(":")));
                        Log.d(TAG, corners.toString());
                        Integer bl_x = Integer.parseInt(corners.get(0).substring(1,
                                corners.get(0).indexOf(",")));
                        Integer bl_y = Integer.parseInt(corners.get(0).substring(corners.get(0).indexOf(",")
                                + 1, corners.get(0).length() - 1));
                        Log.d(TAG, "bl");
                        Log.d(TAG, corners.get(0));
                        Log.d(TAG, bl_y.toString());

                        Integer tl_x = Integer.parseInt(corners.get(1).substring(1,
                                corners.get(1).indexOf(",")));
                        Integer tl_y = Integer.parseInt(corners.get(1).substring(corners.get(1).indexOf(",")
                                + 1, corners.get(1).length() - 1));
                        Log.d(TAG, "tl");
                        Log.d(TAG, tl_x.toString());
                        Log.d(TAG, tl_y.toString());

                        Integer tr_x = Integer.parseInt(corners.get(2).substring(1,
                                corners.get(2).indexOf(",")));
                        Integer tr_y = Integer.parseInt(corners.get(2).substring(corners.get(2).indexOf(",")
                                + 1, corners.get(2).length() - 1));
                        Log.d(TAG, "tr");
                        Log.d(TAG, tr_x.toString());
                        Log.d(TAG, tr_y.toString());

                        Integer br_x = Integer.parseInt(corners.get(3).substring(1,
                                corners.get(3).indexOf(",")));
                        Integer br_y = Integer.parseInt(corners.get(3).substring(corners.get(3).indexOf(",")
                                + 1, corners.get(3).length() - 1));
                        Log.d(TAG, "br");
                        Log.d(TAG, br_x.toString());
                        Log.d(TAG, br_y.toString());
//            canvas.drawText("Ctrl", x0 - strip_width * 2, (y0 - strip_height) / 2 - (y0 - strip_height) / 20, paint);
                        Integer strip_height = tl_y - bl_y;
                        if ("POSITIVE".equals(results.get(i))) {
                            p.setColor(Color.RED);
                            addCallText(tCanvas, p, "+", tl_x, tr_x,
                                    tl_y + strip_height / 20);
                        } else if ("NEGATIVE".equals(results.get(i))) {
                            p.setColor(Color.BLUE);
                            addCallText(tCanvas, p, "-", tl_x, tr_x,
                                    tl_y + strip_height / 20);
                        } else {
                            p.setColor(Color.BLUE);
                            addCallText(tCanvas, p, "C", tl_x, tr_x,
                                    tl_y + strip_height / 20);
                        }

                        tCanvas.drawLine(bl_x, bl_y, tl_x, tl_y, p);
                        tCanvas.drawLine(bl_x - stroke_width / 2, bl_y,
                                br_x + stroke_width / 2, br_y, p);
                        tCanvas.drawLine(tr_x, tr_y, br_x, br_y, p);
                        tCanvas.drawLine(tr_x + stroke_width / 2, tr_y,
                                tl_x - stroke_width / 2, tl_y, p);
                    }
                }

                imageView.setImageDrawable(new BitmapDrawable(getResources(), tBitmap));


                // write to page:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    String htmlText = "<h2>Results</h2><br>";
//                    for (int i = 0; i < results.size(); i++) {
//                        htmlText += "<p> Strip " + i + ": " + results.get(i) + "</p>";
//                    }
//                    textView.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT));
//                } else {
//                    String htmlText = "<p>Results</p><p></p>";
//                    for (int i = 0; i < results.size(); i++) {
//                        htmlText += "<p> Strip " + i + ": " + results.get(i) + "</p>";
//                    }
//                    textView.setText(Html.fromHtml(htmlText));
//                }

                // Create string to save
                StringBuilder resultsTextBuilder = new StringBuilder();
                for (int i = 0; i < results.size(); i++) {
                    if (results.get(i).contains("POSITIVE")) {
                        resultsTextBuilder.append("+,");
                    } else if (results.get(i).contains("NEGATIVE")) {
                        resultsTextBuilder.append("-,");
                    } else if (results.get(i).contains("CONTROL")) {
                        resultsTextBuilder.append("C");
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
        String requestAddress = "http://34.95.33.102:3001/upload";
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
