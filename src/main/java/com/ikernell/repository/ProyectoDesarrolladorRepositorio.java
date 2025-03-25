package com.ikernell.repository;

import com.ikernell.model.ProyectoDesarrollador;
import com.ikernell.model.ProyectoDesarrolladorId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoDesarrolladorRepositorio extends JpaRepository<ProyectoDesarrollador, ProyectoDesarrolladorId> {
}
