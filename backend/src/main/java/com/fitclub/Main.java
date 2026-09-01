package com.fitclub;

import com.fitclub.clase.domain.model.*;
import com.fitclub.plan.application.command.RegistrarMembresiaCommand;
import com.fitclub.plan.domain.model.EstadoMembresia;
import com.fitclub.plan.domain.model.Membresia;
import com.fitclub.plan.domain.model.Plan;
import com.fitclub.socio.application.SocioService;
import com.fitclub.socio.domain.exception.DocumentoDuplicadoException;
import com.fitclub.socio.domain.exception.SocioNoEncontradoException;
import com.fitclub.socio.domain.model.CanalNotificacion;
import com.fitclub.socio.domain.model.Notificacion;
import com.fitclub.socio.domain.model.PrioridadNotificacion;
import com.fitclub.socio.domain.model.Socio;
import com.fitclub.socio.domain.model.TipoNotificacion;
import com.fitclub.socio.domain.port.SocioRepository;
import com.fitclub.socio.infrastructure.memory.SocioRepositoryEnMemoria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== FITCLUB SISTEMA DE GESTION ===");

        // ===================================================================
        // CAPÍTULO 02 — Socio (entidad padre) a través de su contrato
        // de repositorio, con implementación en memoria y servicio Java puro.
        // ===================================================================
        SocioRepository socioRepository = new SocioRepositoryEnMemoria();
        SocioService socioService = new SocioService(socioRepository);

        // Prueba positiva 1: registrar dos socios válidos con documento distinto.
        Socio socio1 = socioService.registrar(new Socio(
                1L, "12345678", "Carlos Gómez", "carlos@email.com", "70000000",
                LocalDate.of(1995, 5, 20), true, OffsetDateTime.now()
        ));
        socioService.registrar(new Socio(
                2L, "87651234", "Ana Torres", "ana@email.com", "70099999",
                LocalDate.of(1998, 3, 12), true, OffsetDateTime.now()
        ));

        System.out.println("Socios registrados: " + socioService.listar().size());
        System.out.println("Socio obtenido por id (1): " + socioService.obtener(1L).getNombreCompleto());

        // Prueba negativa 1: pedir un id que no existe debe lanzar SocioNoEncontradoException.
        try {
            socioService.obtener(999L);
        } catch (SocioNoEncontradoException ex) {
            System.out.println("ERROR CONTROLADO: " + ex.getMessage());
        }

        // Prueba negativa 2: registrar un numeroDocumento repetido debe lanzar DocumentoDuplicadoException.
        try {
            socioService.registrar(new Socio(
                    3L, "12345678", "Otro Nombre", "otro@email.com", "70011111",
                    LocalDate.of(2000, 1, 1), true, OffsetDateTime.now()
            ));
        } catch (DocumentoDuplicadoException ex) {
            System.out.println("ERROR CONTROLADO: " + ex.getMessage());
        }

        // ===================================================================
        // Resto del dominio (Capítulo 01), usando ahora al socio1 registrado
        // arriba a través del servicio en vez de crearlo suelto con "new".
        // ===================================================================
        Plan plan = new Plan(
                10L, "Plan Anual VIP", "Acceso ilimitado a todas las sedes",
                new BigDecimal("299.99"), 365, true
        );

        // record de Capítulo 02: agrupa solo los datos que pide la operación
        // "registrar una membresía" (no fechaFin/estado/createdAt, que se calculan aparte).
        RegistrarMembresiaCommand comandoMembresia = new RegistrarMembresiaCommand(
                socio1.getSocioId(), plan.getPlanId(), LocalDate.now()
        );
        Membresia membresia = new Membresia(
                100L, comandoMembresia.socioId(), comandoMembresia.planId(), comandoMembresia.fechaInicio(),
                comandoMembresia.fechaInicio().plusDays(plan.getDuracionDias()), EstadoMembresia.ACTIVA, OffsetDateTime.now()
        );
        socio1.getMembresias().add(membresia);

        Notificacion notificacion = new Notificacion(
                1000L, socio1.getSocioId(), "¡Bienvenido a FitClub!",
                "Tu cuenta fue creada correctamente.", TipoNotificacion.AVISO_GENERAL, PrioridadNotificacion.MEDIA,
                CanalNotificacion.EMAIL, OffsetDateTime.now(), false, null
        );

        Clase clase = new Clase(50L, "Spinning", "Entrenamiento cardiovascular de alta intensidad", 45, Intensidad.ALTA, true);
        Instructor instructor = new Instructor(20L, "87654321", "Carlos Mendoza", "Indoor Cycling", "carlos.mendoza@fitclub.com", "70011122", "MAÑANA", true);
        HorarioClase horario = new HorarioClase(200L, clase.getClaseId(), instructor.getInstructorId(),
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1), 20, "Sala 2", EstadoHorarioClase.PROGRAMADA);
        ReservaClase reserva = new ReservaClase(300L, membresia.getMembresiaId(), horario.getHorarioClaseId(),
                OffsetDateTime.now(), null, EstadoReservaClase.CONFIRMADA);
        Asistencia asistencia = new Asistencia(400L, reserva.getReservaClaseId(), OffsetDateTime.now(), null, EstadoAsistencia.ASISTIO);

        System.out.println("Plan adquirido: " + plan.getNombre());
        System.out.println("Total de membresías del socio: " + socio1.getMembresias().size());
        System.out.println("Estado de la membresía: " + socio1.getMembresias().get(0).getEstado());
        System.out.println("Notificación: [" + notificacion.getTitulo() + "] " + notificacion.getMensaje());
        System.out.println("Clase: " + clase.getNombre() + " (" + clase.getDuracionMinutos() + " min, intensidad " + clase.getIntensidad() + ")");
        System.out.println("Instructor: " + instructor.getNombreCompleto() + " (turno " + instructor.getTurno() + ")");
        System.out.println("Horario: " + horario.getSalon() + " | Estado: " + horario.getEstado());
        System.out.println("Reserva Estado: " + reserva.getEstado() + " | Asistencia: " + asistencia.getEstado());
    }
}