package org.mundial.quiniela.controller;

import org.mundial.quiniela.repository.ParticipanteRepository;
import org.mundial.quiniela.repository.PartidoRepository;
import org.mundial.quiniela.repository.PrediccionRepository;
import org.mundial.quiniela.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PrediccionRepository prediccionRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ExcelService excelService;

    @Value("${admin.delete.password}")
    private String deletePassword;

    @PostMapping("/borrar-bd")
    public Map<String, Object> borrarBaseDeDatos(@RequestBody Map<String, String> payload) {
        String password = payload.get("password");
        
        if (deletePassword.equals(password)) {
            try {
                prediccionRepository.deleteAll();
                participanteRepository.deleteAll();
                
                return Map.of("success", true, "message", "Base de datos borrada exitosamente.");
            } catch (Exception e) {
                return Map.of("success", false, "message", "Error al borrar la base de datos: " + e.getMessage());
            }
        } else {
            return Map.of("success", false, "message", "Contraseña incorrecta.");
        }
    }

    @Transactional
    @DeleteMapping("/eliminar-participante/{id}")
    public Map<String, Object> eliminarParticipante(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String password = payload.get("password");
        
        if (deletePassword.equals(password)) {
            if (participanteRepository.existsById(id)) {
                try {
                    // Borrar predicciones asociadas primero
                    prediccionRepository.deleteByParticipanteId(id);
                    // Borrar al participante
                    participanteRepository.deleteById(id);
                    return Map.of("success", true, "message", "Participante eliminado exitosamente.");
                } catch (Exception e) {
                    return Map.of("success", false, "message", "Error al eliminar al participante: " + e.getMessage());
                }
            } else {
                return Map.of("success", false, "message", "Participante no encontrado.");
            }
        } else {
            return Map.of("success", false, "message", "Contraseña incorrecta.");
        }
    }

    @GetMapping("/descargar-reporte")
    public ResponseEntity<byte[]> descargarReporteExcel() {
        try {
            byte[] excelContent = excelService.generarReporteExcel();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "Reporte_Quinielazo.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelContent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}