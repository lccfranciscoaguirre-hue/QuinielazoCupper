package org.mundial.quiniela.controller;

import jakarta.transaction.Transactional;
import org.mundial.quiniela.model.Partido;
import org.mundial.quiniela.model.Participante;
import org.mundial.quiniela.model.Prediccion;
import org.mundial.quiniela.repository.PartidoRepository;
import org.mundial.quiniela.repository.ParticipanteRepository;
import org.mundial.quiniela.repository.PrediccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/predicciones")
public class PrediccionController {

    @Autowired
    private PrediccionRepository prediccionRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @GetMapping
    public List<Prediccion> getAll() {
        return prediccionRepository.findAll();
    }

    @PostMapping("/registro-completo")
    @Transactional
    public Map<String, Object> guardarRegistroCompleto(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Extraer y crear el usuario
            Map<String, String> userData = (Map<String, String>) payload.get("usuario");
            if (userData == null) {
                return Map.of("success", false, "message", "Faltan los datos del usuario.");
            }

            // Normalizar el nickname (quitar espacios en blanco al inicio/fin e ignorar mayúsculas/minúsculas para la validación)
            String nickname = userData.get("nickname").trim();
            
            // Validar que el nickname no esté ya en uso (ignorando mayúsculas)
            List<Participante> participantesExistentes = participanteRepository.findAll();
            boolean nicknameEnUso = participantesExistentes.stream()
                    .anyMatch(p -> p.getNickname().equalsIgnoreCase(nickname));

            if (nicknameEnUso) {
                return Map.of("success", false, "message", "El nickname '" + nickname + "' ya está en uso. Por favor, elige otro.");
            }

            Participante participante = new Participante();
            participante.setNickname(nickname); // Guardamos el nickname sin espacios extra
            participante.setNombre(userData.get("nombre"));
            participante.setApellido(userData.get("apellido"));
            participante.setPais(userData.get("pais"));
            
            // Guardar en BD para generar el ID
            participante = participanteRepository.save(participante);

            // 2. Extraer las predicciones
            List<Map<String, Object>> prediccionesData = (List<Map<String, Object>>) payload.get("predicciones");
            if (prediccionesData == null || prediccionesData.isEmpty()) {
                return Map.of("success", false, "message", "Usuario creado pero no se enviaron predicciones.");
            }

            List<Partido> partidos = partidoRepository.findAll();

            // 3. Guardar cada predicción asociada al nuevo participante
            for (Map<String, Object> data : prediccionesData) {
                String homeTeam = (String) data.get("homeTeam");
                String awayTeam = (String) data.get("awayTeam");
                
                Partido partido = partidos.stream()
                    .filter(p -> p.getEquipoLocal().equals(homeTeam) && p.getEquipoVisitante().equals(awayTeam))
                    .findFirst()
                    .orElse(null);

                if (partido != null) {
                    Prediccion p = new Prediccion();
                    p.setPartido(partido);
                    p.setParticipante(participante);
                    p.setGolesLocal(Integer.parseInt(data.get("homeScore").toString()));
                    p.setGolesVisitante(Integer.parseInt(data.get("awayScore").toString()));
                    prediccionRepository.save(p);
                }
            }
            
            return Map.of("success", true, "message", "Registro y predicciones guardados exitosamente.");

        } catch (DataIntegrityViolationException e) {
            // Este error saltará si dos personas intentan registrar el mismo nickname al mismo tiempo exacto
            return Map.of("success", false, "message", "El nickname ya está en uso en la base de datos. Por favor, elige otro.");
        } catch (Exception e) {
            // Loguear el error real en el servidor para depuración
            e.printStackTrace();
            return Map.of("success", false, "message", "Error al procesar el registro: " + e.getMessage());
        }
    }
}