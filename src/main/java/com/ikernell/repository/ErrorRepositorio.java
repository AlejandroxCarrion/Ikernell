package com.ikernell.repository;

import com.ikernell.model.Errores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorRepositorio extends JpaRepository<Errores, Integer> {
    
    List<Errores> findByDesarrolladorId(Integer desarrolladorId);
    List<Errores> findByProyectoId(Integer proyectoId);
    List<Errores> findByProyectoIdAndDesarrolladorId(Integer proyectoId, Integer desarrolladorId);
}
