package org.mundial.quiniela.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "participantes")
@Data
public class Participante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nickname;
    
    private String nombre;
    private String apellido;
    private String pais;
    private String color;
    private Integer puntos = 0;
    private Integer aciertosPartidos = 0;
    private Integer aciertosGanador = 0;
}