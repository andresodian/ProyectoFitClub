package com.fitclub.clase.domain.model;

import java.time.OffsetDateTime;

public class ReservaClase {
    private Long reservaClaseId;
    private Long membresiaId;
    private Long horarioClaseId;
    private OffsetDateTime fechaReserva;
    private OffsetDateTime fechaCancelacion;
    private String estado;

    public ReservaClase() {
    }

    public ReservaClase(Long reservaClaseId, Long membresiaId, Long horarioClaseId,
                        OffsetDateTime fechaReserva, OffsetDateTime fechaCancelacion, String estado) {
        this.reservaClaseId = reservaClaseId;
        this.membresiaId = membresiaId;
        this.horarioClaseId = horarioClaseId;
        this.fechaReserva = fechaReserva;
        this.fechaCancelacion = fechaCancelacion;
        this.estado = estado;
    }

    public Long getReservaClaseId() {
        return reservaClaseId;
    }

    public void setReservaClaseId(Long reservaClaseId) {
        this.reservaClaseId = reservaClaseId;
    }

    public Long getMembresiaId() {
        return membresiaId;
    }

    public void setMembresiaId(Long membresiaId) {
        this.membresiaId = membresiaId;
    }

    public Long getHorarioClaseId() {
        return horarioClaseId;
    }

    public void setHorarioClaseId(Long horarioClaseId) {
        this.horarioClaseId = horarioClaseId;
    }

    public OffsetDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(OffsetDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public OffsetDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(OffsetDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}