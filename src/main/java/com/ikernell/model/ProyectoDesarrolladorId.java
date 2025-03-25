package com.ikernell.model;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProyectoDesarrolladorId implements Serializable {
    private Integer proyecto;
    private Integer desarrollador;

    public ProyectoDesarrolladorId() {}

    public ProyectoDesarrolladorId(Integer proyecto, Integer desarrollador) {
        this.proyecto = proyecto;
        this.desarrollador = desarrollador;
    }

    public Integer getProyecto() {
        return proyecto;
    }

    public void setProyecto(Integer proyecto) {
        this.proyecto = proyecto;
    }

    public Integer getDesarrollador() {
        return desarrollador;
    }

    public void setDesarrollador(Integer desarrollador) {
        this.desarrollador = desarrollador;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProyectoDesarrolladorId that = (ProyectoDesarrolladorId) o;
        return Objects.equals(proyecto, that.proyecto) && Objects.equals(desarrollador, that.desarrollador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proyecto, desarrollador);
    }
}
