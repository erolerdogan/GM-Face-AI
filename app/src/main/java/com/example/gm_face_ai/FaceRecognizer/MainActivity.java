/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.gm_face_ai.FaceRecognizer;

import android.content.ClipData;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.gm_face_ai.FaceRecognizer.env.BorderedText;
import com.example.gm_face_ai.FaceRecognizer.env.FileUtils;
import com.example.gm_face_ai.FaceRecognizer.env.ImageUtils;
import com.example.gm_face_ai.FaceRecognizer.env.Logger;
import com.example.gm_face_ai.FaceRecognizer.tracking.MultiBoxTracker;
import com.example.gm_face_ai.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class MainActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    private static final int FACE_SIZE = 160;
    private static final int CROP_SIZE = 300;

    private static final Size DESIRED_PREVIEW_SIZE = new Size(1024, 768);

    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;

    private Integer sensorOrientation;

    private Classifier classifier;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private byte[] luminanceCopy;

    private BorderedText borderedText;

    private Snackbar initSnackbar;
    private Snackbar trainSnackbar;
    private FloatingActionButton button;
    private FloatingActionButton buttonDel;

    private boolean initialized = false;
    private boolean training = false;

    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
    private FloatingActionButton fab4;
    private TextView txtName;
    private TextView txtHold;
    public float THRESHOLD1;
    public Boolean  CameraW;
    Boolean isOpen = false;

    Animation fabOpen, fabClose, rotateForward, rotateBackward;
    private String TAG = "TAG";


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.container);
        initSnackbar = Snackbar.make(container, "Yükleniyor...", Snackbar.LENGTH_INDEFINITE);
        trainSnackbar = Snackbar.make(container, "Makine eğitiliyor...", Snackbar.LENGTH_INDEFINITE);
        Bundle data=getIntent().getExtras();
        CameraW = data.getBoolean("CameraWay2");

        fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1_add_person);
        fab2 = findViewById(R.id.fab2_contacts);
        fab3 = findViewById(R.id.fab3_del);
        fab4 = findViewById(R.id.fab4_threshold);
        txtName = findViewById(R.id.txtName);
        THRESHOLD1 = 0.51f;

        txtHold = findViewById(R.id.txtThreshold);
        txtHold.setText("Güncel eşik değer : " + THRESHOLD1);
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);


        View dialogNum = getLayoutInflater().inflate(R.layout.number_picker_layout, null);
        NumberPicker picker1 = (NumberPicker) dialogNum.findViewById(R.id.number_picker);
        picker1.setMaxValue(80);
        picker1.setMinValue(30);
        picker1.setValue((int) THRESHOLD1 * 100);
        int[] pickerValues = new int[50];
        AlertDialog editDialogNum = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Eşik değer")
                .setView(dialogNum)
                .setPositiveButton("Tamam", (dialogInterface, i) -> {
                    THRESHOLD1 = picker1.getValue();
                    classifier.THRESHOLD = (float) THRESHOLD1 / 100;
                    txtHold.setText("Güncel eşik değer : " + classifier.THRESHOLD);
                })
                .create();

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        EditText editText = dialogView.findViewById(R.id.edit_text);

        // Dialog window to add new person
        AlertDialog editDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.enter_name)
                .setView(dialogView)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    int idx = classifier.addPerson(editText.getText().toString());
                    performFileSearch(idx - 1);
                })
                .create();
        AssetManager mgr = getAssets();
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileUtils.delfiles(mgr, FileUtils.DATA_FILE);
                FileUtils.delfiles(mgr, FileUtils.MODEL_FILE);
                FileUtils.delfiles(mgr, FileUtils.LABEL_FILE);
                classifier.delClasses();
                Intent intent = new Intent(getApplicationContext(), com.example.gm_face_ai.MainActivity.class);
                startActivity(intent);
            }
        });


        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialogNum.show();
            }

        });

        fab.setOnClickListener(view -> animateFab());
        fab2.setOnClickListener(view ->

                // Dialog window to choose name from list

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.select_name))
                        .setItems(classifier.getClassNames(), (dialogInterface, i) -> {
                            if (i == 0) {

                            } else {
                                performFileSearch(i - 1);
                            }
                        })
                        .show());

        fab1.setOnClickListener(view -> {
            editDialog.show();
        });
        SwitchCam(CameraW);

    }

    private void animateFab() {
        if (isOpen) {
            fab.startAnimation(rotateBackward);
            fab1.startAnimation(fabClose);
            fab2.startAnimation(fabClose);
            fab3.startAnimation(fabClose);
            fab4.startAnimation(fabClose);

            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            fab4.setClickable(false);

            isOpen = false;
        } else {
            fab.startAnimation(rotateForward);
            fab1.startAnimation(fabOpen);
            fab2.startAnimation(fabOpen);
            fab3.startAnimation(fabOpen);
            fab4.startAnimation(fabOpen);

            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            fab4.setClickable(true);
            isOpen = true;
        }
    }


    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        if (!initialized)
            new Thread(this::init).start();

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        // it will be   borderedText.setTypeface(Typeface.MONOSPACE);
        borderedText.setTypeface(Typeface.DEFAULT);

        tracker = new MultiBoxTracker(this);

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(CROP_SIZE, CROP_SIZE, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        CROP_SIZE, CROP_SIZE,
                        sensorOrientation, false);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> {
                    tracker.draw(canvas, txtName);
                    if (isDebug()) {
                        tracker.drawDebug(canvas, txtName);
                    }
                });

        addCallback(
                canvas -> {
                    if (!isDebug()) {
                        return;
                    }
                    final Bitmap copy = cropCopyBitmap;
                    if (copy == null) {
                        return;
                    }

                    final int backgroundColor = Color.argb(100, 0, 0, 0);
                    canvas.drawColor(backgroundColor);

                    final Matrix matrix = new Matrix();
                    final float scaleFactor = 2;
                    matrix.postScale(scaleFactor, scaleFactor);
                    matrix.postTranslate(
                            canvas.getWidth() - copy.getWidth() * scaleFactor,
                            canvas.getHeight() - copy.getHeight() * scaleFactor);
                    canvas.drawBitmap(copy, matrix, new Paint());

                    final Vector<String> lines = new Vector<String>();
                    if (classifier != null) {
                        final String statString = classifier.getStatString();
                        final String[] statLines = statString.split("\n");
                        Collections.addAll(lines, statLines);
                    }
                    lines.add("");
                    lines.add("Frame: " + previewWidth + "x" + previewHeight);
                    lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
                    lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                    lines.add("Rotation: " + sensorOrientation);
                    lines.add("Inference time: " + lastProcessingTimeMs + "ms");

                    borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
                });
    }

    OverlayView trackingOverlay;

    void init() {
        runOnUiThread(() -> initSnackbar.show());
        File dir = new File(FileUtils.ROOT);

        if (!dir.isDirectory()) {
            if (dir.exists()) dir.delete();
            dir.mkdirs();

            AssetManager mgr = getAssets();
            FileUtils.copyAsset(mgr, FileUtils.DATA_FILE);
            FileUtils.copyAsset(mgr, FileUtils.MODEL_FILE);
            FileUtils.copyAsset(mgr, FileUtils.LABEL_FILE);
        }

        try {
            classifier = Classifier.getInstance(getAssets(), FACE_SIZE, FACE_SIZE);
        } catch (Exception e) {
            LOGGER.e("Exception initializing classifier!", e);
            finish();
        }

        runOnUiThread(() -> initSnackbar.dismiss());
        initialized = true;
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        byte[] originalLuminance = getLuminance();
        tracker.onFrame(
                previewWidth,
                previewHeight,
                getLuminanceStride(),
                sensorOrientation,
                originalLuminance,
                timestamp);
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection || !initialized || training) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        if (luminanceCopy == null) {
            luminanceCopy = new byte[originalLuminance.length];
        }
        System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                () -> {
                    LOGGER.i("Running detection on image " + currTimestamp);
                    final long startTime = SystemClock.uptimeMillis();

                    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                    List<Classifier.Recognition> mappedRecognitions =
                            classifier.recognizeImage(croppedBitmap, cropToFrameTransform);

                    lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                    tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
                    trackingOverlay.postInvalidate();
                    requestRender();
                    computingDetection = false;
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!initialized) {
            Snackbar.make(
                    getWindow().getDecorView().findViewById(R.id.container),
                    "Try it again later", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        if (resultCode == RESULT_OK) {
            trainSnackbar.show();
            fab1.setEnabled(false);
            training = true;

            ClipData clipData = data.getClipData();
            ArrayList<Uri> uris = new ArrayList<>();

            if (clipData == null) {
                uris.add(data.getData());
            } else {
                for (int i = 0; i < clipData.getItemCount(); i++)
                    uris.add(clipData.getItemAt(i).getUri());
            }

            new Thread(() -> {
                try {
                    classifier.updateData(requestCode, getContentResolver(), uris);
                } catch (Exception e) {
                    LOGGER.e(e, "Exception!");
                } finally {
                    training = false;
                }
                runOnUiThread(() -> {
                    trainSnackbar.dismiss();
                    fab1.setEnabled(true);
                });
            }).start();

        }
    }

    public void performFileSearch(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");

        startActivityForResult(intent, requestCode);
    }
}

