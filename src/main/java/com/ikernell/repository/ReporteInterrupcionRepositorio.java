package com.ikernell.repository;

import com.ikernell.model.ReporteInterrupciones;
import com.ikernell.model.ReporteInterrupcionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteInterrupcionRepositorio extends JpaRepository<ReporteInterrupciones, ReporteInterrupcionId> {
}
