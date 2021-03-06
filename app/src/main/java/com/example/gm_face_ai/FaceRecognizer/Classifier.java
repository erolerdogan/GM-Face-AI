/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.gm_face_ai.FaceRecognizer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.gm_face_ai.FaceRecognizer.env.FileUtils;
import com.example.gm_face_ai.FaceRecognizer.wrapper.FaceNet;
import com.example.gm_face_ai.FaceRecognizer.wrapper.LibSVM;
import com.example.gm_face_ai.FaceRecognizer.wrapper.MTCNN;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Generic interface for interacting with different recognition engines.
 */
public class Classifier {
    public Float THRESHOLD = 0.31f;

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    public class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /**
         * Display name for the recognition.
         */
        private final String title;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        /**
         * Optional location within the source image for the location of the recognized object.
         */
        private RectF location;

        Recognition(
                final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }

    public static final int EMBEDDING_SIZE = 512;
    private static Classifier classifier;

    private MTCNN mtcnn;
    private FaceNet faceNet;
    private LibSVM svm;

    private List<String> classNames;

    private Classifier() {
    }
    //////////// F  ///////////////////////////

    ///////////////////////////////////////

    static Classifier getInstance(AssetManager assetManager,
                                  int inputHeight,
                                  int inputWidth) throws Exception {
        if (classifier != null) return classifier;

        classifier = new Classifier();

        classifier.mtcnn = MTCNN.create(assetManager);
        classifier.faceNet = FaceNet.create(assetManager, inputHeight, inputWidth);
        classifier.svm = LibSVM.getInstance();

        classifier.classNames = FileUtils.readLabel(FileUtils.LABEL_FILE);

        return classifier;
    }

    CharSequence[] getClassNames() {
        CharSequence[] cs = new CharSequence[classNames.size() + 1];
        int idx = 1;

        cs[0] = "-----Kayıtlı Kişiler-----";
        for (String name : classNames) {
            cs[idx++] = name;
        }

        return cs;
    }

    public void delClasses() {
        classNames.clear();
        getClassNames();
    }

    boolean test = false;

    List<Recognition> recognizeImage(Bitmap bitmap, Matrix matrix, Context context) {
        synchronized (this) {
            Pair faces[] = mtcnn.detect(bitmap);

            final List<Recognition> mappedRecognitions = new LinkedList<>();

            for (Pair face : faces) {
                RectF rectF = (RectF) face.first;

                Rect rect = new Rect();
                rectF.round(rect);

                FloatBuffer buffer = faceNet.getEmbeddings(bitmap, rect);
                Pair<Integer, Float> pair = svm.predict(buffer);

                matrix.mapRect(rectF);
                Float prob = pair.second;

                String name;
                if (prob > THRESHOLD) {

                    name = classNames.get(pair.first);
                    test = true;

                } else {
                    test = false;
                    name = "Bilinemiyor";

                }
                sendLog(name, context);

                Recognition result =
                        new Recognition("" + pair.first, name, prob, rectF);
                mappedRecognitions.add(result);
            }
            return mappedRecognitions;
        }

    }

    public boolean sendTest() {
        return test;
    }

    void updateData(int label, ContentResolver contentResolver, ArrayList<Uri> uris) throws Exception {
        synchronized (this) {
            ArrayList<float[]> list = new ArrayList<>();

            for (Uri uri : uris) {
                Bitmap bitmap = getBitmapFromUri(contentResolver, uri);
                Pair faces[] = mtcnn.detect(bitmap);

                float max = 0f;
                Rect rect = new Rect();

                for (Pair face : faces) {
                    Float prob = (Float) face.second;
                    if (prob > max) {
                        max = prob;
                        test = false;
                        RectF rectF = (RectF) face.first;
                        rectF.round(rect);
                    }
                }

                float[] emb_array = new float[EMBEDDING_SIZE];
                faceNet.getEmbeddings(bitmap, rect).get(emb_array);
                list.add(emb_array);
            }

            svm.train(label, list);
        }
    }

    int addPerson(String name) {
        FileUtils.appendText(name, FileUtils.LABEL_FILE);
        classNames.add(name);
        return classNames.size();
    }

    private Bitmap getBitmapFromUri(ContentResolver contentResolver, Uri uri) throws Exception {
        ParcelFileDescriptor parcelFileDescriptor =
                contentResolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();

        return bitmap;
    }

    void enableStatLogging(final boolean debug) {
    }

    String getStatString() {
        return faceNet.getStatString();
    }

    void close() {
        mtcnn.close();
        faceNet.close();
    }


    String temp = "";

    void sendLog(String name, Context ctx) {
        String str = "";
        Date CurrentDate = Calendar.getInstance().getTime();

        if (name == temp) {

        } else if (name == "Bilinemiyor") {

        } else {
            Log.i("TEST//Giriş yaptı  ", name + "  " + CurrentDate);
            str = name + " " + CurrentDate + "\n";
            addToFile(str);
            //readData();
        }
        temp = name;

    }

    File file1 = new File("/storage/emulated/0/NoProcessData.txt");

    void addToFile(String str) {
        try {

            FileWriter wrtr = new FileWriter(file1, true);
            BufferedWriter bw = new BufferedWriter(wrtr);
            bw.write(str);
            bw.close();
            Log.i("TEST// Dosya ", "Dosyaya kaydedildi " + str);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    UsbManager usbmanager;
    UsbDevice usbdevice;
    UsbDeviceConnection usbDeviceConnection;

    void burn() {
    }

    public File file = new File("/storage/emulated/0/NoProcessData.txt");
    public File report = new File("/storage/emulated/0/ProcessData.txt");
    Boolean isOpen = false;
    //DATABASE
    public FirebaseDatabase database;
    public DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Datas");
    private ArrayList<ArrayList<String>> List = new ArrayList<>();


    public void readData() {
        DataModel dm = new DataModel();
        ArrayList<String> DataList = new ArrayList<>();
        String data, nameData, dateData, result;
        String[] datas;
        Date time;

        int x = 0;
        try {
            Scanner read = new Scanner(file);
            while (read.hasNext()) {
                data = read.nextLine();
                datas = data.split(" ");
                for (int i = 0; i < datas.length; i++) {
                    DataList.add(i, datas[i]);
                }
                dateData = DataList.get(4) + DataList.get(3) + DataList.get(7);
                kontrol(dm.getName());
                dm.setName(DataList.get(0) + " " + DataList.get(1));
                dm.setTime(DataList.get(5));
                dm.setDate(dateData);
                reference.push().setValue(dm);

                result = DataList.get(0) + " " + DataList.get(1) + "\nSaat : " + DataList.get(5) + "\nTarih : " + dateData + "\n\n";

                FileWriter wrtr = new FileWriter(report, true);
                BufferedWriter bw = new BufferedWriter(wrtr);
                bw.write(result);
                Log.i("TEST// Dosya ", "Dosyaya kaydedildi " + result);
                bw.close();
                //Toast.makeText(getApplicationContext(), List.get(0).get(0) + List.get(0).get(1), Toast.LENGTH_LONG).show();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // MakeReport();
        List.clear();
    }

    void kontrol(String name) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (name == ds.child("name").getValue()) {
                        Log.i("Database ", name + " Kayıtlı");
                        Log.i("Database ", ds.getRef().toString());
                        Log.i("Database2 ", ds.getValue().toString());
                        ds.getRef().removeValue();
                    } else {
                        Log.i("Database ", name + " Kayıtlı Değil");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}