package com.example.gm_face_ai.FaceRecognizer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gm_face_ai.R;

public class StartActivity extends AppCompatActivity {
    final boolean[] CameraW2 = {true};// True is Front Cam , false is Back Cam.

    ImageButton btnFront;
    ImageButton btnBack;
    Button btnDevam;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_switch);
        btnFront = findViewById(R.id.btnFront);
        btnBack = findViewById(R.id.btnBack);
        btnDevam = findViewById(R.id.btnDevam);
        btnFront.setBackgroundResource(R.drawable.background_selected);
        btnFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnFront.setBackgroundResource(R.drawable.background_selected);
                btnBack.setBackgroundResource(R.drawable.background_unselected);
                CameraW2[0] = true;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnFront.setBackgroundResource(R.drawable.background_unselected);
                btnBack.setBackgroundResource(R.drawable.background_selected);
                CameraW2[0] = false;

            }
        });

        btnDevam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("CameraWay2", CameraW2[0]);
                startActivity(intent);
            }
        });

    }

}