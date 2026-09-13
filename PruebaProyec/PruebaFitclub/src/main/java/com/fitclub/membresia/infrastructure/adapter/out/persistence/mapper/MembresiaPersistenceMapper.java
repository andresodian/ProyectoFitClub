package com.fitclub.membresia.infrastructure.adapter.out.persistence.mapper;

import com.fitclub.membresia.domain.model.Membresia;
import com.fitclub.membresia.infrastructure.adapter.out.persistence.entity.MembresiaJpaEntity;
import com.fitclub.socio.infrastructure.adapter.out.persistence.entity.SocioJpaEntity;

public final class MembresiaPersistenceMapper {

    private MembresiaPersistenceMapper() {
    }

    public static MembresiaJpaEntity toEntity(Membresia membresia, SocioJpaEntity socio) {
        return new MembresiaJpaEntity(
                membresia.getTipo(),
                membresia.getFechaInicio(),
                membresia.getFechaFin(),
                membresia.getPrecio(),
                socio
        );
    }

    public static Membresia toDomain(MembresiaJpaEntity entity) {
        return new Membresia(
                entity.getId(),
                entity.getTipo(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getPrecio(),
                entity.getSocio().getId()
        );
    }
}