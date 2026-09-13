package com.fitclub.socio.infrastructure.adapter.out.persistence.mapper;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;

public final class SocioPersistenceMapper {

    private SocioPersistenceMapper() {
    }

    public static SocioJpaEntity toEntity(Socio socio) {
        return new SocioJpaEntity(
                socio.getNombre(),
                socio.getEmail(),
                socio.getTelefono(),
                socio.getFechaRegistro()
        );
    }

    public static Socio toDomain(SocioJpaEntity entity) {
        return new Socio(
                entity.getId(),
                entity.getNombre(),
                entity.getEmail(),
                entity.getTelefono(),
                entity.getFechaRegistro()
        );
    }
}