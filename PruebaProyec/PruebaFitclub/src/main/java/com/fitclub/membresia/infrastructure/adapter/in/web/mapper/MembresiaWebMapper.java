package com.fitclub.membresia.infrastructure.adapter.in.web.mapper;

import com.fitclub.membresia.domain.model.Membresia;
import com.fitclub.membresia.infrastructure.adapter.in.web.dto.MembresiaRequestDTO;
import com.fitclub.membresia.infrastructure.adapter.in.web.dto.MembresiaResponseDTO;

public final class MembresiaWebMapper {

    private MembresiaWebMapper() {}

    public static Membresia toDomain(MembresiaRequestDTO request) {
        return new Membresia(null, request.getTipo(), request.getFechaInicio(),
                request.getFechaFin(), request.getPrecio(), request.getSocioId());
    }

    public static MembresiaResponseDTO toResponseDTO(Membresia membresia) {
        return new MembresiaResponseDTO(membresia.getId(), membresia.getTipo(),
                membresia.getFechaInicio(), membresia.getFechaFin(), membresia.getPrecio(),
                membresia.getSocioId());
    }
}