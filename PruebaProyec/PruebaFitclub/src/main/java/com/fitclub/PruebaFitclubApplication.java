package com.fitclub;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
        title = "FitClub API",
        version = "1.0",
        description = "Documentación de los endpoints del backend de FitClub (Socio y Membresia)"
))
@SpringBootApplication
public class PruebaFitclubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PruebaFitclubApplication.class, args);
    }
}