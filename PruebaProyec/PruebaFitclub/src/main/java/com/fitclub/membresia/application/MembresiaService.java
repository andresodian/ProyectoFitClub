package com.fitclub.membresia.application;

import com.fitclub.membresia.domain.model.Membresia;
import com.fitclub.membresia.domain.port.in.MembresiaUseCase;
import com.fitclub.membresia.domain.port.out.MembresiaRepositoryPort;
import com.fitclub.socio.domain.exception.SocioNoEncontradoException;
import com.fitclub.socio.domain.port.in.SocioUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MembresiaService implements MembresiaUseCase {

    private final MembresiaRepositoryPort membresiaRepositoryPort;
    private final SocioUseCase socioUseCase;

    public MembresiaService(MembresiaRepositoryPort membresiaRepositoryPort, SocioUseCase socioUseCase) {
        this.membresiaRepositoryPort = membresiaRepositoryPort;
        this.socioUseCase = socioUseCase;
    }

    @Override
    @Transactional
    public Membresia registrar(Membresia membresia) {
        socioUseCase.buscarPorId(membresia.getSocioId())
                .orElseThrow(() -> new SocioNoEncontradoException(membresia.getSocioId()));
        return membresiaRepositoryPort.guardar(membresia);
    }

    @Override
    public List<Membresia> listar() {
        return membresiaRepositoryPort.listar();
    }

    @Override
    public Optional<Membresia> buscarPorId(Long id) {
        return membresiaRepositoryPort.buscarPorId(id);
    }
}