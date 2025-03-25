package com.ikernell.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "interrupciones")
public class Interrupciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInterrupcion tipo; // ENUM con valores predefinidos

    @Temporal(TemporalType.DATE) // ⚡ Esto guardará solo la fecha, sin hora
    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false)
    private Integer duracionMinutos;

    @Column(nullable = false)
    private String fase;

    @Column(nullable = false)
    private String descripcion; // Campo agregado para la descripción

    @ManyToOne
    @JoinColumn(name = "desarrollador_id", nullable = false)
    private Usuario desarrollador;

    @ManyToOne
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;


    public Interrupciones() {
    }

    public Interrupciones(String nombre, TipoInterrupcion tipo, Date fecha, Integer duracionMinutos, String fase, String descripcion, Usuario desarrollador, Proyecto proyecto) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
        this.fase = fase;
        this.descripcion = descripcion;
        this.desarrollador = desarrollador;
        this.proyecto = proyecto;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoInterrupcion getTipo() {
        return tipo;
    }

    public void setTipo(TipoInterrupcion tipo) {
        this.tipo = tipo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Usuario getDesarrollador() {
        return desarrollador;
    }

    public void setDesarrollador(Usuario desarrollador) {
        this.desarrollador = desarrollador;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    @Override
    public String toString() {
        return "Interrupcion{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", fecha=" + fecha +
                ", duracionMinutos=" + duracionMinutos +
                ", fase='" + fase + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", desarrollador=" + desarrollador +
                ", proyecto=" + proyecto +
                '}';
    }
}
