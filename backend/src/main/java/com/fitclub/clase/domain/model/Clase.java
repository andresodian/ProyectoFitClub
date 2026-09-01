package com.fitclub.clase.domain.model;

public class Clase {
    private Long claseId;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private Intensidad intensidad;
    private Boolean activa;

    public Clase() {
    }

    public Clase(Long claseId, String nombre, String descripcion, Integer duracionMinutos,
                 Intensidad intensidad, Boolean activa) {
        this.claseId = claseId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionMinutos = duracionMinutos;
        this.intensidad = intensidad;
        this.activa = activa;
    }

    public Long getClaseId() {
        return claseId;
    }

    public void setClaseId(Long claseId) {
        this.claseId = claseId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    // Antes era String; ahora es Intensidad (enum). Sigue siendo nullable (ck_clase_intensidad
    // permite NULL), así que puede quedar sin asignar igual que antes.
    public Intensidad getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(Intensidad intensidad) {
        this.intensidad = intensidad;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
