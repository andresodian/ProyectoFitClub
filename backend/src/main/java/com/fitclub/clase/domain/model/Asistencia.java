package com.fitclub.clase.domain.model;

import java.time.OffsetDateTime;

public class Asistencia {
    private Long asistenciaId;
    private Long reservaClaseId;
    private OffsetDateTime fechaHoraEntrada;
    private OffsetDateTime fechaHoraSalida;
    private String estado;

    public Asistencia() {
    }

    public Asistencia(Long asistenciaId, Long reservaClaseId, OffsetDateTime fechaHoraEntrada,
                       OffsetDateTime fechaHoraSalida, String estado) {
        this.asistenciaId = asistenciaId;
        this.reservaClaseId = reservaClaseId;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.estado = estado;
    }

    public Long getAsistenciaId() {
        return asistenciaId;
    }

    public void setAsistenciaId(Long asistenciaId) {
        this.asistenciaId = asistenciaId;
    }

    // Antes se llamaba "reservaId". La FK real de la tabla `asistencia` es
    // reserva_clase_id -> reserva_clase(reserva_clase_id), así que se renombró
    // para que quede inequívoco a qué apunta y para que JPA lo mapee solo más adelante.
    public Long getReservaClaseId() {
        return reservaClaseId;
    }

    public void setReservaClaseId(Long reservaClaseId) {
        this.reservaClaseId = reservaClaseId;
    }

    // Antes existía un único "fechaHoraCheckIn". La tabla tiene dos columnas
    // separadas (fecha_hora_entrada NOT NULL y fecha_hora_salida nullable) porque
    // el check-in y el check-out son eventos distintos.
    public OffsetDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(OffsetDateTime fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public OffsetDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(OffsetDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    // Antes era un booleano "asistio" (solo sí/no). La columna real es "estado"
    // con tres valores posibles según ck_asistencia_estado: "ASISTIO", "AUSENTE"
    // o "JUSTIFICADO". Un booleano no puede representar "JUSTIFICADO", por eso
    // se cambió a String en vez de agregar un segundo campo.
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
