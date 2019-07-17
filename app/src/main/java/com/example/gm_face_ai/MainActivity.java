package com.example.gm_face_ai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gm_face_ai.FaceDetection.FaceTrackerActivity;
import com.example.gm_face_ai.FaceRecognizer.StartActivity;
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
        sb = Snackbar.make(container, "Bu Hizmet şuan da geliştiriliyor...", Snackbar.LENGTH_INDEFINITE);


        ImageButton btnFaceDet = findViewById(R.id.btnFaceDet);
        btnFaceDet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ", "Button Clicked");
                Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
                startActivity(intent);

            }
        });
        ImageButton btnFaceSpot = findViewById(R.id.btnFaceDet2);
        btnFaceSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ", "Button Clicked");
                Intent intent = new Intent(getApplicationContext(), FaceActivity.class);
                startActivity(intent);

            }
        });
        ImageButton btnFaceRec = findViewById(R.id.btnFaceDet3);
        btnFaceRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);

            }
        });

        ImageButton btnLandmark = findViewById(R.id.btnFaceDet4);
        btnLandmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sb.setAction("Tamam", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sb.dismiss();
                    }
                });
                sb.show();
/*                Intent intent = new Intent(getApplicationContext(), com.example.gm_face_ai.SpeechCommands.FaceTrackerActivity.class);
                startActivity(intent);*/

            }
        });



    }
}

/*  SnackBar

sb.setAction("Tamam", new View.OnClickListener() {
@Override
public void onClick(View view) {
        sb.dismiss();
        }
        });
        sb.show();*/
