package org.mundial.quiniela.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "predicciones")
@Data
public class Prediccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participante_id")
    private Participante participante;

    @ManyToOne
    @JoinColumn(name = "partido_id")
    private Partido partido;

    private Integer golesLocal;
    private Integer golesVisitante;
}
