package com.ikernell.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Base64;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_nacimiento", nullable = false)
    private Date fechaNacimiento;

    @Column(nullable = false, unique = true)
    private Long identificacion;

    private String direccion;
    private String profesion;
    private String especialidad;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fotoPerfil;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;

    @Column(nullable = false)
    private String contrasena;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyectoAsignado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DesempenoUsuario desempeno = DesempenoUsuario.BUENO; 

    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Usuario(Integer id, String nombre, String apellido, Date fechaNacimiento, Long identificacion, String direccion,
                   String profesion, String especialidad, byte[] fotoPerfil, String email, String telefono,
                   String contrasena, Rol rol, Proyecto proyectoAsignado, EstadoUsuario estado, DesempenoUsuario desempeno) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.identificacion = identificacion;
        this.direccion = direccion;
        this.profesion = profesion;
        this.especialidad = especialidad;
        this.fotoPerfil = fotoPerfil;
        this.email = email;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.rol = rol;
        this.fechaRegistro = LocalDateTime.now();
        this.proyectoAsignado = proyectoAsignado;
        this.estado = (estado != null) ? estado : EstadoUsuario.ACTIVO;
        this.desempeno = (desempeno != null) ? desempeno : DesempenoUsuario.BUENO;
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Long getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(Long identificacion) {
        this.identificacion = identificacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getProfesion() {
        return profesion;
    }

    public void setProfesion(String profesion) {
        this.profesion = profesion;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public byte[] getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(byte[] fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getFotoBase64() {
        return (fotoPerfil != null) ? Base64.getEncoder().encodeToString(fotoPerfil) : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Proyecto getProyectoAsignado() {
        return proyectoAsignado;
    }

    public void setProyectoAsignado(Proyecto proyectoAsignado) {
        this.proyectoAsignado = proyectoAsignado;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public DesempenoUsuario getDesempeno() {
        return desempeno;
    }

    public void setDesempeno(DesempenoUsuario desempeno) {
        this.desempeno = desempeno;
    }

    // Getter y setter para el nuevo campo
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", identificacion=" + identificacion +
                ", direccion='" + direccion + '\'' +
                ", profesion='" + profesion + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", rol=" + rol +
                ", fechaRegistro=" + fechaRegistro +
                ", proyectoAsignado=" + proyectoAsignado +
                ", estado=" + estado +
                ", desempeño=" + desempeno +
                '}';
    }
}
