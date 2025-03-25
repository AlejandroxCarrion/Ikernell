package com.ikernell.repository;

import com.ikernell.model.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EtapaRepositorio extends JpaRepository<Etapa, Integer> {
    List<Etapa> findByProyectoId(Integer proyectoId);
}
