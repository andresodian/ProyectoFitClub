package com.fitclub.socio.domain.model;

import java.time.OffsetDateTime;

public class Notificacion {
    private Long notificacionId;
    private Long socioId;
    private String titulo;
    private String mensaje;
    private String tipo;
    private String prioridad;
    private String canal;
    private OffsetDateTime fechaEnvio;
    private Boolean leido;
    private OffsetDateTime fechaLectura;

    public Notificacion() {
    }

    public Notificacion(Long notificacionId, Long socioId, String titulo, String mensaje, String tipo,
                         String prioridad, String canal, OffsetDateTime fechaEnvio, Boolean leido,
                         OffsetDateTime fechaLectura) {
        this.notificacionId = notificacionId;
        this.socioId = socioId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.canal = canal;
        this.fechaEnvio = fechaEnvio;
        this.leido = leido;
        this.fechaLectura = fechaLectura;
    }

    public Long getNotificacionId() {
        return notificacionId;
    }

    public void setNotificacionId(Long notificacionId) {
        this.notificacionId = notificacionId;
    }

    public Long getSocioId() {
        return socioId;
    }

    public void setSocioId(Long socioId) {
        this.socioId = socioId;
    }

    // Columna NOT NULL en `notificacion`; faltaba por completo en esta clase.
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    // Valores válidos según ck_notificacion_tipo: "VENCIMIENTO", "RECORDATORIO_CLASE" o "AVISO_GENERAL".
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Valores válidos según ck_notificacion_prioridad: "BAJA", "MEDIA" o "ALTA".
    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    // Valores válidos según ck_notificacion_canal: null, "EMAIL", "PUSH", "SMS" o "IN_APP".
    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public OffsetDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(OffsetDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

    // Nullable: solo se llena cuando el socio efectivamente abre/lee la notificación.
    public OffsetDateTime getFechaLectura() {
        return fechaLectura;
    }

    public void setFechaLectura(OffsetDateTime fechaLectura) {
        this.fechaLectura = fechaLectura;
    }
}
