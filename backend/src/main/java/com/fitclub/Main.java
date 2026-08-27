package com.fitclub;

import com.fitclub.clase.domain.model.*;
import com.fitclub.plan.domain.model.Membresia;
import com.fitclub.plan.domain.model.Plan;
import com.fitclub.socio.domain.model.Notificacion;
import com.fitclub.socio.domain.model.Socio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class Main {
    public static void main(String[] args) {
        // 1. Crear un Socio
        Socio socio = new Socio(
                1L,
                "12345678",
                "Carlos Gómez",
                "carlos@email.com",
                "70000000",
                LocalDate.of(1995, 5, 20),
                true,
                OffsetDateTime.now()
        );

        // 2. Crear un Plan
        Plan plan = new Plan(
                10L,
                "Plan Anual VIP",
                "Acceso ilimitado a todas las sedes",
                new BigDecimal("299.99"),
                365,
                true
        );

        // 3. Crear una Membresía asociada
        Membresia membresia = new Membresia(
                100L,
                socio.getSocioId(),
                plan.getPlanId(),
                LocalDate.now(),
                LocalDate.now().plusDays(plan.getDuracionDias()),
                "ACTIVA",
                OffsetDateTime.now()
        );

        // 4. Agregar la membresía a la lista del socio
        socio.getMembresias().add(membresia);

        // 5. Crear una Notificación para el socio
        Notificacion notificacion = new Notificacion(
                1000L,
                socio.getSocioId(),
                "¡Bienvenido a FitClub!",
                "Tu cuenta fue creada correctamente.",
                "AVISO_GENERAL",
                "MEDIA",
                "EMAIL",
                OffsetDateTime.now(),
                false,
                null
        );

        // 6. Crear una Clase, un Instructor y su Horario
        Clase clase = new Clase(
                50L,
                "Spinning",
                "Entrenamiento cardiovascular de alta intensidad",
                45,
                "ALTA",
                true
        );

        Instructor instructor = new Instructor(
                20L,
                "87654321",
                "Carlos Mendoza",
                "Indoor Cycling",
                "carlos.mendoza@fitclub.com",
                "70011122",
                "MAÑANA",
                true
        );

        HorarioClase horario = new HorarioClase(
                200L,
                clase.getClaseId(),
                instructor.getInstructorId(),
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1),
                20,
                "Sala 2",
                "PROGRAMADA"
        );

        // 7. El socio reserva ese horario a través de su membresía
        ReservaClase reserva = new ReservaClase(
                300L,
                membresia.getMembresiaId(),
                horario.getHorarioClaseId(),
                OffsetDateTime.now(),
                null,
                "CONFIRMADA"
        );

        // 8. Se registra su asistencia ("ASISTIO" es el único valor coherente
        //    con una reserva confirmada que efectivamente se presentó; ver ck_asistencia_estado)
        Asistencia asistencia = new Asistencia(
                400L,
                reserva.getReservaClaseId(),
                OffsetDateTime.now(),
                null,
                "ASISTIO"
        );

        // 9. Imprimir resultados en consola
        System.out.println("=== FITCLUB SISTEMA DE GESTION ===");
        System.out.println("Socio registrado: " + socio.getNombreCompleto());
        System.out.println("Plan adquirido: " + plan.getNombre());
        System.out.println("Total de membresías del socio: " + socio.getMembresias().size());
        System.out.println("Estado de la membresía: " + socio.getMembresias().get(0).getEstado());
        System.out.println("Notificación: [" + notificacion.getTitulo() + "] " + notificacion.getMensaje());
        System.out.println("Clase: " + clase.getNombre() + " (" + clase.getDuracionMinutos()
                + " min, intensidad " + clase.getIntensidad() + ")");
        System.out.println("Instructor: " + instructor.getNombreCompleto() + " (turno " + instructor.getTurno() + ")");
        System.out.println("Horario: " + horario.getSalon() + " | Estado: " + horario.getEstado());
        System.out.println("Reserva Estado: " + reserva.getEstado() + " | Asistencia: " + asistencia.getEstado());
    }
}
