package com.fitclub.socio.infrastructure.adapter.in.web.mapper;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioRequestDTO;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioResponseDTO;

import java.time.LocalDate;

public final class SocioWebMapper {

    private SocioWebMapper() {}

    public static Socio toDomain(SocioRequestDTO request) {
        return new Socio(null, request.getNombre(), request.getEmail(),
                request.getTelefono(), LocalDate.now());
    }

    public static SocioResponseDTO toResponseDTO(Socio socio) {
        return new SocioResponseDTO(socio.getId(), socio.getNombre(), socio.getEmail(),
                socio.getTelefono(), socio.getFechaRegistro());
    }
}