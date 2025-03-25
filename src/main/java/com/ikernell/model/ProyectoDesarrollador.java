package com.ikernell.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proyecto_desarrolladores")
public class ProyectoDesarrollador {

    @EmbeddedId
    private ProyectoDesarrolladorId id;

    @ManyToOne
    @MapsId("proyecto")
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @ManyToOne
    @MapsId("desarrollador")
    @JoinColumn(name = "desarrollador_id")
    private Usuario desarrollador;

    public ProyectoDesarrollador() {}

    public ProyectoDesarrollador(Proyecto proyecto, Usuario desarrollador) {
        this.id = new ProyectoDesarrolladorId(proyecto.getId(), desarrollador.getId());
        this.proyecto = proyecto;
        this.desarrollador = desarrollador;
    }

    public ProyectoDesarrolladorId getId() {
        return id;
    }

    public void setId(ProyectoDesarrolladorId id) {
        this.id = id;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public Usuario getDesarrollador() {
        return desarrollador;
    }

    public void setDesarrollador(Usuario desarrollador) {
        this.desarrollador = desarrollador;
    }

    @Override
    public String toString() {
        return "ProyectoDesarrollador{" +
                "proyecto=" + proyecto +
                ", desarrollador=" + desarrollador +
                '}';
    }
}
