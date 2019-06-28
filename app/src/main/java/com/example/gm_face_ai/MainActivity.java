package com.example.gm_face_ai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gm_face_ai.FaceDetection.FaceTrackerActivity;
import com.example.gm_face_ai.FaceSpotter.FaceActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView txtInfo= findViewById(R.id.textInfo);
        txtInfo.setText("GENERAL MOBİLE AR-GE MERKEZİNDE GM Çalışanları Tarafından Oluşturuldu. \n Daha Fazla Bilgi için : https://github.com/general-mobile ");


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
    }
}
