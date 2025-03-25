package com.ikernell.model;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class ReporteInterrupcionId implements Serializable {
    private Integer reporte;
    private Integer interrupcion;

    public ReporteInterrupcionId() {}

    public ReporteInterrupcionId(Integer reporte, Integer interrupcion) {
        this.reporte = reporte;
        this.interrupcion = interrupcion;
    }

    public Integer getReporte() {
        return reporte;
    }

    public void setReporte(Integer reporte) {
        this.reporte = reporte;
    }

    public Integer getInterrupcion() {
        return interrupcion;
    }

    public void setInterrupcion(Integer interrupcion) {
        this.interrupcion = interrupcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReporteInterrupcionId that = (ReporteInterrupcionId) o;
        return Objects.equals(reporte, that.reporte) && Objects.equals(interrupcion, that.interrupcion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reporte, interrupcion);
    }
}
