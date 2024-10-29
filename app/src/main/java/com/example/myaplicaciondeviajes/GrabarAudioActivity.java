package com.example.myaplicaciondeviajes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

public class GrabarAudioActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView timerText;
   private ImageButton stopButton, recordButton, playButton;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String outputFile;
    private boolean isRecording = false;
    private int seconds = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabar_audio);
        // Obtener la ruta del archivo de audio desde el Intent
        outputFile = getIntent().getStringExtra("audioFilePath");

        if (outputFile == null) {
            Toast.makeText(this, "Error: no se recibió la ruta del archivo", Toast.LENGTH_SHORT).show();
            finish(); // Cerrar la actividad si no hay ruta de archivo
            return;
        }
        // Asignación de vistas
        timerText = findViewById(R.id.timerText);
        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);
        playButton = findViewById(R.id.playButton);

        // Configuración del archivo de salida
        outputFile = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";

        // Solicitar permisos si no están otorgados
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            // Configurar botones
            recordButton.setOnClickListener(this);
            stopButton.setOnClickListener(this);
            playButton.setOnClickListener(this);
            // Desactivar botones que no deberían usarse al inicio
            stopButton.setEnabled(false);
            playButton.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.recordButton) {
            startRecording();
        } else if (view.getId() == R.id.stopButton) {
            stopRecording();
        } else if (view.getId() == R.id.playButton) {
            playRecording();
        }
    }

    private boolean checkPermissions() {
        int recordPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int storagePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return recordPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        }
    }

    private void startRecording() {
        if (isRecording) return;

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFile);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            startTimer();
            recordButton.setEnabled(false);
            stopButton.setEnabled(true);
            playButton.setEnabled(false);
            Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("AudioError", "Error al preparar o iniciar la grabación", e);
            Toast.makeText(this, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e("AudioError", "Error al detener la grabación", e);
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            stopTimer();
            recordButton.setEnabled(true);
            stopButton.setEnabled(false);
            playButton.setEnabled(true);

            // Verificar si el archivo de audio se ha creado correctamente
            File file = new File(outputFile);
            if (file.exists()) {
                Toast.makeText(this, "Grabación detenida y guardada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: archivo de audio no encontrado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playRecording() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(outputFile);  // Usar el archivo de salida configurado
            mediaPlayer.prepare();  // Preparar antes de reproducir
            mediaPlayer.start();
            Toast.makeText(this, "Reproduciendo grabación", Toast.LENGTH_SHORT).show();

            // Deshabilitar el botón de reproducción mientras se reproduce
            playButton.setEnabled(false);

            // Habilitar el botón de reproducción cuando finalice
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    playButton.setEnabled(true);
                }
            });
        } catch (IOException e) {
            Log.e("AudioError", "Error al preparar o reproducir el audio", e);
            Toast.makeText(this, "Error al reproducir la grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        seconds = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                seconds++;
                timerText.setText(formatTime(seconds));
                if (isRecording) {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void stopTimer() {
        handler.removeCallbacksAndMessages(null);
        timerText.setText("00:00:00");
    }

    private String formatTime(int sec) {
        int hrs = sec / 3600;
        int mins = (sec % 3600) / 60;
        int secs = sec % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos otorgados", Toast.LENGTH_SHORT).show();
                recordButton.setEnabled(true);
                stopButton.setEnabled(false);
                playButton.setEnabled(false);
            } else {
                Toast.makeText(this, "Permisos denegados. La grabación no es posible.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
