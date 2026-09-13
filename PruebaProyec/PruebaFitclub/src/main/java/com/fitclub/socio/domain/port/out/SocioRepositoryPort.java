package com.fitclub.socio.domain.port.out;

import com.fitclub.socio.domain.model.Socio;

import java.util.List;
import java.util.Optional;

public interface SocioRepositoryPort {
    Socio guardar(Socio socio);
    List<Socio> listar();
    Optional<Socio> buscarPorId(Long id);
}