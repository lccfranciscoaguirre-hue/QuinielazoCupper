package org.mundial.quiniela.controller;

import org.mundial.quiniela.model.Participante;
import org.mundial.quiniela.repository.ParticipanteRepository;
import org.mundial.quiniela.service.ScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participantes")
@CrossOrigin(origins = "*") // Para desarrollo
public class ParticipanteController {

    @Autowired
    private ParticipanteRepository repository;

    @Autowired
    private ScoringService scoringService;

    @GetMapping
    public List<Participante> getAll() {
        List<Participante> participantes = repository.findAll();
        // Recalcula los puntos para cada participante antes de devolver la lista
        for (Participante p : participantes) {
            scoringService.calcularPuntosParaParticipante(p);
        }
        return participantes;
    }

    @PostMapping
    public Participante save(@RequestBody Participante participante) {
        // Al guardar un nuevo participante, sus puntos iniciales ya son 0, no se necesita cálculo.
        return repository.save(participante);
    }
}
