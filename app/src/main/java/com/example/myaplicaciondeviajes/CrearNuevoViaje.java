package com.example.myaplicaciondeviajes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.myaplicaciondeviajes.dataBases.DataAccess;
import com.example.myaplicaciondeviajes.model.Archivo;
import com.example.myaplicaciondeviajes.model.Viaje;


public class CrearNuevoViaje extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText nombreTextView;
    private TextInputEditText duracionTextView;
    private Button guardarButton;
    private Button imagenesButton;
    private Button videosButton;
    private Button audiosButton;
    private static final int REQUEST_IMAGE_CAPTURE_PERMISSION = 100;
    private Uri photoURI;
    private DataAccess dataAccess;
    private static final int REQUEST_VIDEO_CAPTURE_PERMISSION = 2;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> takeVideoLauncher;
    private long viajeId; // ID del viaje que se guardaráç
    private Uri videoURI; // Para almacenar la URI del video
    private Viaje nuevoViaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_nuevo_viaje);
        dataAccess = new DataAccess(this);

        nombreTextView = findViewById(R.id.textImputNombre);
        duracionTextView = findViewById(R.id.textImputDuracion);
        audiosButton=findViewById(R.id.btnAgregarAudios);
        guardarButton = findViewById(R.id.btnGuardar);
        imagenesButton = findViewById(R.id.btnAgregarFotos);
        videosButton=findViewById(R.id.btnAgregarVideos);
        guardarButton.setOnClickListener(this);
        imagenesButton.setOnClickListener(this);
        videosButton.setOnClickListener(this);
        audiosButton.setOnClickListener(this);
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && photoURI != null) {
                            // Guardar la ruta de la imagen en la base de datos
                            guardarRutaDeImagen(photoURI.toString());
                        } else {
                            Log.e("CrearNuevoViaje", "No se pudo tomar la foto o photoURI es nulo" );
                        }
                    }
                });
        takeVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && videoURI != null) {
                            guardarRutaDeVideo(videoURI.toString());
                        } else {
                            Log.e("CrearNuevoViaje", "No se pudo grabar el video o videoURI es nulo" );
                        }
                    }
                });

    }

    private void checkPermissionsAndRecordVideo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_VIDEO_CAPTURE_PERMISSION);
        } else {
            dispatchTakeVideoIntent();
        }
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss" ).format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return File.createTempFile(videoFileName, ".mp4", storageDir);
    }

    private void guardarRutaDeVideo(String videoPath) {
        if (viajeId != -1) {

            Archivo nuevoArchivo = new Archivo(viajeId, "video", videoURI.toString());
            long newRowId= dataAccess.insertArchivo(nuevoArchivo.getViajeId(), nuevoArchivo.getTipo(), nuevoArchivo.getRuta());
            if (newRowId == -1) {
                Log.e("CrearNuevoViaje", "Error al insertar el video en la base de datos");
            } else {
                Log.i("CrearNuevoViaje", "Video guardado en la base de datos con ID: " + newRowId);
            }
        } else {
            Log.e("CrearNuevoViaje", "El viajeId no es válido");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnAgregarFotos) {
            dispatchTakePictureIntent();
        }else if(v.getId() == R.id.btnAgregarVideos) {
            checkPermissionsAndRecordVideo();
        }else if (v.getId() == R.id.btnAgregarAudios) {
            Intent intent = new Intent(this, GrabarAudioActivity.class);
            String outputFilePath = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";
            intent.putExtra("audioFilePath", outputFilePath);
            startActivity(intent);
        }else if (v.getId() == R.id.btnGuardar) {
            guardarViaje();
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            // Crear archivo para el video
            File videoFile = null;
            try {
                videoFile = createVideoFile(); // Método que creas para generar el archivo
            } catch (IOException ex) {
                Log.e("CrearNuevoViaje", "Error al crear el archivo de video", ex);
            }
            if (videoFile != null) {
                videoURI = FileProvider.getUriForFile(this,
                        "com.example.myaplicaciondeviajes.fileprovider",
                        videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                takeVideoLauncher.launch(takeVideoIntent);
            } else {
                Log.e("CrearNuevoViaje", "videoFile es nulo" );
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crear archivo para la foto
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Método que creas para generar el archivo
            } catch (IOException ex) {
                // Manejar error
                Log.e("CrearNuevoViaje", "Error al crear el archivo de imagen", ex);
            }
            // Continuar solo si el archivo fue creado
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.myaplicaciondeviajes.fileprovider", // Asegúrate de que este es tu authority correcto
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(takePictureIntent);
            } else {
                Log.e("CrearNuevoViaje", "photoFile es nulo" );
            }
        }

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss" ).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void guardarRutaDeImagen(String imagePath) {
        if (viajeId != -1) {
            Archivo nuevoArchivo = new Archivo(viajeId, "imagen", photoURI.toString());
           long newRowId= dataAccess.insertArchivo(nuevoArchivo.getViajeId(), nuevoArchivo.getTipo(), nuevoArchivo.getRuta());
            if (newRowId == -1) {
                Log.e("CrearNuevoViaje", "Error al insertar la imagen en la base de datos" );
            } else {
                Log.i("CrearNuevoViaje", "Imagen guardada en la base de datos con ID: " + newRowId);
            }
        } else {
            Log.e("CrearNuevoViaje", "El viajeId no es válido" );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void guardarViaje() {

        String nombre = nombreTextView.getText().toString();
        String duracion = duracionTextView.getText().toString();

        // Validar que los campos no estén vacíos
        if (nombre.isEmpty() || duracion.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insertar en la base de datos y obtener el ID del viaje

     dataAccess.insertViaje(nuevoViaje.getIdViaje(), nuevoViaje.getNombre(), nuevoViaje.getDuracion());
        if (viajeId == -1) {
            Toast.makeText(this, "Error al guardar el viaje", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Viaje guardado exitosamente con ID: " + viajeId, Toast.LENGTH_SHORT).show();
        }

        // Limpiar los campos después de guardar
        nombreTextView.setText("" );
        duracionTextView.setText("" );


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataAccess.close(); // Cerrar la base de datos al destruir la actividad
    }
}



