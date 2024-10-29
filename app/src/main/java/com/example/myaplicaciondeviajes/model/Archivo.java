package com.example.myaplicaciondeviajes.model;

public class Archivo {
    private long id;
    private long viajeId;
    private String tipo;
    private String ruta;
    public Archivo(long viajeId, String tipo, String ruta) {
        this.viajeId = viajeId;
        this.tipo = tipo;
        this.ruta = ruta;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getViajeId() {
        return viajeId;
    }
    public void setViajeId(long viajeId) {
        this.viajeId = viajeId;
    }
    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
