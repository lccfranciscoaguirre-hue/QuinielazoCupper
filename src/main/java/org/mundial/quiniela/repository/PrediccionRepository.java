package org.mundial.quiniela.repository;

import org.mundial.quiniela.model.Prediccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrediccionRepository extends JpaRepository<Prediccion, Long> {
    List<Prediccion> findByParticipanteId(Long participanteId);
    void deleteByParticipanteId(Long participanteId);
}
