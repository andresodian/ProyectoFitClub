package com.fitclub.membresia.domain.exception;

public class MembresiaNoEncontradaException extends RuntimeException {
    public MembresiaNoEncontradaException(Long id) {
        super("No existe una membresía con id " + id);
    }
}