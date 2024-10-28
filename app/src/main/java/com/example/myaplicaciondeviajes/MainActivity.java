package com.example.myaplicaciondeviajes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Button createViajeButon= findViewById(R.id.button3);
        createViajeButon.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent (MainActivity.this, CrearNuevoViaje.class);
        //startActivities(intent);
    }
}