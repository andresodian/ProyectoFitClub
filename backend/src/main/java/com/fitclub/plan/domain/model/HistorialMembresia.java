package com.fitclub.plan.domain.model;

import java.time.OffsetDateTime;

public class HistorialMembresia {
    private Long historialId;
    private Long membresiaId;
    private String registradoPor;
    private OffsetDateTime fechaEvento;
    private TipoEventoMembresia tipoEvento;
    private EstadoMembresia estadoAnterior;
    private EstadoMembresia estadoNuevo;
    private String motivo;

    public HistorialMembresia() {
    }

    public HistorialMembresia(Long historialId, Long membresiaId, String registradoPor,
                              OffsetDateTime fechaEvento, TipoEventoMembresia tipoEvento,
                              EstadoMembresia estadoAnterior, EstadoMembresia estadoNuevo, String motivo) {
        this.historialId = historialId;
        this.membresiaId = membresiaId;
        this.registradoPor = registradoPor;
        this.fechaEvento = fechaEvento;
        this.tipoEvento = tipoEvento;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.motivo = motivo;
    }

    public Long getHistorialId() {
        return historialId;
    }

    public void setHistorialId(Long historialId) {
        this.historialId = historialId;
    }

    public Long getMembresiaId() {
        return membresiaId;
    }

    public void setMembresiaId(Long membresiaId) {
        this.membresiaId = membresiaId;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }

    public OffsetDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(OffsetDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    // Antes era String; ahora es TipoEventoMembresia (enum) según ck_historial_membresia_tipo:
    // RENOVACION, SUSPENSION, CAMBIO_PLAN o CANCELACION.
    public TipoEventoMembresia getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEventoMembresia tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    // No tiene un CHECK propio en la base, pero representa el mismo concepto que
    // membresia.estado (el estado ANTES del evento), así que reutiliza EstadoMembresia
    // en vez de quedar como texto libre.
    public EstadoMembresia getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoMembresia estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    // Mismo caso que estadoAnterior, pero para el estado DESPUÉS del evento.
    public EstadoMembresia getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoMembresia estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
