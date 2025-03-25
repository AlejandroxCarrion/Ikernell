package com.ikernell.controller;
import java.text.ParseException;
import com.ikernell.model.Actividad;
import com.ikernell.model.Errores;
import com.ikernell.model.EstadoActividad;
import com.ikernell.model.Etapa;
import com.ikernell.model.Interrupciones;
import com.ikernell.model.Proyecto;
import com.ikernell.model.TipoError;
import com.ikernell.model.TipoInterrupcion;
import com.ikernell.model.Usuario;
import com.ikernell.repository.ActividadRepositorio;
import com.ikernell.repository.ErrorRepositorio;
import com.ikernell.repository.InterrupcionRepositorio;
import com.ikernell.repository.ProyectoRepositorio;
import com.ikernell.repository.UsuarioRepositorio;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/desarrollador")
public class DesarrolladorController {

    private final InterrupcionRepositorio interrupcionRepositorio;
    private final ErrorRepositorio errorRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ProyectoRepositorio proyectoRepositorio;
    private final ActividadRepositorio actividadRepositorio;
    private final PasswordEncoder passwordEncoder;

    public DesarrolladorController(UsuarioRepositorio usuarioRepositorio, ActividadRepositorio actividadRepositorio, ErrorRepositorio errorRepositorio, ProyectoRepositorio proyectoRepositorio, InterrupcionRepositorio interrupcionRepositorio, PasswordEncoder passwordEncoder) {
        this.interrupcionRepositorio = interrupcionRepositorio;
        this.errorRepositorio = errorRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.proyectoRepositorio = proyectoRepositorio;
        this.actividadRepositorio = actividadRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/index")
    public String mostrarDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/login?error=Usuario no encontrado";
        }

        Usuario desarrollador = usuarioOpt.get();

        model.addAttribute("nombreUsuario", desarrollador.getNombre());

        // Obtener todas las actividades del desarrollador
        List<Actividad> actividades = actividadRepositorio.findByDesarrolladorId(desarrollador.getId());
        model.addAttribute("actividades", actividades);

        // Calcular estadísticas
        long totalActividades = actividades.size();
        long actividadesPendientes = actividades.stream().filter(a -> a.getEstado() == EstadoActividad.PENDIENTE).count();
        long actividadesEnProceso = actividades.stream().filter(a -> a.getEstado() == EstadoActividad.EN_PROCESO).count();
        long actividadesFinalizadas = actividades.stream().filter(a -> a.getEstado() == EstadoActividad.COMPLETADA).count();

        // Agregar estadísticas al modelo
        model.addAttribute("totalActividades", totalActividades);
        model.addAttribute("actividadesPendientes", actividadesPendientes);
        model.addAttribute("actividadesEnProceso", actividadesEnProceso);
        model.addAttribute("actividadesFinalizadas", actividadesFinalizadas);

        // Para evitar errores si no hay actividades
        if (totalActividades == 0) {
            model.addAttribute("porcentajePendientes", 0);
            model.addAttribute("porcentajeEnProgreso", 0);
            model.addAttribute("porcentajeFinalizadas", 0);
        } else {
            // Calcular porcentajes para asegurar que estén disponibles directamente
            model.addAttribute("porcentajePendientes", (actividadesPendientes * 100.0 / totalActividades));
            model.addAttribute("porcentajeEnProgreso", (actividadesEnProceso * 100.0 / totalActividades));
            model.addAttribute("porcentajeFinalizadas", (actividadesFinalizadas * 100.0 / totalActividades));
        }

        return "desarrollador/index";
    }

    @GetMapping("/actividades")
    public String listarMisActividades(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/login?error=Usuario no encontrado";
        }

        Usuario desarrollador = usuarioOpt.get();
        List<Actividad> actividades = actividadRepositorio.findByDesarrolladorId(desarrollador.getId());

        List<Errores> errores = errorRepositorio.findByDesarrolladorId(desarrollador.getId());

        List<Interrupciones> interrupciones = interrupcionRepositorio.findByDesarrolladorId(desarrollador.getId());

        if (!actividades.isEmpty()) {
            Etapa etapa = actividades.get(0).getEtapa();
            Proyecto proyecto = etapa.getProyecto();

            model.addAttribute("proyecto", proyecto);
            model.addAttribute("etapa", etapa);
        }

        model.addAttribute("actividades", actividades);
        model.addAttribute("errores", errores);
        model.addAttribute("interrupciones", interrupciones);
        return "desarrollador/actividades";
    }

    @PostMapping("/actividades/CambiarEstado/{id}")
    public String cambiarEstadoActividad(@PathVariable Integer id, @RequestParam EstadoActividad estado) {
        Optional<Actividad> actividadOpt = actividadRepositorio.findById(id);

        if (actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            actividad.setEstado(estado);
            actividadRepositorio.save(actividad);
        }

        return "redirect:/desarrollador/actividades?success=Estado actualizado";
    }

    // Método adaptado para el registro de errores desde el modal
    @PostMapping("/Errores/Registrar")
    public String registrarError(
            @RequestParam Integer proyectoId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam TipoError tipo,
            @RequestParam String fase) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(proyectoId);

        if (usuarioOpt.isEmpty() || proyectoOpt.isEmpty()) {
            return "redirect:/desarrollador/actividades?error=No se encontró el usuario o proyecto";
        }

        Usuario desarrollador = usuarioOpt.get();
        Proyecto proyecto = proyectoOpt.get();

        Errores nuevoError = new Errores();
        nuevoError.setNombre(nombre);
        nuevoError.setDescripcion(descripcion);
        nuevoError.setTipo(tipo);
        nuevoError.setFase(fase);
        nuevoError.setDesarrollador(desarrollador);
        nuevoError.setProyecto(proyecto);

        errorRepositorio.save(nuevoError);

        return "redirect:/desarrollador/actividades?success=Error registrado correctamente";
    }

    // Método adaptado para el registro de interrupciones desde el modal
    @PostMapping("/Interrupciones/Registrar")
    public String registrarInterrupcion(
            @RequestParam Integer proyectoId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam TipoInterrupcion tipo,
            @RequestParam String fecha,
            @RequestParam Integer duracionMinutos,
            @RequestParam String fase) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(proyectoId);

        if (usuarioOpt.isEmpty() || proyectoOpt.isEmpty()) {
            return "redirect:/desarrollador/actividades?error=No se encontró el usuario o proyecto";
        }

        Usuario desarrollador = usuarioOpt.get();
        Proyecto proyecto = proyectoOpt.get();

        Date fechaInterrupcion;
        try {
            fechaInterrupcion = new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
        } catch (ParseException e) {
            return "redirect:/desarrollador/actividades?error=Fecha inválida";
        }

        Interrupciones interrupcion = new Interrupciones();
        interrupcion.setNombre(nombre);
        interrupcion.setDescripcion(descripcion);
        interrupcion.setTipo(tipo);
        interrupcion.setFecha(fechaInterrupcion);
        interrupcion.setDuracionMinutos(duracionMinutos);
        interrupcion.setFase(fase);
        interrupcion.setDesarrollador(desarrollador);
        interrupcion.setProyecto(proyecto);

        interrupcionRepositorio.save(interrupcion);
        return "redirect:/desarrollador/actividades?success=Interrupción registrada";
    }

    // Nuevos métodos para listar errores e interrupciones en formato JSON
    @GetMapping("/Errores/Listar")
    @ResponseBody
    public List<Errores> listarErrores() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Usuario desarrollador = usuarioOpt.get();
        return errorRepositorio.findByDesarrolladorId(desarrollador.getId());
    }

    @GetMapping("/Interrupciones/Listar")
    @ResponseBody
    public List<Interrupciones> listarInterrupciones() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Usuario desarrollador = usuarioOpt.get();
        return interrupcionRepositorio.findByDesarrolladorId(desarrollador.getId());
    }

    // Mantengo los métodos GET por si se quieren usar en el futuro
    @GetMapping("/Errores/Registrar/{proyectoId}")
    public String mostrarFormularioRegistrarError(@PathVariable Integer proyectoId, Model model) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(proyectoId);
        if (proyectoOpt.isEmpty()) {
            return "redirect:/desarrollador/actividades?error=Proyecto no encontrado";
        }

        model.addAttribute("proyecto", proyectoOpt.get());
        model.addAttribute("tiposError", TipoError.values());
        return "desarrollador/RegistrarError";
    }

    @GetMapping("/Interrupciones/Registrar/{proyectoId}")
    public String mostrarFormularioInterrupcion(@PathVariable Integer proyectoId, Model model) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(proyectoId);
        if (proyectoOpt.isEmpty()) {
            return "redirect:/desarrollador/actividades?error=Proyecto no encontrado";
        }

        model.addAttribute("proyecto", proyectoOpt.get());
        model.addAttribute("tiposInterrupcion", TipoInterrupcion.values());
        return "desarrollador/RegistrarInterrupcion";
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        String email = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/index?error=Usuario no encontrado";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        return "/desarrollador/perfil";
    }

    @PostMapping("/actualizarPerfil")
    public @ResponseBody String actualizarPerfil(
            @RequestParam Integer id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam String profesion,
            @RequestParam String especialidad,
            @RequestParam Long identificacion,
            @RequestParam(required = false) String contrasena,
            @RequestParam("foto") MultipartFile fotoPerfil,
            Authentication authentication) {

        // Verificar que el usuario sea el propietario del perfil
        String currentUserEmail = authentication.getName();
        Optional<Usuario> currentUserOpt = usuarioRepositorio.findByEmail(currentUserEmail);

        if (currentUserOpt.isEmpty() || !currentUserOpt.get().getId().equals(id)) {
            return "error:No autorizado";
        }

        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isEmpty()) {
            return "error:Usuario no encontrado";
        }

        try {
            Usuario usuario = usuarioOpt.get();

            // Verificar si el email ya está en uso por otro usuario
            Optional<Usuario> emailExistsOpt = usuarioRepositorio.findByEmail(email);
            if (emailExistsOpt.isPresent() && !emailExistsOpt.get().getId().equals(id)) {
                return "error:El correo electrónico ya está registrado por otro usuario";
            }

            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            usuario.setProfesion(profesion);
            usuario.setEspecialidad(especialidad);

            // Actualizar contraseña solo si se proporcionó una nueva
            if (contrasena != null && !contrasena.isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(contrasena));
            }

            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                usuario.setFotoPerfil(fotoPerfil.getBytes());
            }

            usuarioRepositorio.save(usuario);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }

    @GetMapping("/biblioteca")
    public String mostrarBibliotecas() {
        return "desarrollador/biblioteca";
    }

 
}
