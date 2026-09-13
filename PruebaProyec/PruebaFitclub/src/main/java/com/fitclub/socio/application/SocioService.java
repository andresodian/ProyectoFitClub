package com.fitclub.socio.application;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.port.in.SocioUseCase;
import com.fitclub.socio.domain.port.out.SocioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SocioService implements SocioUseCase {

    private final SocioRepositoryPort socioRepositoryPort;

    public SocioService(SocioRepositoryPort socioRepositoryPort) {
        this.socioRepositoryPort = socioRepositoryPort;
    }

    @Override
    @Transactional
    public Socio registrar(Socio socio) {
        return socioRepositoryPort.guardar(socio);
    }

    @Override
    public List<Socio> listar() {
        return socioRepositoryPort.listar();
    }

    @Override
    public Optional<Socio> buscarPorId(Long id) {
        return socioRepositoryPort.buscarPorId(id);
    }
}