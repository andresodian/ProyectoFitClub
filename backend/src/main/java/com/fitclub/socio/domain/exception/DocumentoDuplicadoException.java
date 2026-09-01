package com.fitclub.socio.domain.exception;

// Excepción propia: se lanza cuando se intenta registrar un socio con un
// número de documento que ya está en uso por otro socio.
public class DocumentoDuplicadoException extends RuntimeException {
    public DocumentoDuplicadoException(String numeroDocumento) {
        super("Ya existe un socio registrado con el número de documento: " + numeroDocumento);
    }
}
