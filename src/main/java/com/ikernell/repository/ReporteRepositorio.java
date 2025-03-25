package com.ikernell.repository;

import com.ikernell.model.Reportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReporteRepositorio extends JpaRepository<Reportes, Integer> {
    List<Reportes> findByProyectoId(Integer proyectoId);
}
