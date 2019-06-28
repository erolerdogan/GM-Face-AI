package com.example.gm_face_ai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.gm_face_ai.FaceDetection.FaceTrackerActivity;
import com.example.gm_face_ai.FaceSpotter.FaceActivity;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txtInfo= findViewById(R.id.textInfo);
        txtInfo.setText("GENERAL MOBİLE AR-GE MERKEZİNDE GM Çalışanları Tarafından Oluşturuldu. \n Daha Fazla Bilgi için : https://github.com/general-mobile ");
        Snackbar sb;
        ConstraintLayout container = findViewById(R.id.container2);
        sb =  Snackbar.make(container, "Bu Hizmet şuan da geliştiriliyor...", Snackbar.LENGTH_INDEFINITE);



        Button btnFaceDet = findViewById(R.id.btnFaceDet);
        btnFaceDet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ","Button Clicked");
                Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
                startActivity(intent);

            }
        });
        Button btnFaceSpot = findViewById(R.id.btnFaceDet2);
        btnFaceSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ","Button Clicked");
                Intent intent = new Intent(getApplicationContext(), FaceActivity.class);
                startActivity(intent);

            }
        });
        Button btnFaceRec = findViewById(R.id.btnFaceDet3);
        btnFaceRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG: ","Button Clicked");
                Intent intent = new Intent(getApplicationContext(), com.example.gm_face_ai.FaceRecognizer.MainActivity.class);
                startActivity(intent);

            }
        });

        Button btnLandmark = findViewById(R.id.btnFaceDet4);
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
    }
}
