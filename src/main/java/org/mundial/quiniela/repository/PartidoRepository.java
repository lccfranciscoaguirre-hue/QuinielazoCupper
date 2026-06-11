package org.mundial.quiniela.repository;

import org.mundial.quiniela.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByGrupo(String grupo);
    List<Partido> findByJugado(Boolean jugado);
}
