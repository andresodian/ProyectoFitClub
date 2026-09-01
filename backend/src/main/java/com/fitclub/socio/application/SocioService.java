package com.fitclub.socio.application;

import com.fitclub.socio.domain.exception.DocumentoDuplicadoException;
import com.fitclub.socio.domain.exception.SocioNoEncontradoException;
import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.port.SocioRepository;

import java.util.List;

// Servicio Java puro (sin Spring todavía): contiene las reglas de negocio
// alrededor de Socio. OJO: depende de la INTERFAZ SocioRepository, nunca de
// SocioRepositoryEnMemoria directamente. Así, el día que cambiemos la
// implementación en memoria por una real con PostgreSQL, este servicio no
// se toca para nada — solo cambia qué implementación se le pasa al crearlo.
public class SocioService {

    private final SocioRepository repository;

    public SocioService(SocioRepository repository) {
        this.repository = repository;
    }

    public Socio registrar(Socio socio) {
        if (repository.existePorCampoUnico(socio.getNumeroDocumento())) {
            throw new DocumentoDuplicadoException(socio.getNumeroDocumento());
        }
        return repository.guardar(socio);
    }

    public Socio obtener(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id));
    }

    public List<Socio> listar() {
        return repository.listarTodos();
    }
}
