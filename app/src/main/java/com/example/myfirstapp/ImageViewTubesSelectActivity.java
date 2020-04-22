package com.example.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import static com.example.myfirstapp.ImageViewBoxSelectActivity.M_Y_SCALE_FACTOR;
import static com.example.myfirstapp.MainActivity.TUBE_DILUTIONS;
import static com.example.myfirstapp.MainActivity.SAMPLE_NAME;
import static com.example.myfirstapp.MainActivity.IMAGE_FILE_NAME;
import static com.example.myfirstapp.MainActivity.NUMB_TUBES;

public class ImageViewTubesSelectActivity extends AppCompatActivity {

    public static int REQUEST_CODE_PERMISSIONS = 101;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE"};
    ImageView imageView;
    LinearLayout mlinearLayout;
    public static final String TUBE_COORDS = "tubeCoords";
    public static final String CAMERA_DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String RESULTS_DIRECTORY = "/results";
    private static final String TAG = "ImageViewBoxSelectAct";
    public static final String m_TOP_LEFT_X = "mTopLeftX";
    public static final String m_TOP_LEFT_Y = "mTopLeftY";
    public static final String m_BOTTOM_RIGHT_X = "mBottomRightX";
    public static final String m_BOTTOM_RIGHT_Y = "mBottomRightY";
    public static final String M_ANGLE = "mAngle";
    private long mLastAnalysisResultTime;
    private double exposure_required = 1;
    private float imageViewScaleFactor;
    private Bitmap sourceImage;
    private int numbTubes;
    private int imageHeightPx;
    private int imageWidthPx;
    private Gson gson;
    private ArrayList<String> tubeDilutions;

    private class Box extends View {
        private Paint paint = new Paint();
        private ScaleGestureDetector mScaleGestureDetector;
        public float mScaleFactor = 1.f;
        public float mRectArea = 0;
        private static final int INVALID_POINTER_ID = -1;
        private int mActivePointerId = INVALID_POINTER_ID;
        private int mActivePointerId2 = INVALID_POINTER_ID;
        private float mOrigX1, mOrigY1, mCurrX1, mCurrY1, mOrigX2, mOrigY2, mCurrX2, mCurrY2;
        private float mPrevX;
        private float mPrevY;
        private float mPosX = 0f;
        private float mPosY = 0f;
        public float mAngle = 0f; // in degrees
        private Context mContext;
        private RelativeLayout mOuterContainerLayout;
        private RelativeLayout.LayoutParams mOuterButtonParams;
        private RelativeLayout.LayoutParams mInnerContainerLayoutParams;
        private RelativeLayout mOuterContainerLayout2;
        private RelativeLayout.LayoutParams mOuterButtonParams2;
        private RelativeLayout.LayoutParams mInnerContainerLayoutParams2;
        private RelativeLayout mOuterContainerLayout3;
        private RelativeLayout.LayoutParams mOuterButtonParams3;
        private RelativeLayout.LayoutParams mInnerContainerLayoutParams3;
        private ImageButton mImageButton;
        private Button mRotatePlusButton;
        private Button mRotateMinusButton;
        private Boolean mlayoutParamsSet;
        private float xLoc = 0;
        private float yLoc = 0;
        private float leftCorner;
        private float topCorner;
        private float stripHeight;
        private float stripWidth;
        private ArrayList<float[]> tubeLines;


        Box(Context context, ImageButton imageButton, Button rotatePlusButton,
            Button rotateMinusButton) {
            super(context);
            mContext = context;
            mImageButton = imageButton;
            mRotatePlusButton = rotatePlusButton;
            mRotateMinusButton = rotateMinusButton;
            mlayoutParamsSet = false;
            // mlinearLayout =  linearLayout;
            mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#FF8362"));
            tubeLines = new ArrayList<>();
//            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener());

        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            // Let the ScaleGestureDetector inspect all events.
            View view = this;
            boolean retVal = mScaleGestureDetector.onTouchEvent(ev);
            final int eventAction = ev.getAction();
            switch (eventAction & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    mOrigX1 = ev.getX();
                    mOrigY1 = ev.getY();
                    mCurrX1 = ev.getX();
                    mCurrY1 = ev.getY();
                    mPrevX = ev.getX();
                    mPrevY = ev.getY();
                    mActivePointerId = ev.getPointerId(0);
                    break;
                }

                case MotionEvent.ACTION_POINTER_DOWN: {
                    mOrigX2 = ev.getX();
                    mOrigY2 = ev.getY();
                    mCurrX2 = ev.getX();
                    mCurrY2 = ev.getY();
                    final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    mActivePointerId2 = ev.getPointerId(pointerIndex);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    float currX = ev.getX(pointerIndex);
                    float currY = ev.getY(pointerIndex);
                    // if ScaleGestureDetector is in progress, don't move
                    if (!mScaleGestureDetector.isInProgress()) {
                        float dx = currX - mPrevX;
                        float dy = currY - mPrevY;

                        mPosX += dx;
                        mPosY += dy;
                        invalidate();
                    }

                    // enable rotation:
                    if (mActivePointerId2 != INVALID_POINTER_ID) {
                        pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                        int pointerId = ev.getPointerId(pointerIndex);
                        if (pointerId == mActivePointerId) {
                            mCurrX1 = ev.getX();
                            mCurrY1 = ev.getY();
                        } else if (pointerId == mActivePointerId2) {
                            mCurrX2 = ev.getX();
                            mCurrY2 = ev.getY();
                        }

                        double angle1 = (Math.toDegrees(Math.atan(mOrigY2 / mOrigX2) - Math.atan(mOrigY1 / mOrigX1)) + 360) % 360;
                        double angle2 = (Math.toDegrees(Math.atan(mCurrY2 / mCurrX2) - Math.atan(mCurrY1 / mCurrX1)) + 360) % 360;

                        // mAngle = (float) (angle1 - angle2);
                    }

                    mPrevX = currX;
                    mPrevY = currY;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = ev.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // pointerId was the active pointer on the up motion. We need to chose a new pointer
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mPrevX = ev.getX(newPointerIndex);
                        mPrevY = ev.getY(newPointerIndex);
                        mActivePointerId = ev.getPointerId(newPointerIndex);
                        mActivePointerId2 = INVALID_POINTER_ID;
                    } else {
                        mActivePointerId2 = INVALID_POINTER_ID;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    mActivePointerId2 = INVALID_POINTER_ID;
                    break;
                }
            }
            return true;
        }


        private class ScaleListener
                extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mScaleFactor *= detector.getScaleFactor();

//                pivotPointX = detector.getFocusX();
//                pivotPointY = detector.getFocusY();
                // Don't let the object get too small or too large.
                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

                // tell the View to redraw the Canvas
                invalidate();
                return true;
            }
        }

        private void setupLayoutParams() {
            mOuterContainerLayout = new RelativeLayout(mContext);
            mOuterContainerLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            mOuterButtonParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            mOuterButtonParams.addRule(RelativeLayout.CENTER_IN_PARENT,
                    RelativeLayout.TRUE);

            mInnerContainerLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mInnerContainerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,
                    RelativeLayout.TRUE);


            int x0 = getWidth() / 8;

            mOuterContainerLayout2 = new RelativeLayout(mContext);
            mOuterContainerLayout2.setLayoutParams(new RelativeLayout.LayoutParams(
                    x0, x0));

            mOuterButtonParams2 = new RelativeLayout.LayoutParams(
                    x0, x0);
            mOuterButtonParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                    RelativeLayout.TRUE);

            mInnerContainerLayoutParams2 = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mInnerContainerLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                    RelativeLayout.TRUE);


            mOuterContainerLayout3 = new RelativeLayout(mContext);
            mOuterContainerLayout3.setLayoutParams(new RelativeLayout.LayoutParams(
                    x0,
                    x0));

            mOuterButtonParams3 = new RelativeLayout.LayoutParams(
                    x0,
                    x0);
            mOuterButtonParams3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                    RelativeLayout.TRUE);

            mInnerContainerLayoutParams3 = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mInnerContainerLayoutParams3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                    RelativeLayout.TRUE);
        }

        @Override
        protected void onDraw(Canvas canvas) { // Override the onDraw() Method
            super.onDraw(canvas);

            if (!mlayoutParamsSet) {
                setupLayoutParams();
                mlayoutParamsSet = true;
            }
            paint.setColor(Color.parseColor("#FFFFFF"));
//            canvas.drawRect(xLoc, yLoc, xLoc + 20, yLoc + 40, paint);
            Log.d(TAG, "xLoc: " + xLoc);
            Log.d(TAG, "yLoc: " + yLoc);

            canvas.save();
            canvas.scale(mScaleFactor, mScaleFactor);
            canvas.translate(mPosX, mPosY);
//            canvas.rotate(-1 * mAngle, getWidth() / 2, getHeight() / 2);

//            canvas.drawRect(getWidth() / 2.0f, getHeight() / 2.0f,
//                    getWidth() / 2.0f + 20, getHeight() / 2.0f + 40, paint);


            //center
            float strip_ratio = 233.0f / 86.0f;
            int x0 = getWidth();
            int y0 = getHeight();
            paint.setStrokeWidth(x0 / 100);
            paint.setColor(Color.parseColor("#FF8362"));

            int max_tubes = 12;
            int tube_buffer = 0; // how many strip widths between each strip
            int total_height_strips = max_tubes + max_tubes * tube_buffer + tube_buffer;

            stripHeight = y0 / (total_height_strips + 2);
            stripWidth = strip_ratio * stripHeight/2.5f;

//            //draw guide box
//            canvas.drawRect(x0 - stripWidth * 1.5f, 0,
//                    x0 - stripWidth * 0.5f, ((3) / 2f) * stripHeight, paint);
            leftCorner = x0 - stripWidth * 1.5f;
            topCorner = 1 / 2f * stripHeight;
            float diffWidthY = (float) (stripWidth * Math.sin(Math.toRadians(mAngle)));
            float diffWidthX = (float) (stripWidth * Math.cos(Math.toRadians(mAngle)));
            float diffHeightY = (float) (stripHeight * Math.cos(Math.toRadians(mAngle)));
            float diffHeightX = (float) (stripHeight * Math.sin(Math.toRadians(mAngle)));
            float currTopLeftX = leftCorner;
            float currTopLeftY = topCorner;
            tubeLines = new ArrayList<>(); // each tuple represents a line.
            for (int i = 0; i < numbTubes; i++) {
                if (i == numbTubes - 1) {
                    paint.setColor(Color.parseColor("#627cff"));
//                    paint.setStrokeWidth(x0 / 200);
//                    paint.setTextAlign(Paint.Align.CENTER);
//                    paint.setTextSize(16 * getResources().getDisplayMetrics().density);
//                    canvas.drawText("Control", x0 - stripWidth * 1.0f,
//                            ((2.0f * i + 2) / 2f) * stripHeight - ((paint.descent() + paint.ascent()) / 2), paint);
                    paint.setStrokeWidth(x0 / 100);
                }
                canvas.drawLine(currTopLeftX, currTopLeftY, currTopLeftX + diffWidthX,
                        currTopLeftY - diffWidthY, paint);
                canvas.drawLine(currTopLeftX + diffWidthX, currTopLeftY - diffWidthY,
                        currTopLeftX + diffWidthX + diffHeightX,
                        currTopLeftY - diffWidthY + diffHeightY, paint);
                canvas.drawLine(currTopLeftX + diffWidthX + diffHeightX,
                        currTopLeftY - diffWidthY + diffHeightY,
                        currTopLeftX + diffHeightX,
                        currTopLeftY + diffHeightY, paint);
                canvas.drawLine(currTopLeftX + diffHeightX, currTopLeftY + diffHeightY,
                        currTopLeftX, currTopLeftY, paint);
                tubeLines.add(new float[]{currTopLeftX, currTopLeftY,
                        currTopLeftX + diffWidthX, currTopLeftY - diffWidthY});
                currTopLeftX = currTopLeftX + diffHeightX;
                currTopLeftY = currTopLeftY + diffHeightY;
            }
            tubeLines.add(new float[]{currTopLeftX, currTopLeftY,
                    currTopLeftX + diffWidthX, currTopLeftY - diffWidthY});

            mRectArea = (stripWidth * stripWidth * mScaleFactor);
            Log.d(TAG, "mPosX: " + mPosX);
            Log.d(TAG, "mPosY: " + mPosY);
            Log.d(TAG, "mScaleFactor: " + mScaleFactor);
            Log.d(TAG, "boxViewWidth: " + this.getMeasuredWidth());
            Log.d(TAG, "boxViewHeight: " + this.getMeasuredHeight());
            Log.d(TAG, "mAngle: " + mAngle);

            ((ViewGroup) mImageButton.getParent()).removeView(mImageButton);
//            ((ViewGroup)this.getParent()).addView(mImageButton);
            mOuterButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mImageButton.setLayoutParams(mOuterButtonParams);
            mOuterContainerLayout.addView(mImageButton);
            if (((ViewGroup) mOuterContainerLayout.getParent()) != null) {
                ((ViewGroup) mOuterContainerLayout.getParent()).removeView(mOuterContainerLayout);
            }
            addContentView(mOuterContainerLayout, mInnerContainerLayoutParams);

            ((ViewGroup) mRotateMinusButton.getParent()).removeView(mRotateMinusButton);
            mOuterButtonParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mRotateMinusButton.setLayoutParams(mOuterButtonParams2);
            mOuterContainerLayout2.addView(mRotateMinusButton);
            if (((ViewGroup) mOuterContainerLayout2.getParent()) != null) {
                ((ViewGroup) mOuterContainerLayout2.getParent()).removeView(mOuterContainerLayout2);
            }
            addContentView(mOuterContainerLayout2, mInnerContainerLayoutParams2);

            ((ViewGroup) mRotatePlusButton.getParent()).removeView(mRotatePlusButton);
            mOuterButtonParams3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mRotatePlusButton.setLayoutParams(mOuterButtonParams3);
            mOuterContainerLayout3.addView(mRotatePlusButton);
            if (((ViewGroup) mOuterContainerLayout3.getParent()) != null) {
                ((ViewGroup) mOuterContainerLayout3.getParent()).removeView(mOuterContainerLayout3);
            }
            addContentView(mOuterContainerLayout3, mInnerContainerLayoutParams3);
            canvas.restore();

        }
    }


    // Change the coordinate basis of the tube lines to be relative to the image instead of
    // relative to the view.
    // NOTE: THIS ONLY WORKS IF THE IMAGE IS IN PORTRAIT MODE (which it should be!)
    private ArrayList<float[]> convertBoxPxToImagePx(Box box) {
        ArrayList<float[]> tubeLines = box.tubeLines;
        ArrayList<float[]> convertedTubeLines = new ArrayList<>();
        // the number of screen pixels above where the image starts.
        float yBuffer = (box.getHeight() - imageHeightPx / imageViewScaleFactor) / 2;
        for (float[] line : tubeLines) {
            // tuple is: {x1, y1, x2, y2}
            float[] convertedLine = new float[]{(line[0] + box.mPosX) * box.mScaleFactor * imageViewScaleFactor,
                    ((line[1] + box.mPosY) * box.mScaleFactor - yBuffer) * imageViewScaleFactor,
                    (line[2] + box.mPosX) * box.mScaleFactor * imageViewScaleFactor,
                    ((line[3] + box.mPosY) * box.mScaleFactor - yBuffer) * imageViewScaleFactor};
            Log.d(TAG, gson.toJson(convertedLine));
            convertedTubeLines.add(convertedLine);
        }
        return convertedTubeLines;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_strip_tube_select);
        imageView = (ImageView) findViewById(R.id.capturedImage);

        String photoFilePath = getIntent().getStringExtra(SAMPLE_NAME);
        Log.d(TAG, photoFilePath);
        numbTubes = Integer.parseInt(getIntent().getStringExtra(NUMB_TUBES));
        tubeDilutions = getIntent().getStringArrayListExtra(TUBE_DILUTIONS);
        gson = new Gson();

        Log.d("ImageViewBoxSelectAct", "photoFilePath: " + photoFilePath);
        File imageFile = new File(photoFilePath);
        if (!imageFile.exists()) {
            Log.d("ImageViewBoxSelectAct", "DOES NOT EXIST: " + photoFilePath);

        }
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 16;
        sourceImage = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imageView.setImageURI(Uri.fromFile(imageFile));
        Matrix rotationMatrix = new Matrix();
//        rotationMatrix.postRotate(getCameraPhotoOrientation(this,
//                FileProvider.getUriForFile(this,
//                        "com.example.myfirstapp.provider",
//                        new File(photoFilePath)),
//                photoFilePath));
//        Bitmap b = Bitmap.createBitmap(sourceImage, 0, 0,
//                sourceImage.getWidth(), sourceImage.getHeight(), rotationMatrix, true);
//        imageView.setImageBitmap(b);

        imageView = findViewById(R.id.capturedImage);
        imageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        imageWidthPx = imageView.getMeasuredWidth(); // this is the image width
        imageHeightPx = imageView.getMeasuredHeight(); // this is the image height

        Log.d(TAG, "imageWidthPx: " + imageWidthPx);
        Log.d(TAG, "imageHeightPx: " + imageHeightPx);


        addBox(photoFilePath);

    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }


    private void addBox(String photoFilePath) {
        // Set up preview screen based
        final int screenWidth = imageView.getWidth();
        final int screenHeight = imageView.getHeight();
        Size screen = new Size(screenWidth, screenHeight);

        Intent resultsPageIntent = new Intent(this, ResultsPageActivity.class);
        resultsPageIntent.putExtra(IMAGE_FILE_NAME, photoFilePath);


        ImageButton sendToResultsButton = findViewById(R.id.sendToServerButton);
        Button rotatePlusButton = findViewById(R.id.rotatePlus);
        Button rotateMinusButton = findViewById(R.id.rotateMinus);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Box box = new Box(this, sendToResultsButton, rotatePlusButton, rotateMinusButton);
        addContentView(box, params);

        sendToResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("", "");
                Log.d(TAG, "button pressed");
                // if the aspect ratio of canvas is larger than the aspect ratio of the image
                // (height/width), then the scaling factor of image pixel to canvas pixel is
                // constrained by the ratio of the widths. e.g. canvas is 400x200, and the image is
                // 400x400. Then the scaling is by a factor of 2.
//                imageViewScaleFactor = box.getHeight() / box.getWidth() >
//                        imageHeightPx / imageWidthPx ?
//                        (float) imageWidthPx / box.getWidth() :
//                        (float) imageHeightPx / box.getHeight();
                imageViewScaleFactor = (float) imageWidthPx / box.getWidth();
                // M_Y_SCALE_FACTOR is the total number of pixels in the jpg file that correspond
                // to the user-enclosed box.
                resultsPageIntent.putExtra(M_Y_SCALE_FACTOR, box.mRectArea *
                        imageViewScaleFactor * imageViewScaleFactor);
                resultsPageIntent.putExtra(NUMB_TUBES, Integer.toString(numbTubes));
                resultsPageIntent.putStringArrayListExtra(TUBE_DILUTIONS, tubeDilutions);

                resultsPageIntent.putExtra(TUBE_COORDS, gson.toJson(convertBoxPxToImagePx(box)));
                Log.d(TAG, "imageViewScaleFactor: " + imageViewScaleFactor);
                Log.d(TAG, "box.mScaleFactor:" + box.mScaleFactor);
                Log.d(TAG, "box.mRectArea: " + box.mRectArea);
                Log.d(TAG, "box.mRectArea * imageViewScaleFactor * imageViewScaleFactor: "
                        + box.mRectArea * imageViewScaleFactor * imageViewScaleFactor);
                Log.d(TAG, "box.getWidth(): " + box.getWidth());
                Log.d(TAG, "box.getHeight(): " + box.getHeight());
                Log.d(TAG, "imageWidthPx: " + imageWidthPx);
                Log.d(TAG, "imageHeightPx: " + imageHeightPx);


                box.xLoc = (box.tubeLines.get(1)[0] + box.mPosX) * box.mScaleFactor;
                box.yLoc = (box.tubeLines.get(1)[1] + box.mPosY) * box.mScaleFactor;
                box.invalidate();
                startActivity(resultsPageIntent);
            }
        });

        rotatePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "rotatePlusButton clicked");
                box.mAngle += 0.5;
                box.invalidate();
            }
        });

        rotateMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "rotateMinusButton clicked");
                box.mAngle -= 0.5;
                box.invalidate();
                Log.d(TAG, "mAngle: " + box.mAngle);
            }
        });
    }

}
