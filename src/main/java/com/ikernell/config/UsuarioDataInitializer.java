package com.ikernell.config;

import com.ikernell.model.Rol;
import com.ikernell.model.Usuario;
import com.ikernell.repository.RolRepositorio;
import com.ikernell.repository.UsuarioRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
@Order(2)
public class UsuarioDataInitializer {

    @Bean
    public CommandLineRunner initUsuarios(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio) {
        return args -> {
            // Verificar si el rol de coordinador existe
            Optional<Rol> rolCoordinador = rolRepositorio.findById(2);
            if (rolCoordinador.isEmpty()) {
                System.out.println("ERROR: Rol con ID 2 no encontrado. No se crearon los usuarios.");
                return;
            }

            // Contraseña común para los nuevos usuarios
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String passwordEncriptada = passwordEncoder.encode("12345678");

            // Lista de usuarios a crear
            List<Usuario> usuariosACrear = Arrays.asList(
                    crearUsuario("Alejandro", "Martínez", "alejandro@gmail.com", "3124567890",
                            "Carrera 45 #78-23", "Front-end", "Ingeniero de Sistemas", 98765432L,
                            "25/06/1995", rolCoordinador.get(), "12345678"),

                    crearUsuario("Samuel", "Rodríguez", "samuel@gmail.com", "3007894563",
                            "Avenida 7 #15-42", "Back-end", "Desarrollador Full Stack", 56789012L,
                            "03/09/1990", rolCoordinador.get(), "12345678"),

                    crearUsuario("Mafe", "González", "mafe@gmail.com", "3154567812",
                            "Calle 80 #23-15", "UX/UI", "Diseñadora", 34567890L,
                            "17/12/1992", rolCoordinador.get(), "12345678")
            );

            // Crear cada usuario si no existe
            for (Usuario usuario : usuariosACrear) {
                Optional<Usuario> usuarioExistente = usuarioRepositorio.findByEmail(usuario.getEmail());

                if (usuarioExistente.isEmpty()) {
                    usuarioRepositorio.save(usuario);
                    System.out.println("Usuario '" + usuario.getNombre() + "' creado correctamente con rol de Coordinador de Proyecto.");
                } else {
                    System.out.println("Usuario '" + usuario.getNombre() + "' ya existe. No se creó nuevamente.");
                }
            }
        };
    }

    // Método auxiliar para crear un objeto Usuario
    private Usuario crearUsuario(String nombre, String apellido, String email, String telefono,
                                 String direccion, String especialidad, String profesion,
                                 Long identificacion, String fechaNacimiento, Rol rol, String contrasena) {
        try {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            usuario.setEspecialidad(especialidad);
            usuario.setProfesion(profesion);
            usuario.setIdentificacion(identificacion);
            usuario.setFechaNacimiento(new SimpleDateFormat("dd/MM/yyyy").parse(fechaNacimiento));
            usuario.setRol(rol);

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            usuario.setContrasena(passwordEncoder.encode(contrasena));

            return usuario;
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage());
        }
    }
}