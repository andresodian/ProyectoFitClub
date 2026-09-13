package com.fitclub.membresia.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MembresiaResponseDTO {

    private Long id;
    private String tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal precio;
    private Long socioId;

    public MembresiaResponseDTO() {
    }

    public MembresiaResponseDTO(Long id, String tipo, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal precio, Long socioId) {
        this.id = id;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precio = precio;
        this.socioId = socioId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Long getSocioId() {
        return socioId;
    }

    public void setSocioId(Long socioId) {
        this.socioId = socioId;
    }
}