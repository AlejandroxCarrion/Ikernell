package com.ikernell.repository;

import com.ikernell.model.Interrupciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterrupcionRepositorio extends JpaRepository<Interrupciones, Integer> {
    List<Interrupciones> findByProyectoId(Integer proyectoId);
    List<Interrupciones> findByDesarrolladorId(Integer desarrolladorId);

}
