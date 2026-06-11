package org.mundial.quiniela.service;

import org.mundial.quiniela.model.Partido;
import org.mundial.quiniela.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PartidoRepository partidoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (partidoRepository.count() == 0) {
            System.out.println("Base de datos de partidos vacía. Inicializando datos...");
            crearPartidos();
        } else {
            System.out.println("La base de datos de partidos ya contiene datos. No se requiere inicialización.");
        }
    }

    private void crearPartidos() {
        Map<String, List<String>> groups = Map.ofEntries(
            Map.entry("Group A", List.of("Mexico", "South Africa", "South Korea", "Czechia")),
            Map.entry("Group B", List.of("Canada", "Bosnia & Herzegovina", "Qatar", "Switzerland")),
            Map.entry("Group C", List.of("Brazil", "Morocco", "Haiti", "Scotland")),
            Map.entry("Group D", List.of("United States", "Paraguay", "Australia", "Türkiye")),
            Map.entry("Group E", List.of("Germany", "Curaçao", "Ivory Coast", "Ecuador")),
            Map.entry("Group F", List.of("Netherlands", "Japan", "Sweden", "Tunisia")),
            Map.entry("Group G", List.of("Belgium", "Egypt", "Iran", "New Zealand")),
            Map.entry("Group H", List.of("Spain", "Cape Verde", "Saudi Arabia", "Uruguay")),
            Map.entry("Group I", List.of("France", "Senegal", "Iraq", "Norway")),
            Map.entry("Group J", List.of("Argentina", "Algeria", "Austria", "Jordan")),
            Map.entry("Group K", List.of("Portugal", "DR Congo", "Uzbekistan", "Colombia")),
            Map.entry("Group L", List.of("England", "Croatia", "Ghana", "Panama"))
        );

        List<Partido> partidos = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<String> teams = entry.getValue();
            partidos.addAll(getMatchesForGroup(groupName, teams));
        }

        partidoRepository.saveAll(partidos);
        System.out.println(partidos.size() + " partidos han sido creados y guardados en la base de datos.");
    }

    private List<Partido> getMatchesForGroup(String groupName, List<String> teams) {
        List<Partido> groupMatches = new ArrayList<>();
        int[][] pairings = {{0, 1}, {2, 3}, {0, 2}, {1, 3}, {3, 0}, {1, 2}};

        for (int[] pairing : pairings) {
            Partido partido = new Partido();
            partido.setGrupo(groupName);
            partido.setEquipoLocal(teams.get(pairing[0]));
            partido.setEquipoVisitante(teams.get(pairing[1]));
            partido.setGolesLocal(null); // Cambiado a null
            partido.setGolesVisitante(null); // Cambiado a null
            partido.setJugado(false);
            groupMatches.add(partido);
        }
        return groupMatches;
    }
}