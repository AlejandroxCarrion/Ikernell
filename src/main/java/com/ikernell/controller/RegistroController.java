package com.ikernell.controller;

import com.ikernell.model.Rol;
import com.ikernell.model.Usuario;
import com.ikernell.repository.RolRepositorio;
import com.ikernell.repository.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Controller
public class RegistroController {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;

    public RegistroController(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "registro"; 
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String fechaNacimiento,
            @RequestParam Long identificacion,
            @RequestParam String direccion,
            @RequestParam String profesion,
            @RequestParam String especialidad,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String contrasena,
            @RequestParam("fotoPerfil") MultipartFile fotoPerfil,
            Model model) {

        // Buscar el rol "Coordinador_proyecto" en la base de datos
        Optional<Rol> rolCoordinador = rolRepositorio.findById(2);

        if (rolCoordinador.isPresent()) {
            try {
                // Conversión de fecha
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date fechaNacimientoDate = dateFormat.parse(fechaNacimiento);

                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setNombre(nombre);
                nuevoUsuario.setApellido(apellido);
                nuevoUsuario.setFechaNacimiento(fechaNacimientoDate);
                nuevoUsuario.setIdentificacion(identificacion);
                nuevoUsuario.setDireccion(direccion);
                nuevoUsuario.setProfesion(profesion);
                nuevoUsuario.setEspecialidad(especialidad);
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setTelefono(telefono);
                nuevoUsuario.setContrasena(passwordEncoder.encode(contrasena));
                nuevoUsuario.setRol(rolCoordinador.get());

                // Guardar la imagen en la base de datos
                if (!fotoPerfil.isEmpty()) {
                    nuevoUsuario.setFotoPerfil(fotoPerfil.getBytes());
                }

                usuarioRepositorio.save(nuevoUsuario);
                return "redirect:/login?registrado";

            } catch (ParseException e) {
                model.addAttribute("error", "⚠️ Error en el formato de fecha. Use YYYY-MM-DD.");
                return "registro";
            } catch (IOException e) {
                model.addAttribute("error", "⚠️ Error al subir la foto de perfil.");
                return "registro";
            }
        } else {
            model.addAttribute("error", "⚠️ No se encontró el rol de Coordinador_proyecto.");
            return "registro";
        }
    }
}
