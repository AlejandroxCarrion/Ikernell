package com.ikernell.controller;

import com.ikernell.model.*;
import com.ikernell.repository.*;
import com.ikernell.service.EtapaService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.http.HttpHeaders;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.stream.Collectors;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lider")
public class LiderController {

    @Autowired
    private EtapaService etapaService;

    private final InterrupcionRepositorio interrupcionRepositorio;
    private final ActividadRepositorio actividadRepositorio;
    private final ProyectoRepositorio proyectoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final EtapaRepositorio etapaRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final RolRepositorio rolRepositorio;
    private final ErrorRepositorio errorRepositorio;



    public LiderController(ProyectoRepositorio proyectoRepositorio, UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio, EtapaRepositorio etapaRepositorio, ActividadRepositorio actividadRepositorio, InterrupcionRepositorio interrupcionRepositorio, PasswordEncoder passwordEncoder, RolRepositorio rolRepositorio1, ErrorRepositorio errorRepositorio) {
        this.interrupcionRepositorio = interrupcionRepositorio;
        this.proyectoRepositorio = proyectoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.etapaRepositorio = etapaRepositorio;
        this.actividadRepositorio = actividadRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.rolRepositorio = rolRepositorio1;
        this.errorRepositorio = errorRepositorio;
    }

    @GetMapping("/index")
    public String mostrarDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/login?error=Usuario no encontrado";
        }

        Usuario lider = usuarioOpt.get();
        model.addAttribute("lider", lider);

        // Obtener proyectos liderados por este usuario
        List<Proyecto> proyectosLiderados = proyectoRepositorio.findByLiderId(lider.getId());
        model.addAttribute("proyectosLiderados", proyectosLiderados);
        model.addAttribute("cantidadProyectos", proyectosLiderados.size());

        // Contar desarrolladores en sus proyectos
        Set<Usuario> desarrolladoresUnicos = new HashSet<>();
        for (Proyecto proyecto : proyectosLiderados) {
            List<Usuario> desarrolladoresProyecto = usuarioRepositorio.findByProyectoAsignadoId(proyecto.getId());
            desarrolladoresUnicos.addAll(desarrolladoresProyecto);
        }
        model.addAttribute("cantidadDesarrolladores", desarrolladoresUnicos.size());

        // Proyectos por estado
        long proyectosHabilitados = proyectoRepositorio.countByEstado(EstadoProyecto.HABILITADO);
        model.addAttribute("proyectosHabilitados", proyectosHabilitados);

        // Obtener actividades de todos los proyectos liderados
        List<Actividad> todasActividades = new ArrayList<>();
        for (Proyecto proyecto : proyectosLiderados) {
            todasActividades.addAll(actividadRepositorio.findByEtapaProyectoId(proyecto.getId()));
        }
        model.addAttribute("cantidadActividades", todasActividades.size());

        // Actividades por estado
        Map<EstadoActividad, Long> actividadesPorEstado = todasActividades.stream()
                .collect(Collectors.groupingBy(Actividad::getEstado, Collectors.counting()));
        model.addAttribute("actividadesPorEstado", actividadesPorEstado);

        // Obtener errores en proyectos liderados
        List<Errores> erroresProyectos = new ArrayList<>();
        for (Proyecto proyecto : proyectosLiderados) {
            erroresProyectos.addAll(errorRepositorio.findByProyectoId(proyecto.getId()));
        }
        model.addAttribute("cantidadErrores", erroresProyectos.size());

        // Obtener interrupciones en proyectos liderados
        List<Interrupciones> interrupciones = new ArrayList<>();
        for (Proyecto proyecto : proyectosLiderados) {
            interrupciones.addAll(interrupcionRepositorio.findByProyectoId(proyecto.getId()));
        }
        model.addAttribute("cantidadInterrupciones", interrupciones.size());

        return "lider/index";
    }

    @GetMapping("/CrearProyecto")
    public String mostrarFormularioCrearProyecto() {
        return "lider/CrearProyecto";
    }

    @PostMapping("/Proyectos/crear")
    public String registrarProyecto(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Date fechaInicio,
            @RequestParam EstadoProyecto estado) {
    
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);
    
        if (usuarioOpt.isEmpty()) {
            return "redirect:/lider/CrearProyecto?error=Usuario no encontrado";
        }
    
        try {
            Usuario lider = usuarioOpt.get();
    
            Proyecto nuevoProyecto = new Proyecto();
            nuevoProyecto.setNombre(nombre);
            nuevoProyecto.setDescripcion(descripcion);
            nuevoProyecto.setFechaInicio(fechaInicio);
            nuevoProyecto.setEstado(estado);
            nuevoProyecto.setLider(lider);
    
            proyectoRepositorio.save(nuevoProyecto);
    
            return "redirect:/lider/Proyectos?success=Proyecto creado";
        } catch (Exception e) {
            // Captura errores como problemas en la base de datos
            return "redirect:/lider/Proyectos?error=Error al crear el proyecto";
        }
    }

    @GetMapping("/Proyectos")
    public String listarProyectos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

    if (usuarioOpt.isEmpty()) {
        return "redirect:/lider/Proyectos?error=Usuario no encontrado";
    }

        Usuario lider = usuarioOpt.get();

        model.addAttribute("estados", EstadoProyecto.values());


        List<Proyecto> proyectos = proyectoRepositorio.findByLider(lider);
        model.addAttribute("proyectos", proyectos);

        return "lider/Proyectos";
    }

    @GetMapping("/Proyectos/modificar/{id}")
    public String mostrarFormularioModificar(@PathVariable Integer id, Model model) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(id);
        if (proyectoOpt.isEmpty()) {
            return "redirect:/lider/Proyectos?error=Proyecto no encontrado";
        }

    model.addAttribute("proyecto", proyectoOpt.get());
    return "lider/EditarProyecto";
    }

    @PostMapping("/Proyectos/modificar/{id}")
    public String actualizarProyecto(@PathVariable Integer id, 
                                 @RequestParam String nombre,
                                 @RequestParam String descripcion,
                                 @RequestParam Date fechaInicio,
                                 @RequestParam EstadoProyecto estado) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(id);
        if (proyectoOpt.isPresent()) {
            Proyecto proyecto = proyectoOpt.get();
            proyecto.setNombre(nombre);
            proyecto.setDescripcion(descripcion);
            proyecto.setFechaInicio(fechaInicio);
            proyecto.setEstado(estado);
            proyectoRepositorio.save(proyecto);
        }

        return "redirect:/lider/Proyectos?success=Proyecto actualizado";
    }

    @GetMapping("/Proyectos/detalle/{id}")
    public String detallesProyecto(@PathVariable Integer id, Model model) {
        Proyecto proyecto = proyectoRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no válido: " + id));

        List<Etapa> etapas = etapaRepositorio.findByProyectoId(id);
        List<Usuario> desarrolladores = usuarioRepositorio.findByRolId(3);

        // Obtener todas las actividades del proyecto para poder mostrarlas en modales
        List<Actividad> actividadesProyecto = actividadRepositorio.findByEtapaProyectoId(id);

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("etapas", etapas);
        model.addAttribute("desarrolladores", desarrolladores);
        model.addAttribute("actividadesProyecto", actividadesProyecto);

        return "lider/DetallesProyecto";
    }


    @PostMapping("/Proyectos/cambiarEstado/{id}")
    public String cambiarEstadoProyecto(@PathVariable Integer id) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(id);

        if (proyectoOpt.isPresent()) {
            Proyecto proyecto = proyectoOpt.get();
            proyecto.setEstado(proyecto.getEstado() == EstadoProyecto.HABILITADO ? EstadoProyecto.INHABILITADO : EstadoProyecto.HABILITADO);
            proyectoRepositorio.save(proyecto);
        }

        return "redirect:/lider/Proyectos";
    }


    @PostMapping("/Proyectos/{proyectoId}/AgregarEtapa")
    public String registrarEtapa(@PathVariable Integer proyectoId,
                                 @RequestParam String nombre,
                                 @RequestParam String descripcion,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(proyectoId);

        if (proyectoOpt.isPresent()) {
            Proyecto proyecto = proyectoOpt.get();
            Etapa etapa = new Etapa();
            etapa.setNombre(nombre);
            etapa.setDescripcion(descripcion);
            etapa.setFechaInicio(fechaInicio);
            etapa.setFechaFinal(fechaFinal);
            etapa.setProyecto(proyecto);

            etapaRepositorio.save(etapa);
        }

        return "redirect:/lider/Proyectos/detalle/" + proyectoId + "#etapas-tab";
    }


    @PostMapping("/Proyectos/{proyectoId}/Etapas/Editar/{id}")
    public String actualizarEtapa(@PathVariable Integer proyectoId, @PathVariable Integer id,
                                  @RequestParam String nombre,
                                  @RequestParam String descripcion,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal) {
        Optional<Etapa> etapaOpt = etapaRepositorio.findById(id);

        if (etapaOpt.isPresent()) {
            Etapa etapa = etapaOpt.get();
            etapa.setNombre(nombre);
            etapa.setDescripcion(descripcion);
            etapa.setFechaFinal(fechaFinal);

            etapaRepositorio.save(etapa);
        }

        return "redirect:/lider/Proyectos/detalle/" + proyectoId + "#etapas-tab";
    }

    @PostMapping("/Proyectos/{proyectoId}/Etapas/Eliminar/{id}")
    public String eliminarEtapa(@PathVariable Integer proyectoId, @PathVariable Integer id) {
        boolean eliminada = etapaService.eliminarEtapa(id);

        if (!eliminada) {
            return "redirect:/lider/Proyectos/detalle/" + proyectoId + "#etapas-tab/?error=Error al eliminar la etapa";
        }

        return "redirect:/lider/Proyectos/detalle/" + proyectoId + "#etapas-tab/?success=Etapa eliminada";
    }



    @GetMapping("/Actividades/Agregar/{etapaId}")
    public String mostrarFormularioAgregarActividad(@PathVariable Integer etapaId, Model model) {
        Optional<Etapa> etapaOpt = etapaRepositorio.findById(etapaId);
        if (etapaOpt.isEmpty()) {
            return "redirect:/lider/Proyectos?error=Etapa no encontrada";
        }

        List<Usuario> desarrolladores = usuarioRepositorio.findByRolId(3);

        model.addAttribute("etapa", etapaOpt.get());
        model.addAttribute("desarrolladores", desarrolladores);
        return "lider/AgregarActividad";
    }

    @PostMapping("/actividades/agregar")
    public String agregarActividad(@RequestParam Integer etapaId,
                                   @RequestParam String nombre,
                                   @RequestParam String descripcion,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal,
                                   @RequestParam Integer desarrolladorId) {

        Etapa etapa = etapaRepositorio.findById(etapaId)
                .orElseThrow(() -> new IllegalArgumentException("Etapa no válida"));

        Usuario desarrollador = usuarioRepositorio.findById(desarrolladorId)
                .orElseThrow(() -> new IllegalArgumentException("Desarrollador no válido"));

        Actividad nuevaActividad = new Actividad(
                nombre,
                descripcion,
                EstadoActividad.PENDIENTE,
                desarrollador,
                etapa,
                fechaInicio,
                fechaFinal
        );

        actividadRepositorio.save(nuevaActividad);
        return "redirect:/lider/actividades/" + etapaId + "?success=Actividad agregada correctamente";
    }


    @PostMapping("/Actividades/CambiarEstado/{id}")
    public String cambiarEstadoActividadLider(@PathVariable Integer id, @RequestParam EstadoActividad estado) {
        Optional<Actividad> actividadOpt = actividadRepositorio.findById(id);
        if (actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            actividad.setEstado(estado);
            actividadRepositorio.save(actividad);
            return "redirect:/lider/actividades/" + actividad.getEtapa().getId() + "?success=Estado actualizado";
        }
        return "redirect:/lider/Proyectos?error=Actividad no encontrada";
    }

    @PostMapping("/Actividades/cambiarEstado/{actividadId}")
    public String cambiarEstadoActividad(@PathVariable Integer actividadId,
                                         @RequestParam EstadoActividad nuevoEstado,
                                         HttpServletRequest request) {
        Optional<Actividad> actividadOpt = actividadRepositorio.findById(actividadId);

        if (actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            actividad.setEstado(nuevoEstado);

            // Si pasa a EN_PROCESO, registramos la fecha de inicio
            if (nuevoEstado == EstadoActividad.EN_PROCESO && actividad.getFechaInicio() == null) {
                actividad.setFechaInicio(LocalDate.now());
            }

            // Si está COMPLETADA, registramos la fecha de finalización
            if (nuevoEstado == EstadoActividad.COMPLETADA && actividad.getFechaFinal() == null) {
                actividad.setFechaFinal(LocalDate.now());
            }

            actividadRepositorio.save(actividad);
        }

        // Redirigir a la página anterior
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/lider/actividades/");
    }


    @GetMapping("/Actividades/Editar/{id}")
    public String editarActividad(@PathVariable Integer id, Model model) {
        Optional<Actividad> actividadOpt = actividadRepositorio.findById(id);
        if (actividadOpt.isEmpty()) {
            return "redirect:/lider/Actividades/Listado?error=Actividad no encontrada";
        }

        model.addAttribute("actividad", actividadOpt.get());
        model.addAttribute("estados", EstadoActividad.values());
        model.addAttribute("desarrolladores", usuarioRepositorio.findByRolId(3));

        return "lider/EditarActividad";
    }

    @PostMapping("/actividades/editar/{id}")
    public String editarActividad(@PathVariable Integer id,
                                  @RequestParam String nombre,
                                  @RequestParam String descripcion,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinal,
                                  @RequestParam EstadoActividad estado,
                                  @RequestParam Integer desarrolladorId) {

        Actividad actividad = actividadRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));

        Usuario desarrollador = usuarioRepositorio.findById(desarrolladorId)
                .orElseThrow(() -> new IllegalArgumentException("Desarrollador no válido"));

        actividad.setNombre(nombre);
        actividad.setDescripcion(descripcion);
        actividad.setFechaInicio(fechaInicio);
        actividad.setFechaFinal(fechaFinal);
        actividad.setEstado(estado);
        actividad.setDesarrollador(desarrollador);

        Integer etapaId = actividad.getEtapa().getId();

        actividadRepositorio.save(actividad);
        return "redirect:/lider/actividades/" + etapaId + "?success=Actividad actualizada";
    }


    @PostMapping("/Actividades/eliminar/{actividadId}")
    public String eliminarActividad(@PathVariable Integer actividadId, HttpServletRequest request) {
        Optional<Actividad> actividadOpt = actividadRepositorio.findById(actividadId);

        if (actividadOpt.isPresent()) {
            Actividad actividad = actividadOpt.get();
            Integer etapaId = actividad.getEtapa().getId();

            actividadRepositorio.delete(actividad);
        }

        // Redirigir a la página anterior
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/lider/actividades/");
    }


    @GetMapping("/actividades/{etapaId}")
    public String mostrarActividadesPorEtapa(@PathVariable Integer etapaId, Model model) {
        // Obtener la etapa y sus actividades
        Optional<Etapa> etapaOptional = etapaRepositorio.findById(etapaId);

        if (etapaOptional.isPresent()) {
            Etapa etapa = etapaOptional.get();
            Proyecto proyecto = etapa.getProyecto(); // Obtenemos el proyecto asociado

            List<Actividad> todasLasActividades = actividadRepositorio.findByEtapaId(etapaId);
            List<Etapa> etapas = etapaRepositorio.findAll();
            List<Usuario> desarrolladores = usuarioRepositorio.findByRolId(3);

            // Filtrar actividades por estado
            List<Actividad> actividadesPendientes = todasLasActividades.stream()
                    .filter(a -> a.getEstado() == EstadoActividad.PENDIENTE)
                    .collect(Collectors.toList());

            List<Actividad> actividadesEnProceso = todasLasActividades.stream()
                    .filter(a -> a.getEstado() == EstadoActividad.EN_PROCESO)
                    .collect(Collectors.toList());

            List<Actividad> actividadesCompletadas = todasLasActividades.stream()
                    .filter(a -> a.getEstado() == EstadoActividad.COMPLETADA)
                    .collect(Collectors.toList());

            // Agregar todos los atributos necesarios para la vista
            model.addAttribute("etapa", etapa);
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("proyectoId", proyecto.getId()); // Añadimos explícitamente el ID del proyecto
            model.addAttribute("todasLasActividades", todasLasActividades);
            model.addAttribute("actividadesPendientes", actividadesPendientes);
            model.addAttribute("actividadesEnProceso", actividadesEnProceso);
            model.addAttribute("actividadesCompletadas", actividadesCompletadas);
            model.addAttribute("etapas", etapas);
            model.addAttribute("desarrolladores", desarrolladores);

            return "lider/actividades";
        } else {
            // Manejar caso donde la etapa no existe
            return "redirect:/lider/Proyectos";
        }
    }

    @GetMapping("/actividades/")
    public String listarActividades(Model model) {
        List<Actividad> todasLasActividades = actividadRepositorio.findAll();
        List<Etapa> etapas = etapaRepositorio.findAll();
        List<Usuario> desarrolladores = usuarioRepositorio.findByRolId(3);

        List<Actividad> actividadesPendientes = todasLasActividades.stream()
                .filter(a -> a.getEstado() == EstadoActividad.PENDIENTE)
                .collect(Collectors.toList());

        List<Actividad> actividadesEnProceso = todasLasActividades.stream()
                .filter(a -> a.getEstado() == EstadoActividad.EN_PROCESO)
                .collect(Collectors.toList());

        List<Actividad> actividadesCompletadas = todasLasActividades.stream()
                .filter(a -> a.getEstado() == EstadoActividad.COMPLETADA)
                .collect(Collectors.toList());

        model.addAttribute("todasLasActividades", todasLasActividades);
        model.addAttribute("actividadesPendientes", actividadesPendientes);
        model.addAttribute("actividadesEnProceso", actividadesEnProceso);
        model.addAttribute("actividadesCompletadas", actividadesCompletadas);
        model.addAttribute("etapas", etapas);
        model.addAttribute("desarrolladores", desarrolladores);

        return "actividades";
    }



    @GetMapping("/Desarrolladores")
    public String listarDesarrolladores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Usuario> paginaDesarrolladores = usuarioRepositorio.findByRolId(3, pageable);
        List<Usuario> desarrolladores = paginaDesarrolladores.getContent();

        Map<Integer, Long> actividadesPorDesarrollador = desarrolladores.stream()
                .collect(Collectors.toMap(
                        Usuario::getId,
                        dev -> actividadRepositorio.countByDesarrolladorId(dev.getId())
                ));

        Map<Integer, List<Actividad>> actividadesDesarrollador = desarrolladores.stream()
                .collect(Collectors.toMap(
                        Usuario::getId,
                        dev -> actividadRepositorio.findByDesarrolladorId(dev.getId())
                ));

        model.addAttribute("desarrolladores", desarrolladores);
        model.addAttribute("actividadesPorDesarrollador", actividadesPorDesarrollador);
        model.addAttribute("actividadesDesarrollador", actividadesDesarrollador);
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaDesarrolladores.getTotalPages());
        model.addAttribute("etapas", etapaRepositorio.findAll());

        return "lider/Desarrolladores";
    }

    @PostMapping("/Actividades/Asignar")
    public String asignarActividad(
            @RequestParam Integer desarrolladorId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Integer etapaId,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> desarrolladorOpt = usuarioRepositorio.findById(desarrolladorId);
        Optional<Etapa> etapaOpt = etapaRepositorio.findById(etapaId);

        if (desarrolladorOpt.isEmpty() || etapaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Desarrollador o etapa no encontrados");
            return "redirect:/lider/Desarrolladores";
        }

        Actividad nuevaActividad = new Actividad();
        nuevaActividad.setNombre(nombre);
        nuevaActividad.setDescripcion(descripcion);
        nuevaActividad.setEstado(EstadoActividad.PENDIENTE);
        nuevaActividad.setDesarrollador(desarrolladorOpt.get());
        nuevaActividad.setEtapa(etapaOpt.get());
        nuevaActividad.setFechaInicio(LocalDate.now());
        nuevaActividad.setFechaFinal(LocalDate.now().plusDays(7)); // Default 7 days

        actividadRepositorio.save(nuevaActividad);

        redirectAttributes.addFlashAttribute("success", "Actividad asignada correctamente");
        return "redirect:/lider/Desarrolladores";
    }

    @GetMapping("/Proyectos/generarReporte/{proyectoId}")
    public void generarReporteInterrupciones(@PathVariable Integer proyectoId, HttpServletResponse response) throws IOException, DocumentException {
        // 🔹 Configurar respuesta HTTP para descargar PDF
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Interrupciones_Proyecto_" + proyectoId + ".pdf");

        // 🔹 Buscar proyecto e interrupciones
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(proyectoId);
        if (proyectoOpt.isEmpty()) {
            response.getWriter().write("Proyecto no encontrado");
            return;
        }
        Proyecto proyecto = proyectoOpt.get();

        // Obtener todas las interrupciones relacionadas con el proyecto
        List<Interrupciones> interrupciones = interrupcionRepositorio.findByProyectoId(proyectoId);

        // 🔹 Configurar documento
        Document documento = new Document(PageSize.A4, 36, 36, 54, 36); // Márgenes para encabezado
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, response.getOutputStream());

            // Añadir encabezado a cada página
            writer.setPageEvent(new HeaderFooter("src/main/resources/static/img/logo.png"));

            documento.open();

            // 🔹 Definir estilos
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
            Font contenidoFont = new Font(Font.FontFamily.HELVETICA, 12);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            // 🔹 Título del documento
            Paragraph titulo = new Paragraph("Reporte de Interrupciones del Proyecto", tituloFont);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);

            // 🔹 Información del proyecto
            PdfPTable infoProyecto = new PdfPTable(2);
            infoProyecto.setWidthPercentage(100);
            infoProyecto.setSpacingBefore(10);
            infoProyecto.setSpacingAfter(20);

            float[] anchosInfo = {1.0f, 2.0f};
            infoProyecto.setWidths(anchosInfo);

            PdfPCell celdaProyecto = new PdfPCell(new Phrase("Información del Proyecto", subtituloFont));
            celdaProyecto.setColspan(2);
            celdaProyecto.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaProyecto.setPadding(10);
            celdaProyecto.setBackgroundColor(new BaseColor(240, 240, 240));
            infoProyecto.addCell(celdaProyecto);

            // Datos básicos del proyecto
            addRow(infoProyecto, "Nombre del Proyecto:", proyecto.getNombre(), labelFont, contenidoFont);
            addRow(infoProyecto, "Estado:", proyecto.getEstado().name(), labelFont, contenidoFont);
            addRow(infoProyecto, "Total de Interrupciones:", String.valueOf(interrupciones.size()), labelFont, contenidoFont);

            documento.add(infoProyecto);

            // 🔹 Tabla de Interrupciones
            if (!interrupciones.isEmpty()) {
                Paragraph interrupcionesTitulo = new Paragraph("Listado de Interrupciones", subtituloFont);
                interrupcionesTitulo.setSpacingBefore(10);
                interrupcionesTitulo.setSpacingAfter(10);
                documento.add(interrupcionesTitulo);

                PdfPTable tablaInterrupciones = new PdfPTable(5);
                tablaInterrupciones.setWidthPercentage(100);

                float[] anchosInterrupciones = {1.0f, 1.5f, 1.0f, 1.0f, 1.5f};
                tablaInterrupciones.setWidths(anchosInterrupciones);

                // Encabezados
                PdfPCell headerCell;
                String[] encabezados = {"Fecha", "Nombre", "Tipo", "Duración (min)", "Fase"};
                for (String encabezado : encabezados) {
                    headerCell = new PdfPCell(new Phrase(encabezado, labelFont));
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    headerCell.setBackgroundColor(new BaseColor(240, 240, 240));
                    headerCell.setPadding(8);
                    tablaInterrupciones.addCell(headerCell);
                }

                // Añadir datos
                for (Interrupciones i : interrupciones) {
                    tablaInterrupciones.addCell(new Phrase(i.getFecha().toString(), contenidoFont));
                    tablaInterrupciones.addCell(new Phrase(i.getNombre(), contenidoFont));
                    tablaInterrupciones.addCell(new Phrase(i.getTipo().name(), contenidoFont));
                    tablaInterrupciones.addCell(new Phrase(i.getDuracionMinutos().toString(), contenidoFont));
                    tablaInterrupciones.addCell(new Phrase(i.getFase(), contenidoFont));
                }

                documento.add(tablaInterrupciones);
            } else {
                Paragraph noData = new Paragraph("No se han registrado interrupciones para este proyecto.", contenidoFont);
                noData.setSpacingBefore(10);
                documento.add(noData);
            }

            java.util.Date fechaActual = new java.util.Date();
            // 🔹 Pie de página con fecha de generación
            Paragraph fechaGeneracion = new Paragraph("Reporte generado el: " +
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fechaActual),
                    new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
            fechaGeneracion.setSpacingBefore(30);
            fechaGeneracion.setAlignment(Element.ALIGN_RIGHT);
            documento.add(fechaGeneracion);

            documento.close();
        } catch (Exception e) {
            // Manejar excepción
            documento.close();
            throw e;
        }
    }




    private void addTableHeader(PdfPTable table, String... headers) {
        if (headers == null || headers.length == 0) {
            throw new IllegalArgumentException("Los encabezados de la tabla no pueden estar vacíos.");
        }

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell);
        }
    }

    // Método auxiliar para añadir filas a la tabla
    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setPadding(8);
        cellLabel.setBackgroundColor(new BaseColor(245, 245, 245));

        PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
        cellValue.setPadding(8);

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }

    @GetMapping("/Proyectos/generarReporteActividades/{proyectoId}")
    public void generarReporteActividades(@PathVariable("proyectoId") Integer proyectoId, HttpServletResponse response) throws IOException, DocumentException {
        // 🔹 Configurar respuesta HTTP para descargar PDF
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Actividades_Proyecto_" + proyectoId + ".pdf");

        // 🔹 Buscar proyecto y actividades
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(proyectoId);
        if (proyectoOpt.isEmpty()) {
            response.getWriter().write("Proyecto no encontrado");
            return;
        }
        Proyecto proyecto = proyectoOpt.get();

        // Obtener todas las actividades relacionadas con el proyecto
        List<Actividad> actividades = actividadRepositorio.findByEtapaProyectoId(proyectoId);

        // 🔹 Configurar documento
        Document documento = new Document(PageSize.A4, 36, 36, 54, 36); // Márgenes para encabezado
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, response.getOutputStream());

            // Añadir encabezado a cada página
            writer.setPageEvent(new HeaderFooter("src/main/resources/static/img/logo.png"));

            documento.open();

            // 🔹 Definir estilos
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
            Font contenidoFont = new Font(Font.FontFamily.HELVETICA, 12);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            // 🔹 Título del documento
            Paragraph titulo = new Paragraph("Reporte de Actividades del Proyecto", tituloFont);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);

            // 🔹 Información del proyecto
            PdfPTable infoProyecto = new PdfPTable(2);
            infoProyecto.setWidthPercentage(100);
            infoProyecto.setSpacingBefore(10);
            infoProyecto.setSpacingAfter(20);

            float[] anchosInfo = {1.0f, 2.0f};
            infoProyecto.setWidths(anchosInfo);

            PdfPCell celdaProyecto = new PdfPCell(new Phrase("Información del Proyecto", subtituloFont));
            celdaProyecto.setColspan(2);
            celdaProyecto.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaProyecto.setPadding(10);
            celdaProyecto.setBackgroundColor(new BaseColor(240, 240, 240));
            infoProyecto.addCell(celdaProyecto);

            // Datos básicos del proyecto
            addRow(infoProyecto, "Nombre del Proyecto:", proyecto.getNombre(), labelFont, contenidoFont);
            addRow(infoProyecto, "Estado:", proyecto.getEstado().name(), labelFont, contenidoFont);
            addRow(infoProyecto, "Total de Actividades:", String.valueOf(actividades.size()), labelFont, contenidoFont);

            documento.add(infoProyecto);

            // 🔹 Tabla de Actividades
            if (!actividades.isEmpty()) {
                Paragraph actividadesTitulo = new Paragraph("Listado de Actividades", subtituloFont);
                actividadesTitulo.setSpacingBefore(10);
                actividadesTitulo.setSpacingAfter(10);
                documento.add(actividadesTitulo);

                PdfPTable tablaActividades = new PdfPTable(5);
                tablaActividades.setWidthPercentage(100);

                float[] anchosActividades = {0.8f, 2.0f, 1.5f, 1.0f, 1.0f};
                tablaActividades.setWidths(anchosActividades);

                // Encabezados
                String[] encabezados = {"Etapa", "Actividad", "Desarrollador", "Estado", "Prioridad"};
                for (String encabezado : encabezados) {
                    PdfPCell celda = new PdfPCell(new Phrase(encabezado, labelFont));
                    celda.setBackgroundColor(new BaseColor(230, 230, 230));
                    celda.setPadding(7);
                    tablaActividades.addCell(celda);
                }

                // Datos de actividades
                for (Actividad actividad : actividades) {
                    tablaActividades.addCell(new Phrase(actividad.getEtapa().getNombre(), contenidoFont));
                    tablaActividades.addCell(new Phrase(actividad.getDescripcion(), contenidoFont));

                    String desarrollador = actividad.getDesarrollador() != null ?
                            actividad.getDesarrollador().getNombre() + " " + actividad.getDesarrollador().getApellido() : "No asignado";
                    tablaActividades.addCell(new Phrase(desarrollador, contenidoFont));

                    tablaActividades.addCell(new Phrase(actividad.getEstado().name(), contenidoFont));
                }

                documento.add(tablaActividades);

                // Agregar estadísticas de actividades
                documento.add(new Paragraph("\n"));

                Paragraph estadisticasTitulo = new Paragraph("Estadísticas de Actividades", subtituloFont);
                estadisticasTitulo.setSpacingBefore(15);
                estadisticasTitulo.setSpacingAfter(10);
                documento.add(estadisticasTitulo);

                // Contar actividades por estado
                Map<EstadoActividad, Long> actividadesPorEstado = actividades.stream()
                        .collect(Collectors.groupingBy(Actividad::getEstado, Collectors.counting()));

                PdfPTable tablaEstadisticas = new PdfPTable(2);
                tablaEstadisticas.setWidthPercentage(70);
                tablaEstadisticas.setHorizontalAlignment(Element.ALIGN_LEFT);

                PdfPCell celdaEstadoTitle = new PdfPCell(new Phrase("Estado", labelFont));
                PdfPCell celdaCantidadTitle = new PdfPCell(new Phrase("Cantidad", labelFont));

                celdaEstadoTitle.setBackgroundColor(new BaseColor(230, 230, 230));
                celdaCantidadTitle.setBackgroundColor(new BaseColor(230, 230, 230));
                celdaEstadoTitle.setPadding(7);
                celdaCantidadTitle.setPadding(7);

                tablaEstadisticas.addCell(celdaEstadoTitle);
                tablaEstadisticas.addCell(celdaCantidadTitle);

                for (EstadoActividad estado : EstadoActividad.values()) {
                    Long cantidad = actividadesPorEstado.getOrDefault(estado, 0L);
                    tablaEstadisticas.addCell(new Phrase(estado.name(), contenidoFont));
                    tablaEstadisticas.addCell(new Phrase(cantidad.toString(), contenidoFont));
                }

                documento.add(tablaEstadisticas);
            } else {
                documento.add(new Paragraph("No hay actividades registradas para este proyecto.", contenidoFont));
            }

            documento.close();
        } catch (DocumentException e) {
            e.printStackTrace();
            response.getWriter().write("Error al generar el documento: " + e.getMessage());
        }
    }

    // Clase para manejar el encabezado de cada página
    class HeaderFooter extends PdfPageEventHelper {
        private String logoPath;

        public HeaderFooter(String logoPath) {
            this.logoPath = logoPath;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfPTable header = new PdfPTable(2);
                header.setWidthPercentage(100);
                header.setWidths(new float[]{1, 3});
                header.setSpacingAfter(30);

                // Logo de la empresa - Manejo mejorado del logo
                PdfPCell logoCell;
                try {
                    // Intenta cargar el logo desde la ruta del sistema de archivos
                    File logoFile = new File(logoPath);
                    if (logoFile.exists()) {
                        Image logo = Image.getInstance(logoFile.getAbsolutePath());
                        logo.scaleToFit(80, 80);
                        logoCell = new PdfPCell(logo);
                    } else {
                        // Si no existe el archivo, crear un espacio para el logo con texto
                        Paragraph logoText = new Paragraph("LOGO", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
                        logoCell = new PdfPCell(logoText);
                        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    }
                } catch (Exception e) {
                    // Si hay error, crear un espacio para el logo con texto
                    Paragraph logoText = new Paragraph("LOGO", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
                    logoCell = new PdfPCell(logoText);
                    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                }

                logoCell.setPadding(5);
                logoCell.setBorder(Rectangle.NO_BORDER);
                header.addCell(logoCell);

                // Nombre de la empresa
                Font empresaFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
                Font sloganFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);

                Paragraph empresaInfo = new Paragraph();
                empresaInfo.add(new Chunk("IKernell Soluciones Software\n", empresaFont));
                empresaInfo.add(new Chunk("Innovación y Calidad en Desarrollo de Software", sloganFont));

                PdfPCell empresaCell = new PdfPCell(empresaInfo);
                empresaCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                empresaCell.setPadding(5);
                empresaCell.setBorder(Rectangle.NO_BORDER);

                header.addCell(empresaCell);

                // Línea separadora
                PdfPTable lineTable = new PdfPTable(1);
                lineTable.setWidthPercentage(100);
                PdfPCell line = new PdfPCell(new Phrase(""));
                line.setBorder(Rectangle.BOTTOM);
                line.setBorderWidth(1);
                line.setBorderColor(BaseColor.GRAY);
                line.setPaddingBottom(5);
                lineTable.addCell(line);

                document.add(header);
                document.add(lineTable);

            } catch (Exception e) {
                // Manejar la excepción
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/Proyectos/generarReporteTodos")
    public void generarReporteTodosProyectos(HttpServletResponse response) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> liderOpt = usuarioRepositorio.findByEmail(email);

        if (liderOpt.isEmpty()) {
            response.sendRedirect("/lider/Proyectos?error=Usuario no encontrado");
            return;
        }

        Usuario lider = liderOpt.get();

        List<Proyecto> proyectos = proyectoRepositorio.findByLiderId(lider.getId());

        // Configurar respuesta para texto plano con extensión markdown
        response.setContentType("text/markdown");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Proyectos_Lider.md");

        PrintWriter writer = response.getWriter();

        // Título del documento
        writer.println("# Reporte de Proyectos Asignados\n");

        // Información del líder
        writer.println("## Líder del Proyecto\n");
        writer.println("Nombre: " + lider.getNombre() + " " + lider.getApellido() + "\n");

        // Tabla de proyectos
        if (proyectos.isEmpty()) {
            writer.println("**No hay proyectos asignados a este líder.**");
        } else {
            writer.println("## Lista de Proyectos\n");

            // Encabezados de la tabla
            writer.println("| ID | Nombre | Descripción | Fecha Inicio |");
            writer.println("|---|---|---|---|");

            // Datos de proyectos
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Proyecto proyecto : proyectos) {
                writer.println("| " + proyecto.getId() + " | " +
                        proyecto.getNombre() + " | " +
                        proyecto.getDescripcion() + " | " +
                        sdf.format(proyecto.getFechaInicio()) + " |");
            }
        }

        writer.flush();
        writer.close();
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        String email = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/index?error=Usuario no encontrado";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        return "/lider/perfil";
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
        return "lider/biblioteca";
    }

    
}
