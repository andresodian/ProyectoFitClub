package com.fitclub.socio.infrastructure.adapter.in.web;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.port.in.SocioUseCase;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioRequestDTO;
import com.fitclub.socio.infrastructure.adapter.in.web.dto.SocioResponseDTO;
import com.fitclub.socio.infrastructure.adapter.in.web.mapper.SocioWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioUseCase socioUseCase;

    public SocioController(SocioUseCase socioUseCase) {
        this.socioUseCase = socioUseCase;
    }

    @PostMapping
    public ResponseEntity<SocioResponseDTO> crear(@Valid @RequestBody SocioRequestDTO request) {
        Socio guardado = socioUseCase.registrar(SocioWebMapper.toDomain(request));
        return ResponseEntity.status(201).body(SocioWebMapper.toResponseDTO(guardado));
    }

    @GetMapping
    public List<SocioResponseDTO> listar() {
        return socioUseCase.listar().stream()
                .map(SocioWebMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocioResponseDTO> buscarPorId(@PathVariable Long id) {
        Socio socio = socioUseCase.buscarPorId(id)
                .orElseThrow(() -> new com.fitclub.socio.domain.exception.SocioNoEncontradoException(id));
        return ResponseEntity.ok(SocioWebMapper.toResponseDTO(socio));
    }
}