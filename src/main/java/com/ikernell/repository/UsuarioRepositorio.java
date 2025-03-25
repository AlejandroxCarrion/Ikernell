package com.ikernell.repository;

import com.ikernell.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombre(String nombre);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRolId(Integer rolId);
    long countByRolId(Integer rolId);
    Optional<Usuario> findByIdentificacion(Long identificacion);
    List<Usuario> findByProyectoAsignadoId(Integer proyectoId);
    Page<Usuario> findByRolId(Integer rolId, Pageable pageable);

}
