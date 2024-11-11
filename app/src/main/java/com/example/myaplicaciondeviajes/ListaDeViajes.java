package com.example.myaplicaciondeviajes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataBases.DataAccess;
import model.Viaje;

public class ListaDeViajes extends AppCompatActivity {

    private Spinner viajesSpinner;
    private DataAccess data;
    private ImageButton eliminarButton;
    private Button detallesButon;
    private long id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_de_viajes); // Asegúrate de que este layout exista

        // Inicializar el Spinner
        viajesSpinner = findViewById(R.id.spinner);
        eliminarButton = findViewById(R.id.deleteButton);
        detallesButon=findViewById(R.id.detallesButon);
        // Inicializar el acceso a datos
        data = new DataAccess(this);

        // Cargar los viajes en el Spinner
        loadViajesIntoSpinner();
        // Configurar el botón de eliminación
        eliminarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarViajeSeleccionado();
            }
        });
        detallesButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detalleIntent= new Intent(ListaDeViajes.this, DetallesViajes.class);
                String nombreViaje = viajesSpinner.getSelectedItem().toString();
                long id=data.recuperarIdViajePorNombre(nombreViaje);


                detalleIntent.putExtra("viajeId",id);

                startActivity(detalleIntent);
            }
        });
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
    private void eliminarViajeSeleccionado() {
        String nombreViajeSeleccionado = (String) viajesSpinner.getSelectedItem();

        if (nombreViajeSeleccionado == null || nombreViajeSeleccionado.equals("No hay viajes disponibles")) {
            Toast.makeText(this, "No hay viajes para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Eliminar el viaje seleccionado usando DataAccess
        boolean eliminado = data.deleteViajeByName(nombreViajeSeleccionado);

        if (eliminado) {
            Toast.makeText(this, "Viaje eliminado exitosamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al eliminar el viaje", Toast.LENGTH_SHORT).show();
        }

        // Recargar el Spinner para reflejar los cambios
        loadViajesIntoSpinner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        data.close(); // Cerrar la base de datos al destruir la actividad
    }
}