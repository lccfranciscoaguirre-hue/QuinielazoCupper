package org.mundial.quiniela.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mundial.quiniela.model.Participante;
import org.mundial.quiniela.model.Partido;
import org.mundial.quiniela.model.Prediccion;
import org.mundial.quiniela.repository.ParticipanteRepository;
import org.mundial.quiniela.repository.PartidoRepository;
import org.mundial.quiniela.repository.PrediccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private PrediccionRepository prediccionRepository;

    public byte[] generarReporteExcel() throws IOException {
        List<Participante> participantes = participanteRepository.findAll();
        List<Partido> partidos = partidoRepository.findAll();
        List<Prediccion> predicciones = prediccionRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pronósticos");

            // Estilo para la cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Fila de cabecera
            Row headerRow = sheet.createRow(0);
            Cell cell0 = headerRow.createCell(0);
            cell0.setCellValue("Participante");
            cell0.setCellStyle(headerStyle);

            for (int i = 0; i < partidos.size(); i++) {
                Partido p = partidos.get(i);
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(p.getEquipoLocal() + " vs " + p.getEquipoVisitante());
                cell.setCellStyle(headerStyle);
            }

            // Datos de los participantes
            int rowIdx = 1;
            for (Participante participante : participantes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(participante.getNickname());

                for (int i = 0; i < partidos.size(); i++) {
                    Partido partido = partidos.get(i);
                    Prediccion pred = buscarPrediccion(predicciones, participante.getId(), partido.getId());
                    
                    Cell cell = row.createCell(i + 1);
                    if (pred != null) {
                        cell.setCellValue(pred.getGolesLocal() + "-" + pred.getGolesVisitante());
                    } else {
                        cell.setCellValue("-");
                    }
                    
                    CellStyle centerStyle = workbook.createCellStyle();
                    centerStyle.setAlignment(HorizontalAlignment.CENTER);
                    cell.setCellStyle(centerStyle);
                }
            }

            // Autoajustar columnas
            for (int i = 0; i <= partidos.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private Prediccion buscarPrediccion(List<Prediccion> predicciones, Long participanteId, Long partidoId) {
        for (Prediccion p : predicciones) {
            if (p.getParticipante().getId().equals(participanteId) && p.getPartido().getId().equals(partidoId)) {
                return p;
            }
        }
        return null;
    }
}
