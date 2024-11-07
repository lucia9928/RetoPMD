package com.example.myaplicaciondeviajes;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myaplicaciondeviajes.R;

import dataBases.DataAccess;
import model.Viaje;

public class DetallesViajes extends AppCompatActivity {

    private AutoCompleteTextView nombreTextView;
    private AutoCompleteTextView duracionTextView;
    private DataAccess dataAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_viajes);

        // Inicializar DataAccess y elementos de UI
        dataAccess = new DataAccess(this);
        nombreTextView = findViewById(R.id.autoComTexNombre);
        duracionTextView = findViewById(R.id.autoComTextDuracion);

        // Obtener el ID del viaje desde el Intent
        long viajeId = getIntent().getLongExtra("viajeId", 1);
        if (viajeId != -1) {
            // Cargar y mostrar los datos del viaje
            cargarDatosDelViaje(viajeId);
        }
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
