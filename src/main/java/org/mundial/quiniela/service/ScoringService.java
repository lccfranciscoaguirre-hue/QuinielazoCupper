package org.mundial.quiniela.service;

import org.mundial.quiniela.model.Participante;
import org.mundial.quiniela.model.Partido;
import org.mundial.quiniela.model.Prediccion;
import org.mundial.quiniela.repository.ParticipanteRepository;
import org.mundial.quiniela.repository.PartidoRepository;
import org.mundial.quiniela.repository.PrediccionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScoringService {
    private static final Logger logger = LoggerFactory.getLogger(ScoringService.class);

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private PrediccionRepository prediccionRepository;

    @Transactional
    public void actualizarResultadosYCalcularPuntos(Map<String, String> resultadosOficiales) {
        logger.info("Recibidos resultados para actualizar: {}", resultadosOficiales);

        List<Partido> partidos = partidoRepository.findAll();
        boolean cambiosRealizados = false;

        for (Partido partido : partidos) {
            // Usamos el ID real de la base de datos en lugar de un índice arbitrario
            String homeKey = "final_" + partido.getId() + "_home";
            String awayKey = "final_" + partido.getId() + "_away";

            if (resultadosOficiales.containsKey(homeKey) && resultadosOficiales.containsKey(awayKey)) {
                String homeVal = resultadosOficiales.get(homeKey);
                String awayVal = resultadosOficiales.get(awayKey);

                // Si los campos NO están vacíos, actualizamos el marcador y lo marcamos como jugado
                if (homeVal != null && !homeVal.trim().isEmpty() && awayVal != null && !awayVal.trim().isEmpty()) {
                    partido.setGolesLocal(Integer.parseInt(homeVal));
                    partido.setGolesVisitante(Integer.parseInt(awayVal));
                    partido.setJugado(true);
                    partidoRepository.save(partido);
                    cambiosRealizados = true;
                    logger.info("Actualizado resultado del partido ID {}: {} vs {} ({} - {})", partido.getId(), partido.getEquipoLocal(), partido.getEquipoVisitante(), homeVal, awayVal);
                } 
                // Si los campos ESTÁN vacíos, pero el partido ya estaba marcado como jugado, significa que el admin quiere resetear este partido en particular
                else if (partido.getJugado()) {
                    partido.setGolesLocal(null);
                    partido.setGolesVisitante(null);
                    partido.setJugado(false);
                    partidoRepository.save(partido);
                    cambiosRealizados = true;
                    logger.info("Reseteado el resultado del partido ID {}: {} vs {}", partido.getId(), partido.getEquipoLocal(), partido.getEquipoVisitante());
                }
            }
        }

        if (cambiosRealizados) {
            logger.info("Se actualizaron los resultados de los partidos. Recalculando todos los puntos...");
            List<Participante> todosLosParticipantes = participanteRepository.findAll();
            for (Participante p : todosLosParticipantes) {
                calcularPuntosParaParticipante(p);
            }
            logger.info("Puntos recalculados para todos los participantes.");
        }
    }

    @Transactional
    public void calcularPuntosParaParticipante(Participante participante) {
        logger.info("Calculando puntos para el participante: {}", participante.getNickname());

        List<Prediccion> predicciones = prediccionRepository.findByParticipanteId(participante.getId());
        List<Partido> partidosJugados = partidoRepository.findByJugado(true);

        Map<Long, Partido> mapaPartidosJugados = partidosJugados.stream()
                .collect(Collectors.toMap(Partido::getId, Function.identity()));

        int totalPuntos = 0;
        int totalAciertosPartidos = 0;
        int totalAciertosGanador = 0;

        for (Prediccion prediccion : predicciones) {
            Partido partidoOficial = mapaPartidosJugados.get(prediccion.getPartido().getId());

            if (partidoOficial != null) { // Solo puntuar si el partido ya se jugó
                boolean aciertoExacto = prediccion.getGolesLocal().equals(partidoOficial.getGolesLocal()) &&
                                        prediccion.getGolesVisitante().equals(partidoOficial.getGolesVisitante());

                boolean aciertoGanador = Integer.compare(prediccion.getGolesLocal(), prediccion.getGolesVisitante()) ==
                                         Integer.compare(partidoOficial.getGolesLocal(), partidoOficial.getGolesVisitante());

                if (aciertoExacto) {
                    totalPuntos += 2; // Cambiado a 2 puntos por marcador exacto
                    totalAciertosPartidos++;
                } else if (aciertoGanador) {
                    totalPuntos += 1; // Cambiado a 1 punto por acierto de ganador/empate
                    totalAciertosGanador++;
                }
            }
        }

        participante.setPuntos(totalPuntos);
        participante.setAciertosPartidos(totalAciertosPartidos);
        participante.setAciertosGanador(totalAciertosGanador);
        
        // Guardar el participante actualizado en la base de datos
        participanteRepository.save(participante);
        
        logger.info("Cálculo finalizado para {}: Puntos={}, Aciertos Exactos={}, Aciertos Ganador={}", 
            participante.getNickname(), totalPuntos, totalAciertosPartidos, totalAciertosGanador);
    }
}