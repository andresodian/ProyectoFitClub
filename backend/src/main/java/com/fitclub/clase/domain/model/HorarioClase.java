package com.fitclub.clase.domain.model;

import java.time.OffsetDateTime;

public class HorarioClase {
    private Long horarioClaseId;
    private Long claseId;
    private Long instructorId;
    private OffsetDateTime fechaHoraInicio;
    private OffsetDateTime fechaHoraFin;
    private Integer cupoMaximo;
    private String salon;
    private EstadoHorarioClase estado;

    public HorarioClase() {
    }

    public HorarioClase(Long horarioClaseId, Long claseId, Long instructorId, OffsetDateTime fechaHoraInicio,
                         OffsetDateTime fechaHoraFin, Integer cupoMaximo, String salon, EstadoHorarioClase estado) {
        this.horarioClaseId = horarioClaseId;
        this.claseId = claseId;
        this.instructorId = instructorId;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.cupoMaximo = cupoMaximo;
        this.salon = salon;
        this.estado = estado;
    }

    public Long getHorarioClaseId() {
        return horarioClaseId;
    }

    public void setHorarioClaseId(Long horarioClaseId) {
        this.horarioClaseId = horarioClaseId;
    }

    public Long getClaseId() {
        return claseId;
    }

    public void setClaseId(Long claseId) {
        this.claseId = claseId;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public OffsetDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(OffsetDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public OffsetDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(OffsetDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public String getSalon() {
        return salon;
    }

    public void setSalon(String salon) {
        this.salon = salon;
    }

    // Antes era String; ahora es EstadoHorarioClase (enum) para que solo acepte
    // PROGRAMADA, CANCELADA o FINALIZADA — los únicos valores válidos según ck_horario_clase_estado.
    public EstadoHorarioClase getEstado() {
        return estado;
    }

    public void setEstado(EstadoHorarioClase estado) {
        this.estado = estado;
    }
}
