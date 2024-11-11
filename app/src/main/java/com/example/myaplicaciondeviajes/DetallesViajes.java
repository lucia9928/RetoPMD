package com.example.myaplicaciondeviajes;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myaplicaciondeviajes.R;

import dataBases.DataAccess;
import model.Archivo;
import model.Viaje;

public class DetallesViajes extends AppCompatActivity {
    Button btnImagenes, btnVideos, btnAudios, btnAtras;
    private TextView nombreTextView;
    private TextView duracionTextView;
    private DataAccess dataAccess;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_viajes);
        //Dar valor a los botones
        btnImagenes=findViewById(R.id.btnImagenes);
        btnVideos=findViewById(R.id.btnVideos);
        btnAudios=findViewById(R.id.btnAudios);
        btnAtras=findViewById(R.id.btnAtras);

        // Inicializar DataAccess y elementos de UI
        dataAccess = new DataAccess(this);
        nombreTextView = findViewById(R.id.textNombre);
        duracionTextView = findViewById(R.id.textDuracion);

        // Obtener el ID del viaje desde el Intent

        long viajeId = getIntent().getLongExtra("viajeId", id);
        if (viajeId != -1) {
            // Cargar y mostrar los datos del viaje
            cargarDatosDelViaje(viajeId);
        }
        btnImagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DetallesViajes.this, Galeria.class);
                Archivo archivo=dataAccess.recuperarArchivo(viajeId);
                intent.putExtra("objeto", archivo);
                startActivity(intent);
            }
        });
        btnVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cargar los videos asociados al viaje y pasar a la galería
                Intent intent = new Intent(DetallesViajes.this, Galeria.class);
                // Pasar la lista de videos asociados a este viaje
                Archivo archivo = dataAccess.recuperarArchivo(viajeId);
                intent.putExtra("objeto", archivo);
                startActivity(intent);
            }
        });
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetallesViajes.this, ListaDeViajes.class);

                startActivity(intent);
            }
        });
    }



    private void cargarDatosDelViaje(long viajeId) {

        try {
            Viaje viaje = dataAccess.getViajeById(viajeId);
            if (viaje != null) {
                nombreTextView.setText(viaje.getNombre());
                duracionTextView.setText(viaje.getDuracion());
            } else {
                Toast.makeText(this, "No se encontró el viaje", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DetallesViajes", "Error al cargar datos del viaje", e);
            Toast.makeText(this, "Error al cargar datos del viaje", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataAccess.close();  // Cerrar la base de datos cuando la actividad se destruya
    }
}