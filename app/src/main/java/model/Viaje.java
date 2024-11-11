package model;

import java.io.Serializable;

public class Viaje implements Serializable {


    private long id;
    private String nombre;
    private String duracion;

    public Viaje(String nombre, String duracion) {

        this.nombre=nombre;
        this.duracion=duracion;
    }
    public Viaje( long id, String nombre, String duracion) {
        this.id=id;
        this.nombre=nombre;
        this.duracion=duracion;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Viaje{" +
                "nombre='" + nombre + '\'' +
                ", duracion='" + duracion + '\'' +
                '}';
    }

}

