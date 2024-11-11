package dataBases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import model.Archivo;
import model.Viaje;

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
    public long insertViaje(String nombre, String duracion) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("duracion", duracion);

        // Insertar en la base de datos y devolver el ID autogenerado
        long viajeId = database.insert("viajes", null, values);
        if (viajeId == -1) {
            Log.e("DataAccess", "Error al insertar el viaje.");
        } else {
            Log.d("DataAccess", "Viaje insertado con ID: " + viajeId);
        }

        return viajeId;  // Devuelve el ID del viaje recién creado
    }

    // Inserta un archivo asociado a un viaje
    // Inserta un archivo asociado a un viaje
    public long insertArchivo(long viajeId, String tipo, String ruta) {
        ContentValues values = new ContentValues();
        values.put("viaje_id", viajeId);
        values.put("tipo", tipo);
        values.put("ruta", ruta);

        // Insertar el archivo en la tabla de archivos
        long archivoId = database.insert("archivos", null, values);

        if (archivoId == -1) {
            Log.e("DataAccess", "Error al insertar el archivo.");
        } else {
            Log.d("DataAccess", "Archivo insertado con ID: " + archivoId);
        }
        return archivoId; // Corregido
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
                    Log.e("asd", nombre);
                    if (nombre != null && duracion != null) {
                        Viaje viaje = new Viaje(id, nombre, duracion);
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
    // Método para eliminar un viaje por nombre
    public boolean deleteViajeByName(String nombreViaje) {
        int rowsAffected = database.delete(TABLE_VIAJES, "nombre = ?", new String[]{nombreViaje});
        if (rowsAffected > 0) {
            Log.d("DataAccess", "Viaje eliminado: " + nombreViaje);
            return true;
        } else {
            Log.e("DataAccess", "Error al eliminar el viaje: " + nombreViaje);
            return false;
        }
    }
    public Viaje getViajeById(long id) {
        Viaje viaje = null;
        Cursor cursor = null;

        try {
            // Consulta SQL para obtener el viaje por ID
            String sql = "SELECT nombre, duracion FROM " + TABLE_VIAJES + " WHERE id = ?";
            cursor = database.rawQuery(sql, new String[]{String.valueOf(id)});

            // Si encontramos el viaje, extraemos sus datos
            if (cursor != null && cursor.moveToFirst()) {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String duracion = cursor.getString(cursor.getColumnIndexOrThrow("duracion"));

                // Crear el objeto Viaje con los datos obtenidos
                viaje = new Viaje(id, nombre, duracion);
            } else {
                Log.d("DataAccess", "No se encontró un viaje con ID: " + id);
            }
        } catch (SQLiteException e) {
            Log.e("DataAccess", "Error al obtener el viaje con ID: " + id, e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Asegurarse de cerrar el cursor
            }
        }

        return viaje; // Retorna el viaje o null si no se encontró
    }

    public Archivo recuperarArchivo(long id) {
        Archivo archivo = null;
        Cursor cursor = null;

        try {
            // Consulta SQL con marcador de posición
            String sql = "SELECT viajeId, tipo, ruta FROM " + TABLE_VIAJES + " WHERE viajeId = ?";

            // Ejecuta la consulta pasando el ID como parámetro
            cursor = database.rawQuery(sql, new String[]{String.valueOf(id)});

            // Verifica si el cursor tiene resultados y obtén los datos
            if (cursor != null && cursor.moveToFirst()) {
                long viajeID = cursor.getLong(cursor.getColumnIndexOrThrow("viajeId"));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));
                String ruta = cursor.getString(cursor.getColumnIndexOrThrow("ruta"));

                // Crear el objeto Archivo con los datos obtenidos
                archivo = new Archivo(viajeID, tipo, ruta);
            }
        } catch (SQLiteException e) {
            Log.e("DataAccess", "Error al obtener el archivo: ", e);
        } finally {
            // Asegúrate de cerrar el cursor para liberar los recursos
            if (cursor != null) {
                cursor.close();
            }
        }

        return archivo;
    }


    public long recuperarIdViajePorNombre(String nombre) {
        long viajeID = -1;  // Asigna un valor por defecto si no se encuentra el viaje
        Cursor cursor = null;

        try {
            // Usa un marcador de posición para el valor de nombre
            String sql = "SELECT id, nombre, duracion FROM " + TABLE_VIAJES + " WHERE nombre = ?";

            // Ejecuta la consulta pasando el parámetro
            cursor = database.rawQuery(sql, new String[]{nombre});

            // Verifica si el cursor tiene resultados y obtén el ID
            if (cursor != null && cursor.moveToFirst()) {
                viajeID = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (SQLiteException e) {
            Log.d("DataAccess", "No se encontró un viaje con nombre: " + nombre, e);
        } finally {
            // Asegúrate de cerrar el cursor para liberar los recursos
            if (cursor != null) {
                cursor.close();
            }
        }

        return viajeID;  // Devuelve el ID encontrado o -1 si no se encontró
    }
}

