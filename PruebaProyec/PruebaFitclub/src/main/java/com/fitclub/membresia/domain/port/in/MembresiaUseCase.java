package com.fitclub.membresia.domain.port.in;

import com.fitclub.membresia.domain.model.Membresia;

import java.util.List;
import java.util.Optional;

public interface MembresiaUseCase {
    Membresia registrar(Membresia membresia);
    List<Membresia> listar();
    Optional<Membresia> buscarPorId(Long id);
}