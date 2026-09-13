package com.fitclub.membresia.domain.port.out;

import com.fitclub.membresia.domain.model.Membresia;

import java.util.List;
import java.util.Optional;

public interface MembresiaRepositoryPort {
    Membresia guardar(Membresia membresia);
    List<Membresia> listar();
    Optional<Membresia> buscarPorId(Long id);
}