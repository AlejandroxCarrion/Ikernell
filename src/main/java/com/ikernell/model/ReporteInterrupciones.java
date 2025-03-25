package com.ikernell.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reporte_interrupciones")
public class ReporteInterrupciones {

    @EmbeddedId
    private ReporteInterrupcionId id;

    @ManyToOne
    @MapsId("reporte")
    @JoinColumn(name = "reporte_id")
    private Reportes reporte;

    @ManyToOne
    @MapsId("interrupcion")
    @JoinColumn(name = "interrupcion_id")
    private Interrupciones interrupcion;

    public ReporteInterrupciones() {}

    public ReporteInterrupciones(Reportes reporte, Interrupciones interrupcion) {
        this.id = new ReporteInterrupcionId(reporte.getId(), interrupcion.getId());
        this.reporte = reporte;
        this.interrupcion = interrupcion;
    }

    public ReporteInterrupcionId getId() {
        return id;
    }

    public void setId(ReporteInterrupcionId id) {
        this.id = id;
    }

    public Reportes getReporte() {
        return reporte;
    }

    public void setReporte(Reportes reporte) {
        this.reporte = reporte;
    }

    public Interrupciones getInterrupcion() {
        return interrupcion;
    }

    public void setInterrupcion(Interrupciones interrupcion) {
        this.interrupcion = interrupcion;
    }

    @Override
    public String toString() {
        return "ReporteInterrupciones{" +
                "reporte=" + reporte +
                ", interrupcion=" + interrupcion +
                '}';
    }
}
