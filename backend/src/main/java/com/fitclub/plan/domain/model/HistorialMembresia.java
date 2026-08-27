package com.fitclub.plan.domain.model;

import java.time.OffsetDateTime;

public class HistorialMembresia {
    private Long historialId;
    private Long membresiaId;
    private String registradoPor;
    private OffsetDateTime fechaEvento;
    private String tipoEvento;
    private String estadoAnterior;
    private String estadoNuevo;
    private String motivo;

    public HistorialMembresia() {
    }

    public HistorialMembresia(Long historialId, Long membresiaId, String registradoPor,
                              OffsetDateTime fechaEvento, String tipoEvento,
                              String estadoAnterior, String estadoNuevo, String motivo) {
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

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}