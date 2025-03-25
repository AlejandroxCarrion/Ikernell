package com.ikernell.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "actividades")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoActividad estado = EstadoActividad.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "desarrollador_id", nullable = false)
    private Usuario desarrollador;


    @ManyToOne
    @JoinColumn(name = "etapa_id", nullable = false)
    private Etapa etapa;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFinal;

    public Actividad() {
    }

    public Actividad(String nombre, String descripcion, EstadoActividad estado, Usuario desarrollador, Etapa etapa, LocalDate fechaInicio, LocalDate fechaFinal) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = (estado != null) ? estado : EstadoActividad.PENDIENTE;
        this.desarrollador = desarrollador;
        this.etapa = etapa;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoActividad getEstado() {
        return estado;
    }

    public void setEstado(EstadoActividad estado) {
        this.estado = (estado != null) ? estado : EstadoActividad.PENDIENTE;
    }

    public Usuario getDesarrollador() {
        return desarrollador;
    }

    public void setDesarrollador(Usuario desarrollador) {
        this.desarrollador = desarrollador;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(LocalDate fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    @Override
    public String toString() {
        return "Actividad{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", estado=" + estado +
                ", desarrollador=" + desarrollador +
                ", etapa=" + etapa +
                ", fechaInicio=" + fechaInicio +
                ", fechaFinal=" + fechaFinal +
                '}';
    }
}
