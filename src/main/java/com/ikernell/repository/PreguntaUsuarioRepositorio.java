package com.ikernell.repository;

import com.ikernell.model.PreguntasUsuarios;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaUsuarioRepositorio extends JpaRepository<PreguntasUsuarios, Integer> {
}
