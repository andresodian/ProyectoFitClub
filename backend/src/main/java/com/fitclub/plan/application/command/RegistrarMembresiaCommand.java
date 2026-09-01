package com.fitclub.plan.application.command;

import java.time.LocalDate;

// record = clase inmutable y compacta: Java genera solo con esta línea el
// constructor, los getters (socioId(), planId(), fechaInicio() — sin "get"),
// equals, hashCode y toString. Perfecto para algo como esto que solo
// transporta datos de una operación puntual, sin comportamiento propio.
//
// Solo lleva lo que la operación "registrar una membresía" necesita recibir
// de afuera. fechaFin, estado y createdAt NO van aquí porque se calculan o
// se asignan dentro del sistema (fechaFin sale de la duración del plan,
// estado siempre arranca en ACTIVA, createdAt lo pone el propio sistema) —
// no son datos que alguien tenga que mandar a mano.
public record RegistrarMembresiaCommand(
        Long socioId,
        Long planId,
        LocalDate fechaInicio
) {
}
