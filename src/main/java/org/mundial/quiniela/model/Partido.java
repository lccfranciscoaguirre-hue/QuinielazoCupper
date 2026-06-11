package org.mundial.quiniela.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "partidos")
@Data
public class Partido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String equipoLocal;
    private String equipoVisitante;
    private String grupo;
    private Integer golesLocal;
    private Integer golesVisitante;
    private Boolean jugado = false;
}
