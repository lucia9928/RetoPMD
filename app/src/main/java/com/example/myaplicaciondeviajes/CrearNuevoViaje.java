package com.example.myaplicaciondeviajes;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dataBases.DataAccess;
import model.Archivo;
import model.Viaje;

public class CrearNuevoViaje extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText nombreTexImp;
    private TextInputEditText duracionTexImp;
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
    private long viajeId; // ID del viaje que se guardará
    private Uri videoURI; // Para almacenar la URI del video
    List<Archivo> archivosAsociados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_nuevo_viaje);

        // Configuración de UI inmersiva usando WindowInsetsController
        setupFullscreenUI();

        dataAccess = new DataAccess(this);
        nombreTexImp = findViewById(R.id.textImputNombre);
        duracionTexImp = findViewById(R.id.textImputDuracion);
        audiosButton = findViewById(R.id.btnAgregarAudios);
        guardarButton = findViewById(R.id.btnGuardar);
        imagenesButton = findViewById(R.id.btnAgregarFotos);
        videosButton = findViewById(R.id.btnAgregarVideos);
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
                            Log.e("CrearNuevoViaje", "No se pudo tomar la foto o photoURI es nulo");
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
                            Log.e("CrearNuevoViaje", "No se pudo grabar el video o videoURI es nulo");
                        }
                    }
                });
    }


    private void setupFullscreenUI() {
        WindowInsetsController insetsController = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            insetsController = getWindow().getInsetsController();
        }
        if (insetsController != null) {
            // Oculta las barras de estado y navegación
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
            // Configura el comportamiento para que las barras solo reaparezcan si el usuario desliza
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
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

    private void checkPermissionsAndRecordImagen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_IMAGE_CAPTURE_PERMISSION);
        }else{
            dispatchTakePictureIntent();
        }

    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return File.createTempFile(videoFileName, ".mp4", storageDir);
    }

    private void guardarRutaDeVideo(String videoPath) {
        if (viajeId != -1) {
            Archivo nuevoArchivo = new Archivo(viajeId, "video", videoURI.toString());
            long newRowId = dataAccess.insertArchivo(nuevoArchivo.getViajeId(), nuevoArchivo.getTipo(), nuevoArchivo.getRuta());
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
            checkPermissionsAndRecordImagen();
        } else if (v.getId() == R.id.btnAgregarVideos) {
            checkPermissionsAndRecordVideo();
        } else if (v.getId() == R.id.btnAgregarAudios) {
            Intent intent = new Intent(this, GrabarAudioActivity.class);
            String outputFilePath = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";
            intent.putExtra("audioFilePath", outputFilePath);
            startActivity(intent);
        } else if (v.getId() == R.id.btnGuardar) {
            guardarViaje();
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile();
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
                Log.e("CrearNuevoViaje", "videoFile es nulo");
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("CrearNuevoViaje", "Error al crear el archivo de imagen", ex);
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.myaplicaciondeviajes.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(takePictureIntent);
            } else {
                Log.e("CrearNuevoViaje", "photoFile es nulo");
            }
        }else {
            Log.e("CrearNuevoViaje", "Intent no resuelto para la cámara");
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void guardarRutaDeImagen(String imagePath) {
        if (viajeId != -1) {
            Archivo nuevoArchivo = new Archivo(viajeId, "imagen", photoURI.toString());
            long newRowId = dataAccess.insertArchivo(nuevoArchivo.getViajeId(), nuevoArchivo.getTipo(), nuevoArchivo.getRuta());
            if (newRowId == -1) {
                Log.e("CrearNuevoViaje", "Error al insertar la imagen en la base de datos");
            } else {
                Log.i("CrearNuevoViaje", "Imagen guardada en la base de datos con ID: " + newRowId);
            }
        } else {
            Log.e("CrearNuevoViaje", "El viajeId no es válido");
        }
    }

    private void guardarViaje() {
        String nombre = nombreTexImp.getText().toString();
        String duracion = duracionTexImp.getText().toString();

        // Validar que los campos no estén vacíos
        if (nombre.isEmpty() || duracion.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insertar en la base de datos y obtener el ID del viaje
        viajeId = dataAccess.insertViaje(nombre, duracion);

        if (viajeId == -1) {
            Toast.makeText(this, "Error al guardar el viaje", Toast.LENGTH_SHORT).show();
        } else {
            // Guardar las rutas y tipos de los archivos asociados al viaje
            for (Archivo archivo : archivosAsociados) {  // archivosAsociados es una lista de archivos a guardar
                dataAccess.insertArchivo(viajeId, archivo.getTipo(), archivo.getRuta());
            }

            Toast.makeText(this, "Viaje guardado exitosamente con ID: " + viajeId, Toast.LENGTH_SHORT).show();
        }

        // Limpiar los campos después de guardar
        nombreTexImp.setText("");
        duracionTexImp.setText("");
        archivosAsociados.clear(); // Limpia la lista de archivos después de guardar
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataAccess.close(); // Cerrar la base de datos al destruir la actividad
    }
}