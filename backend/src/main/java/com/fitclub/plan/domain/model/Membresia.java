package com.fitclub.plan.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class Membresia {
    private Long membresiaId;
    private Long socioId;
    private Long planId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private OffsetDateTime createdAt;

    public Membresia() {
    }

    public Membresia(Long membresiaId, Long socioId, Long planId, LocalDate fechaInicio,
                     LocalDate fechaFin, String estado, OffsetDateTime createdAt) {
        this.membresiaId = membresiaId;
        this.socioId = socioId;
        this.planId = planId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.createdAt = createdAt;
    }

    public Long getMembresiaId() {
        return membresiaId;
    }

    public void setMembresiaId(Long membresiaId) {
        this.membresiaId = membresiaId;
    }

    public Long getSocioId() {
        return socioId;
    }

    public void setSocioId(Long socioId) {
        this.socioId = socioId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}