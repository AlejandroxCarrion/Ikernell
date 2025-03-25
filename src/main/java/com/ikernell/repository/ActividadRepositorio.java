package com.ikernell.repository;

import com.ikernell.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActividadRepositorio extends JpaRepository<Actividad, Integer> {

    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.desarrollador.id = :desarrolladorId")
    int contarActividadesPorDesarrollador(@Param("desarrolladorId") Integer desarrolladorId);

    @Query("SELECT a FROM Actividad a JOIN a.etapa e JOIN e.proyecto p JOIN a.desarrollador d WHERE p.id = :proyectoId AND d.email = :email")
    List<Actividad> findActivitiesByProjectAndDeveloper(@Param("proyectoId") Integer proyectoId, @Param("email") String email);


    @Modifying
    @Transactional
    @Query("DELETE FROM Actividad a WHERE a.etapa.id = :etapaId")
    void deleteByEtapaId(@Param("etapaId") Integer etapaId);

    List<Actividad> findByEtapaProyectoId(Integer proyectoId);
    long countByDesarrolladorId(Integer desarrolladorId);
    List<Actividad> findByDesarrolladorId(Integer desarrolladorId);
    List<Actividad> findByEtapaId(Integer etapaId);


}
