package com.fitclub.socio.infrastructure.adapter.out.persistence;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.port.out.SocioRepositoryPort;
import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;
import com.fitclub.socio.infrastructure.adapter.out.persistence.mapper.SocioPersistenceMapper;
import com.fitclub.socio.infrastructure.adapter.out.persistence.repository.SpringDataSocioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SocioPersistenceAdapter implements SocioRepositoryPort {

    private final SpringDataSocioRepository springDataSocioRepository;

    public SocioPersistenceAdapter(SpringDataSocioRepository springDataSocioRepository) {
        this.springDataSocioRepository = springDataSocioRepository;
    }

    @Override
    public Socio guardar(Socio socio) {
        SocioJpaEntity entity = SocioPersistenceMapper.toEntity(socio);
        SocioJpaEntity guardado = springDataSocioRepository.save(entity);
        return SocioPersistenceMapper.toDomain(guardado);
    }

    @Override
    public List<Socio> listar() {
        return springDataSocioRepository.findAll().stream()
                .map(SocioPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Socio> buscarPorId(Long id) {
        return springDataSocioRepository.findById(id)
                .map(SocioPersistenceMapper::toDomain);
    }
}