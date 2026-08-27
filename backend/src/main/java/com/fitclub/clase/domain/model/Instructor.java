package com.fitclub.clase.domain.model;

public class Instructor {
    private Long instructorId;
    private String numeroDocumento;
    private String nombreCompleto;
    private String especialidad;
    private String email;
    private String telefono;
    private String turno;
    private Boolean activo;

    public Instructor() {
    }

    public Instructor(Long instructorId, String numeroDocumento, String nombreCompleto, String especialidad,
                       String email, String telefono, String turno, Boolean activo) {
        this.instructorId = instructorId;
        this.numeroDocumento = numeroDocumento;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.email = email;
        this.telefono = telefono;
        this.turno = turno;
        this.activo = activo;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // Turno de trabajo del instructor (ej. "MAÑANA", "TARDE", "NOCHE").
    // Columna nullable en `instructor`; faltaba aquí.
    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
