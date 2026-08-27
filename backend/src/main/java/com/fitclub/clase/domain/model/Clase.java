package com.fitclub.clase.domain.model;

public class Clase {
    private Long claseId;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private String intensidad;
    private Boolean activa;

    public Clase() {
    }

    public Clase(Long claseId, String nombre, String descripcion, Integer duracionMinutos,
                 String intensidad, Boolean activa) {
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

    // Reemplaza al antiguo "capacidad", que no existe como columna en la tabla `clase`.
    // duracion_minutos sí existe y es NOT NULL.
    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    // Valores válidos según ck_clase_intensidad: null, "BAJA", "MEDIA" o "ALTA".
    public String getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(String intensidad) {
        this.intensidad = intensidad;
    }

    // El campo se llama "activa" (no "activo") porque así se llama la columna real
    // de la tabla `clase`; así Spring Data JPA lo mapeará solo más adelante.
    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
