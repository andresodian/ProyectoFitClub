package com.fitclub.socio.domain.exception;

// Excepción propia: se lanza cuando alguien pide un socio por id y ese id
// no existe. Es una RuntimeException porque es un error de negocio (no un
// error de programación), y así no obliga a envolver cada llamada en try/catch
// a menos que realmente quieras manejar ese caso.
public class SocioNoEncontradoException extends RuntimeException {
    public SocioNoEncontradoException(Long id) {
        super("No existe el socio con id: " + id);
    }
}
