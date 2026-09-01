package com.fitclub.socio.infrastructure.memory;

import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.port.SocioRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Primera implementación del contrato SocioRepository: guarda todo en un
// Map en memoria RAM (se pierde al cerrar el programa). Es un reemplazo
// temporal mientras no conectamos PostgreSQL desde el código todavía.
// LinkedHashMap en vez de HashMap: mantiene el orden en que se van
// insertando los socios, que es más predecible para probar y depurar.
public class SocioRepositoryEnMemoria implements SocioRepository {

    private final Map<Long, Socio> datos = new LinkedHashMap<>();

    @Override
    public Socio guardar(Socio socio) {
        datos.put(socio.getSocioId(), socio);
        return socio;
    }

    @Override
    public Optional<Socio> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Socio> listarTodos() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public boolean existePorCampoUnico(String numeroDocumento) {
        return datos.values().stream()
                .anyMatch(s -> s.getNumeroDocumento().equalsIgnoreCase(numeroDocumento));
    }
}
