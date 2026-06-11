package org.mundial.quiniela.controller;

import org.mundial.quiniela.model.Partido;
import org.mundial.quiniela.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partidos")
@CrossOrigin(origins = "*") // Para desarrollo
public class PartidoController {

    @Autowired
    private PartidoRepository repository;

    @Autowired
    private org.mundial.quiniela.service.ScoringService scoringService;

    @GetMapping
    public List<Partido> getAll() {
        return repository.findAll();
    }

    @PostMapping("/actualizar-resultados")
    public Map<String, Object> updateResults(@RequestBody Map<String, String> resultados) {
        try {
            scoringService.actualizarResultadosYCalcularPuntos(resultados);
            return Map.of("success", true, "message", "Resultados actualizados y puntos calculados.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/grupo/{grupo}")
    public List<Partido> getByGrupo(@PathVariable String grupo) {
        return repository.findByGrupo(grupo);
    }
}
