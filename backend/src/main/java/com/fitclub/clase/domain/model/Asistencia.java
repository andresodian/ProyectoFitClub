package com.fitclub.clase.domain.model;

import java.time.OffsetDateTime;

public class Asistencia {
    private Long asistenciaId;
    private Long reservaClaseId;
    private OffsetDateTime fechaHoraEntrada;
    private OffsetDateTime fechaHoraSalida;
    private EstadoAsistencia estado;

    public Asistencia() {
    }

    public Asistencia(Long asistenciaId, Long reservaClaseId, OffsetDateTime fechaHoraEntrada,
                       OffsetDateTime fechaHoraSalida, EstadoAsistencia estado) {
        this.asistenciaId = asistenciaId;
        this.reservaClaseId = reservaClaseId;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.estado = estado;
    }

    public Long getAsistenciaId() { return asistenciaId; }
    public void setAsistenciaId(Long asistenciaId) { this.asistenciaId = asistenciaId; }
    public Long getReservaClaseId() { return reservaClaseId; }
    public void setReservaClaseId(Long reservaClaseId) { this.reservaClaseId = reservaClaseId; }
    public OffsetDateTime getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(OffsetDateTime fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }
    public OffsetDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(OffsetDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    // Antes era String; ahora es EstadoAsistencia (enum) para que solo acepte
    // ASISTIO, AUSENTE o JUSTIFICADO — los únicos valores válidos según ck_asistencia_estado.
    public EstadoAsistencia getEstado() { return estado; }
    public void setEstado(EstadoAsistencia estado) { this.estado = estado; }
}
