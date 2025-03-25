package com.ikernell.model;

import jakarta.persistence.*;

@Entity
@Table(name = "errores")
public class Errores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;  // 🔹 Nuevo campo agregado

    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoError tipo = TipoError.FUNCIONAL;

    private String fase;

    @ManyToOne
    @JoinColumn(name = "desarrollador_id")
    private Usuario desarrollador;

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    public Errores() {
    }

    public Errores(Integer id, String nombre, String descripcion, TipoError tipo, String fase, Usuario desarrollador, Proyecto proyecto) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = (tipo != null) ? tipo : TipoError.FUNCIONAL;
        this.fase = fase;
        this.desarrollador = desarrollador;
        this.proyecto = proyecto;
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

    public TipoError getTipo() {
        return tipo;
    }

    public void setTipo(TipoError tipo) {
        this.tipo = (tipo != null) ? tipo : TipoError.FUNCIONAL;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
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
        return "Errores{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tipo=" + tipo +
                ", fase='" + fase + '\'' +
                ", desarrollador=" + desarrollador +
                ", proyecto=" + proyecto +
                '}';
    }
}
