package com.fitclub.socio.domain.port;

import com.fitclub.socio.domain.model.Socio;

import java.util.List;
import java.util.Optional;

// Este es el "contrato" del repositorio: define QUÉ operaciones necesita
// la aplicación sobre Socio, pero no dice CÓMO se guardan los datos (no
// menciona PostgreSQL, SQL ni JPA). Eso se decide afuera, en la implementación.
public interface SocioRepository {

    Socio guardar(Socio socio);

    // Optional en vez de devolver null directo: obliga a quien llama a manejar
    // explícitamente el caso "no lo encontré", en vez de arriesgarse a un
    // NullPointerException si se le olvida comprobar.
    Optional<Socio> buscarPorId(Long id);

    List<Socio> listarTodos();

    // El campo único de Socio es numeroDocumento (su DNI/documento de identidad):
    // en la vida real dos personas no pueden compartir el mismo número de documento,
    // así que este método sirve para revisar eso ANTES de guardar un socio nuevo.
    boolean existePorCampoUnico(String numeroDocumento);
}
