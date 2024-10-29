package com.example.myaplicaciondeviajes.model;

public class Viaje {
    private String nombre;
    private String duracion;
    private long idViaje;
    // Constructor


    public Viaje(long id, String nombre, String duracion) {
        this.idViaje=id;
        this.nombre=nombre;
        this.duracion=duracion;
    }


    // Getters y Setters
    public String getNombre() { return nombre; }
    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) {
        this.duracion = duracion;
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
    public long getIdViaje() {
        return idViaje;
    }

    public void setIdViaje(long idViaje) {
        this.idViaje = idViaje;
    }
}

