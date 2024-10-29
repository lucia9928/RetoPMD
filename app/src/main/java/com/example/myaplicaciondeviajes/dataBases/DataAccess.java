package com.example.myaplicaciondeviajes.dataBases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.example.myaplicaciondeviajes.model.Viaje;

public class DataAccess {
    private static final String DATABASE_NAME = "viajes.db";
    private static final String TABLE_VIAJES = "viajes";
    private static final String TABLE_ARCHIVOS = "archivos";
    private SQLiteDatabase database;

    // Sentencias SQL para crear tablas
    private static final String CREATE_TABLE_VIAJES = "CREATE TABLE IF NOT EXISTS " + TABLE_VIAJES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nombre TEXT NOT NULL, " +
            "duracion TEXT NOT NULL);";

    private static final String CREATE_TABLE_ARCHIVOS = "CREATE TABLE IF NOT EXISTS " + TABLE_ARCHIVOS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "viajeId INTEGER, " +
            "tipo TEXT NOT NULL, " +
            "ruta TEXT NOT NULL, " +
            "FOREIGN KEY(viajeId) REFERENCES " + TABLE_VIAJES + "(id) ON DELETE CASCADE ON UPDATE CASCADE);";

    public DataAccess(Context context) {
        // Abrir o crear la base de datos
        try {
            database = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
            createTables();
        } catch (SQLiteException e) {
            Log.e("DataAccess", "Error al abrir o crear la base de datos", e);
        }
    }

    // Método para crear las tablas manualmente
    private void createTables() {
        try {
            database.execSQL(CREATE_TABLE_VIAJES);
            database.execSQL(CREATE_TABLE_ARCHIVOS);
            Log.d("DataAccess", "Tablas creadas correctamente.");
        } catch (SQLException e) {
            Log.e("DataAccess", "Error al crear tablas", e);
        }
    }

    // Método para cerrar la base de datos
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    // Inserta un nuevo viaje en la base de datos
    public void insertViaje(long id, String nombre, String duracion) {
        ContentValues values = new ContentValues();
        Cursor select =database.rawQuery("select * from sqlite_sequence", null);
        id=Long.parseLong(select.toString());
        values.put("id", id);;
        values.put("nombre", nombre);
        values.put("duracion", duracion);
        // Insertar el nuevo registro y obtener el ID autogenerado
        database.execSQL("INSERT INTO TABLE_VIAJES VALUES (?,?,?)");

        // Verificar si se generó un ID válido
       if (id == -1) {
            Log.e("DataAccess", "Error al insertar el viaje.");
        } else {
            Log.d("DataAccess", "Viaje insertado con ID: " + id);
        }

    }

    // Inserta un archivo asociado a un viaje
    public long insertArchivo(long viajeId, String tipo, String ruta) {
        ContentValues values = new ContentValues();
        values.put("viajeId", viajeId);
        values.put("tipo", tipo);
        values.put("ruta", ruta);

        return database.insert(TABLE_ARCHIVOS, null, values);
    }

    // Obtiene todos los viajes de la base de datos
    public List<Viaje> getAllViajes() {
        List<Viaje> viajes = new ArrayList<>();
        Cursor cursor = null;

        try {
            // Usar rawQuery para obtener todos los viajes
            String sql = "SELECT id, nombre, duracion FROM " + TABLE_VIAJES;
            cursor = database.rawQuery(sql, null);

            // Verifica si el cursor no es nulo y mueve el cursor a la primera fila
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id=cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    String duracion = cursor.getString(cursor.getColumnIndexOrThrow("duracion"));
                    if (nombre != null && duracion != null) {
                        Viaje viaje = new Viaje(id,nombre, duracion);
                        viajes.add(viaje);
                    } else {
                        Log.d("DataAcces", "Viaje omitido por tener campos nulos");
                    }
                } while (cursor.moveToNext()); // Mueve el cursor a la siguiente fila
            } else {
                Log.d("", "No hay viajes en la base de datos.");
            }
            return viajes;
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close(); // Asegúrate de cerrar el cursor
            }
        }
        return viajes;
    }

}

