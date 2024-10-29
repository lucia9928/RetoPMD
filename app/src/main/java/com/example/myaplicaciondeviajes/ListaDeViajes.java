package com.example.myaplicaciondeviajes;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import com.example.myaplicaciondeviajes.dataBases.DataAccess;
import com.example.myaplicaciondeviajes.model.Viaje;

public class ListaDeViajes extends AppCompatActivity {

    private Spinner viajesSpinner;
    private DataAccess data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_de_viajes); // Asegúrate de que este layout exista

        // Inicializar el Spinner
        viajesSpinner = findViewById(R.id.spinner);

        // Inicializar el acceso a datos
        data = new DataAccess(this);

        // Cargar los viajes en el Spinner
        loadViajesIntoSpinner();
    }
    private void loadViajesIntoSpinner() {
        List<Viaje> viajes = data.getAllViajes();

        // Crear una lista para los nombres de los viajes
        List<String> viajeNombres = new ArrayList<>();
        for (Viaje viaje : viajes) {
            if (viaje != null) { // Verificar que el objeto y el nombre no sean nulos
                viajeNombres.add(viaje.getNombre());
            } else {
                Log.d("ListaDeViajes", "Viaje o nombre de viaje es null");
            }
        }
        // Si la lista está vacía, se añade un mensaje predeterminado
        if (viajeNombres.isEmpty()) {
            viajeNombres.add("No hay viajes disponibles");
        }else {
            // Creamos un adaptador para el Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, viajeNombres);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Asignar el adaptador al Spinner
            viajesSpinner.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        data.close(); // Cerrar la base de datos al destruir la actividad
    }
}