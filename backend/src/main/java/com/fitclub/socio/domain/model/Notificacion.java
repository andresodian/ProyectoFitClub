package com.fitclub.socio.domain.model;

import java.time.OffsetDateTime;

public class Notificacion {
    private Long notificacionId;
    private Long socioId;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private PrioridadNotificacion prioridad;
    private CanalNotificacion canal;
    private OffsetDateTime fechaEnvio;
    private Boolean leido;
    private OffsetDateTime fechaLectura;

    public Notificacion() {
    }

    public Notificacion(Long notificacionId, Long socioId, String titulo, String mensaje, TipoNotificacion tipo,
                         PrioridadNotificacion prioridad, CanalNotificacion canal, OffsetDateTime fechaEnvio, Boolean leido,
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

    // Antes era String; ahora es TipoNotificacion (enum) según ck_notificacion_tipo:
    // VENCIMIENTO, RECORDATORIO_CLASE o AVISO_GENERAL.
    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    // Antes era String; ahora es PrioridadNotificacion (enum) según ck_notificacion_prioridad:
    // BAJA, MEDIA o ALTA.
    public PrioridadNotificacion getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadNotificacion prioridad) {
        this.prioridad = prioridad;
    }

    // Antes era String; ahora es CanalNotificacion (enum), nullable igual que antes,
    // según ck_notificacion_canal: null, EMAIL, PUSH, SMS o IN_APP.
    public CanalNotificacion getCanal() {
        return canal;
    }

    public void setCanal(CanalNotificacion canal) {
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

    public OffsetDateTime getFechaLectura() {
        return fechaLectura;
    }

    public void setFechaLectura(OffsetDateTime fechaLectura) {
        this.fechaLectura = fechaLectura;
    }
}
