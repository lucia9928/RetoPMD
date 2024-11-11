package com.example.myaplicaciondeviajes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import model.Archivo;

public class Galeria extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria);

        ImageView imageView = findViewById(R.id.imageView);

        // Obtiene el objeto Archivo del Intent y verifica si es nulo
        Archivo archivo = (Archivo) getIntent().getSerializableExtra("objeto");
        if (archivo != null) {
            String imagen = archivo.getRuta();
            cargarImagen(imagen, imageView);
        } else {
            // Muestra un mensaje si no se recibió el objeto
            Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarImagen(String imagen, ImageView imageView) {
        try {
            // Convierte la ruta de la imagen en un objeto File
            File imgFile = new File(imagen);
            if (imgFile.exists()) {
                // Decodifica el archivo en un Bitmap con opciones para optimizar
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // Escala la imagen para reducir tamaño (ajústalo según tus necesidades)
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                // Establece el Bitmap en el ImageView
                imageView.setImageBitmap(myBitmap);
            } else {
                Toast.makeText(this, "Archivo de imagen no encontrado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}
