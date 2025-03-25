package com.ikernell.repository;

import com.ikernell.model.EstadoProyecto;
import com.ikernell.model.Proyecto;
import com.ikernell.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProyectoRepositorio extends JpaRepository<Proyecto, Integer> {
    List<Proyecto> findByEstado(String estado);
    long countByEstado(EstadoProyecto estado);
    List<Proyecto> findByLider(Usuario lider);
    List<Proyecto> findByLiderId(Integer liderId);

}
