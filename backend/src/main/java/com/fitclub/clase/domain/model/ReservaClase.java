package com.fitclub.clase.domain.model;

import java.time.OffsetDateTime;

public class ReservaClase {
    private Long reservaClaseId;
    private Long membresiaId;
    private Long horarioClaseId;
    private OffsetDateTime fechaReserva;
    private OffsetDateTime fechaCancelacion;
    private EstadoReservaClase estado;

    public ReservaClase() {
    }

    public ReservaClase(Long reservaClaseId, Long membresiaId, Long horarioClaseId,
                        OffsetDateTime fechaReserva, OffsetDateTime fechaCancelacion, EstadoReservaClase estado) {
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

    // Antes era String; ahora es EstadoReservaClase (enum) para que solo acepte
    // CONFIRMADA, CANCELADA o COMPLETADA — los únicos valores válidos según ck_reserva_clase_estado.
    public EstadoReservaClase getEstado() {
        return estado;
    }

    public void setEstado(EstadoReservaClase estado) {
        this.estado = estado;
    }
}
