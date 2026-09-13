package com.fitclub.membresia.infrastructure.adapter.out.persistence;

import com.fitclub.membresia.domain.model.Membresia;
import com.fitclub.membresia.domain.port.out.MembresiaRepositoryPort;
import com.fitclub.membresia.infrastructure.adapter.out.persistence.entity.MembresiaJpaEntity;
import com.fitclub.membresia.infrastructure.adapter.out.persistence.mapper.MembresiaPersistenceMapper;
import com.fitclub.membresia.infrastructure.adapter.out.persistence.repository.SpringDataMembresiaRepository;
import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;
import com.fitclub.socio.infrastructure.adapter.out.persistence.repository.SpringDataSocioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MembresiaPersistenceAdapter implements MembresiaRepositoryPort {

    private final SpringDataMembresiaRepository springDataMembresiaRepository;
    private final SpringDataSocioRepository springDataSocioRepository;

    public MembresiaPersistenceAdapter(SpringDataMembresiaRepository springDataMembresiaRepository,
                                       SpringDataSocioRepository springDataSocioRepository) {
        this.springDataMembresiaRepository = springDataMembresiaRepository;
        this.springDataSocioRepository = springDataSocioRepository;
    }

    @Override
    public Membresia guardar(Membresia membresia) {
        SocioJpaEntity socioRef = springDataSocioRepository.getReferenceById(membresia.getSocioId());
        MembresiaJpaEntity entity = MembresiaPersistenceMapper.toEntity(membresia, socioRef);
        MembresiaJpaEntity guardado = springDataMembresiaRepository.save(entity);
        return MembresiaPersistenceMapper.toDomain(guardado);
    }

    @Override
    public List<Membresia> listar() {
        return springDataMembresiaRepository.findAll().stream()
                .map(MembresiaPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Membresia> buscarPorId(Long id) {
        return springDataMembresiaRepository.findById(id)
                .map(MembresiaPersistenceMapper::toDomain);
    }
}