package com.fitclub.socio.domain.port.in;

import com.fitclub.socio.domain.model.Socio;

import java.util.List;
import java.util.Optional;

public interface SocioUseCase {
    Socio registrar(Socio socio);
    List<Socio> listar();
    Optional<Socio> buscarPorId(Long id);
}