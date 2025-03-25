package com.ikernell.model;

import jakarta.persistence.*;

@Entity
@Table(name = "preguntas_usuarios")
public class PreguntasUsuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombreUsuario; // El nombre ingresado por el usuario

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pregunta;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    // Relación con la tabla usuarios (Opcional, puede ser null)
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = true) // No obligatorio
    private Usuario usuario; // Si el usuario existe, se relaciona

    public PreguntasUsuarios() {}

    public PreguntasUsuarios(String nombreUsuario, String pregunta, String respuesta, Usuario usuario) {
        this.nombreUsuario = nombreUsuario;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.usuario = usuario;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    @Override
    public String toString() {
        return "PreguntasUsuarios{" +
                "id=" + id +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", pregunta='" + pregunta + '\'' +
                ", respuesta='" + respuesta + '\'' +
                ", usuario=" + (usuario != null ? usuario.getId() : "Ninguno") +
                '}';
    }
}
