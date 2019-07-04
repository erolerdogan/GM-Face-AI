package com.example.gm_face_ai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.gm_face_ai.FaceDetection.FaceTrackerActivity;
import com.example.gm_face_ai.FaceSpotter.FaceActivity;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    final boolean[] CameraW2 = {true}; // True is Front Cam , false is Back Cam.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Snackbar sb;
        ConstraintLayout container = findViewById(R.id.container2);
        sb =  Snackbar.make(container, "Bu Hizmet şuan da geliştiriliyor...", Snackbar.LENGTH_INDEFINITE);



        ImageButton btnFaceDet = findViewById(R.id.btnFaceDet);
        btnFaceDet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ","Button Clicked");
                Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
                startActivity(intent);

            }
        });
        ImageButton btnFaceSpot = findViewById(R.id.btnFaceDet2);
        btnFaceSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ","Button Clicked");
                Intent intent = new Intent(getApplicationContext(), FaceActivity.class);
                startActivity(intent);

            }
        });
        ImageButton btnFaceRec = findViewById(R.id.btnFaceDet3);
        btnFaceRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ","Button Clicked");
                Intent intent = new Intent(getApplicationContext(), com.example.gm_face_ai.FaceRecognizer.MainActivity.class);
                intent.putExtra("CameraWay2",CameraW2[0]);
                startActivity(intent);

            }
        });

        ImageButton btnLandmark = findViewById(R.id.btnFaceDet4);
        btnLandmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sb.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sb.dismiss();
                    }
                });
                sb.show();

            }
        });

        Switch swCam = findViewById(R.id.swCamera);
        swCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (swCam.isChecked()){
                    Log.i("SWBUTTON : ","Back Cam Open");
                    CameraW2[0] =false;

                }
                else {
                    Log.i("SWBUTTON : ","Front Cam Open");
                    CameraW2[0] = true;
                }
            }
        });
    }
}
