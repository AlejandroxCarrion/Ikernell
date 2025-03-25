package com.ikernell.controller;

import com.ikernell.model.Proyecto;
import com.ikernell.model.EstadoProyecto;
import com.ikernell.model.EstadoUsuario;
import com.ikernell.model.Usuario;
import com.ikernell.model.Rol;
import com.ikernell.repository.ProyectoRepositorio;
import com.ikernell.repository.UsuarioRepositorio;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletResponse;

import com.ikernell.repository.RolRepositorio;
import com.ikernell.repository.ActividadRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@Controller
public class CoordinadorController {

    private final ActividadRepositorio actividadRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final ProyectoRepositorio proyectoRepositorio;
    private final PasswordEncoder passwordEncoder;

    public CoordinadorController(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio,
                                 ProyectoRepositorio proyectoRepositorio, PasswordEncoder passwordEncoder, ActividadRepositorio actividadRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.proyectoRepositorio = proyectoRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.actividadRepositorio = actividadRepositorio;
    }

    @GetMapping("/coordinador/index")
    public String mostrarIndex(Model model) {
        // Estadísticas de usuarios por rol
        model.addAttribute("totalCoordinadores", usuarioRepositorio.countByRolId(2));
        model.addAttribute("totalLideres", usuarioRepositorio.countByRolId(1));
        model.addAttribute("totalDesarrolladores", usuarioRepositorio.countByRolId(3));

        // Estadísticas de proyectos por estado
        model.addAttribute("proyectosActivos", proyectoRepositorio.countByEstado(EstadoProyecto.HABILITADO));
        model.addAttribute("proyectosInactivos", proyectoRepositorio.countByEstado(EstadoProyecto.INHABILITADO));

        // Proyectos recientes (últimos 3)
        List<Proyecto> proyectosRecientes = proyectoRepositorio.findAll(PageRequest.of(0, 3, Sort.by("fechaInicio").descending())).getContent();
        model.addAttribute("proyectosRecientes", proyectosRecientes);

        // Usuarios recientes (últimos 3)
        List<Usuario> usuariosRecientes = usuarioRepositorio.findAll(PageRequest.of(0, 3, Sort.by("fechaRegistro").descending())).getContent();
        model.addAttribute("usuariosRecientes", usuariosRecientes);

        return "/coordinador/index";
    }

    @GetMapping("/coordinador/Usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        List<Rol> roles = rolRepositorio.findAll();
        List<Proyecto> proyectos = proyectoRepositorio.findAll();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        model.addAttribute("proyectos", proyectos);

        // Agregamos un objeto Usuario vacío para el formulario de creación
        model.addAttribute("nuevoUsuario", new Usuario());

        return "/coordinador/Usuarios";
    }



    @PostMapping("/coordinador/Usuarios/nuevo")
    public String registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam Date fechaNacimiento,
            @RequestParam Long identificacion,
            @RequestParam String direccion,
            @RequestParam String profesion,
            @RequestParam String especialidad,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String contrasena,
            @RequestParam("foto") MultipartFile fotoPerfil,
            @RequestParam Integer rol,
            @RequestParam(required = false) Integer proyecto,
            Model model) {

        if (usuarioRepositorio.findByEmail(email).isPresent()) {
            return "redirect:/coordinador/Usuarios?error=El correo electrónico ya está registrado";
        }

        if (usuarioRepositorio.findByIdentificacion(identificacion).isPresent()) {
            return "redirect:/coordinador/Usuarios?error=La identificación ya está registrada";
        }

        Optional<Rol> rolSeleccionado = rolRepositorio.findById(rol);
        if (rolSeleccionado.isEmpty()) {
            return "redirect:/coordinador/Usuarios?error=El rol seleccionado no es válido";
        }

        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellido(apellido);
            nuevoUsuario.setFechaNacimiento(fechaNacimiento);
            nuevoUsuario.setIdentificacion(identificacion);
            nuevoUsuario.setDireccion(direccion);
            nuevoUsuario.setProfesion(profesion);
            nuevoUsuario.setEspecialidad(especialidad);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setTelefono(telefono);
            nuevoUsuario.setContrasena(passwordEncoder.encode(contrasena));
            nuevoUsuario.setRol(rolSeleccionado.get());

            if (proyecto != null) {
                Optional<Proyecto> proyectoSeleccionado = proyectoRepositorio.findById(proyecto);
                proyectoSeleccionado.ifPresent(nuevoUsuario::setProyectoAsignado);
            }

            if (!fotoPerfil.isEmpty()) {
                nuevoUsuario.setFotoPerfil(fotoPerfil.getBytes());
            }

            usuarioRepositorio.save(nuevoUsuario);
            return "redirect:/coordinador/Usuarios?success=Usuario creado correctamente";

        } catch (Exception e) {
            return "redirect:/coordinador/Usuarios?error=Error al crear el usuario: " + e.getMessage();
        }
    }

    // Add this to your CoordinadorController.java
    @GetMapping("/coordinador/Usuarios/obtener/{id}")
    @ResponseBody
    public Map<String, Object> obtenerUsuario(@PathVariable Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Collections.emptyMap();
        }

        Usuario usuario = usuarioOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("id", usuario.getId());
        result.put("nombre", usuario.getNombre());
        result.put("apellido", usuario.getApellido());
        // Add other simple fields

        // Add base64 image string if available
        if (usuario.getFotoPerfil() != null) {
            result.put("fotoBase64", Base64.getEncoder().encodeToString(usuario.getFotoPerfil()));
        }

        return result;
    }

    @PostMapping("/coordinador/Usuarios/editar/{id}")
    public String actualizarUsuario(@PathVariable Integer id,
                                    @RequestParam String nombre,
                                    @RequestParam String apellido,
                                    @RequestParam Date fechaNacimiento,
                                    @RequestParam Long identificacion,
                                    @RequestParam String direccion,
                                    @RequestParam String profesion,
                                    @RequestParam String especialidad,
                                    @RequestParam String email,
                                    @RequestParam String telefono,
                                    @RequestParam Integer rol,
                                    @RequestParam(required = false) Integer proyecto,
                                    @RequestParam("foto") MultipartFile fotoPerfil) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/coordinador/Usuarios?error=Usuario no encontrado";
        }

        // Verificar si el email ya está en uso por otro usuario
        Optional<Usuario> emailExistsOpt = usuarioRepositorio.findByEmail(email);
        if (emailExistsOpt.isPresent() && !emailExistsOpt.get().getId().equals(id)) {
            return "redirect:/coordinador/Usuarios?error=El correo electrónico ya está registrado por otro usuario";
        }

        try {
            Usuario usuario = usuarioOpt.get();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setFechaNacimiento(fechaNacimiento);
            usuario.setIdentificacion(identificacion);
            usuario.setDireccion(direccion);
            usuario.setProfesion(profesion);
            usuario.setEspecialidad(especialidad);
            usuario.setEmail(email);
            usuario.setTelefono(telefono);
            usuario.setRol(rolRepositorio.findById(rol).orElse(usuario.getRol()));

            if (proyecto != null) {
                Optional<Proyecto> proyectoSeleccionado = proyectoRepositorio.findById(proyecto);
                proyectoSeleccionado.ifPresent(usuario::setProyectoAsignado);
            } else {
                usuario.setProyectoAsignado(null);
            }

            if (!fotoPerfil.isEmpty()) {
                usuario.setFotoPerfil(fotoPerfil.getBytes());
            }

            usuarioRepositorio.save(usuario);
            return "redirect:/coordinador/Usuarios?success=Usuario actualizado correctamente";
        } catch (Exception e) {
            return "redirect:/coordinador/Usuarios?error=Error al actualizar el usuario: " + e.getMessage();
        }
    }

    @PostMapping("/coordinador/Usuarios/cambiarEstado/{id}")
    public String cambiarEstadoUsuario(@PathVariable Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setEstado(usuario.getEstado() == EstadoUsuario.ACTIVO ? EstadoUsuario.INACTIVO : EstadoUsuario.ACTIVO);
            usuarioRepositorio.save(usuario);
        }
        return "redirect:/coordinador/Usuarios";
    }

    // New mapping for the projects view
    @GetMapping("/coordinador/proyectos")
    public String mostrarProyectos(Model model) {
        // Get all project leaders (users with role id 1)
        int rolLiderId = 1;
        List<Usuario> lideres = usuarioRepositorio.findByRolId(rolLiderId);
        model.addAttribute("lideres", lideres);

        // Get all projects to display in the list
        List<Proyecto> proyectos = proyectoRepositorio.findAll();
        model.addAttribute("proyectos", proyectos);

        return "/coordinador/proyectos";
    }

    // Update the endpoint for project creation
    @PostMapping("/coordinador/proyectos")
    public String registrarProyecto(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Date fechaInicio,
            @RequestParam EstadoProyecto estado,
            @RequestParam(required = false) Integer lider,
            Model model) {

        try {
            Usuario liderProyecto = null;
            if (lider != null) {
                Optional<Usuario> usuarioLider = usuarioRepositorio.findById(lider);
                if (usuarioLider.isEmpty()) {
                    return "redirect:/coordinador/proyectos?error=El líder seleccionado no existe";
                }
                liderProyecto = usuarioLider.get();
            }

            Proyecto nuevoProyecto = new Proyecto();
            nuevoProyecto.setNombre(nombre);
            nuevoProyecto.setDescripcion(descripcion);
            nuevoProyecto.setFechaInicio(fechaInicio);
            nuevoProyecto.setEstado(estado);
            nuevoProyecto.setLider(liderProyecto);

            nuevoProyecto = proyectoRepositorio.save(nuevoProyecto);

            if (liderProyecto != null) {
                liderProyecto.setProyectoAsignado(nuevoProyecto);
                usuarioRepositorio.save(liderProyecto);
            }

            return "redirect:/coordinador/proyectos?success";
        } catch (Exception e) {
            return "redirect:/coordinador/proyectos?error";
        }
    }

    // Add methods for editing and viewing project details
    @GetMapping("/coordinador/proyectos/editar/{id}")
    public String editarProyecto(@PathVariable Integer id, Model model) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(id);

        if (proyectoOpt.isEmpty()) {
            return "redirect:/coordinador/proyectos?error=Proyecto no encontrado";
        }

        Proyecto proyecto = proyectoOpt.get();
        model.addAttribute("proyecto", proyecto);

        // Get all project leaders
        int rolLiderId = 1;
        List<Usuario> lideres = usuarioRepositorio.findByRolId(rolLiderId);
        model.addAttribute("lideres", lideres);

        return "/coordinador/editarProyecto";
    }

    @PostMapping("/coordinador/proyectos/editar/{id}")
    public String actualizarProyecto(
            @PathVariable Integer id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Date fechaInicio,
            @RequestParam EstadoProyecto estado,
            @RequestParam(required = false) Integer lider) {

        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(id);

        if (proyectoOpt.isEmpty()) {
            return "redirect:/coordinador/proyectos?error=Proyecto no encontrado";
        }

        try {
            Proyecto proyecto = proyectoOpt.get();

            // Update project details
            proyecto.setNombre(nombre);
            proyecto.setDescripcion(descripcion);
            proyecto.setFechaInicio(fechaInicio);
            proyecto.setEstado(estado);

            // Update leader if changed
            if (lider != null) {
                Optional<Usuario> usuarioLider = usuarioRepositorio.findById(lider);
                if (usuarioLider.isPresent()) {
                    // If this project already has a leader, remove project assignment
                    if (proyecto.getLider() != null && !proyecto.getLider().getId().equals(lider)) {
                        Usuario antiguoLider = proyecto.getLider();
                        antiguoLider.setProyectoAsignado(null);
                        usuarioRepositorio.save(antiguoLider);
                    }

                    // Set new leader
                    Usuario nuevoLider = usuarioLider.get();
                    proyecto.setLider(nuevoLider);
                    nuevoLider.setProyectoAsignado(proyecto);
                    usuarioRepositorio.save(nuevoLider);
                }
            } else {
                // If removing leader assignment
                if (proyecto.getLider() != null) {
                    Usuario antiguoLider = proyecto.getLider();
                    antiguoLider.setProyectoAsignado(null);
                    usuarioRepositorio.save(antiguoLider);
                }
                proyecto.setLider(null);
            }

            proyectoRepositorio.save(proyecto);
            return "redirect:/coordinador/proyectos?success=Proyecto actualizado correctamente";
        } catch (Exception e) {
            return "redirect:/coordinador/proyectos?error=Error al actualizar el proyecto";
        }
    }

    @GetMapping("/coordinador/proyectos/detalles/{id}")
    public String detallesProyecto(@PathVariable Integer id, Model model) {
        Optional<Proyecto> proyectoOpt = proyectoRepositorio.findById(id);

        if (proyectoOpt.isEmpty()) {
            return "redirect:/coordinador/proyectos?error=Proyecto no encontrado";
        }

        Proyecto proyecto = proyectoOpt.get();
        model.addAttribute("proyecto", proyecto);

        // Get developers assigned to this project
        List<Usuario> desarrolladoresAsignados = usuarioRepositorio.findByProyectoAsignadoId(id);
        model.addAttribute("desarrolladores", desarrolladoresAsignados);

        return "/coordinador/detallesProyecto";
    }

    @PostMapping("/coordinador/actualizarPerfil")
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

    @GetMapping("/coordinador/Usuarios/reporte/{id}")
    public void generarReporte(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException, DocumentException {
        // 🔹 Configurar respuesta HTTP para descargar PDF
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Usuario_" + id + ".pdf");

        // 🔹 Buscar usuario
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isEmpty()) {
            response.getWriter().write("Usuario no encontrado");
            return;
        }
        Usuario usuario = usuarioOpt.get();

        long cantidadActividades = actividadRepositorio.countByDesarrolladorId(id);

        // 🔹 Configurar documento
        Document documento = new Document(PageSize.A4, 36, 36, 54, 36); // Márgenes para encabezado
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, response.getOutputStream());

            // Añadir encabezado a cada página
            // Pasamos la ruta del logo como parámetro
            writer.setPageEvent(new HeaderFooter("src/main/resources/static/img/logo.png"));

            documento.open();

            // 🔹 Definir estilos
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
            Font contenidoFont = new Font(Font.FontFamily.HELVETICA, 12);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            // 🔹 Título del documento
            Paragraph titulo = new Paragraph("Reporte de Usuario", tituloFont);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);

            // 🔹 Agregar foto de perfil
            if (usuario.getFotoPerfil() != null) {
                try {
                    Image imagen = Image.getInstance(usuario.getFotoPerfil());
                    imagen.scaleToFit(100, 100);
                    imagen.setAlignment(Image.ALIGN_CENTER);
                    imagen.setBorderWidth(1);
                    documento.add(imagen);
                    documento.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 6))); // Pequeño espacio
                } catch (Exception e) {
                    documento.add(new Paragraph("Error al cargar imagen de perfil", contenidoFont));
                }
            }

            // 🔹 Datos del usuario en un recuadro
            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);
            tabla.setSpacingAfter(10);

            // Establecer anchos relativos de las columnas
            float[] anchos = {1.0f, 2.0f};
            tabla.setWidths(anchos);

            // Estilo para las celdas
            PdfPCell celdaTitulo = new PdfPCell(new Phrase("Información del Usuario", subtituloFont));
            celdaTitulo.setColspan(2);
            celdaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaTitulo.setPadding(10);
            celdaTitulo.setBackgroundColor(new BaseColor(240, 240, 240));
            tabla.addCell(celdaTitulo);

            // Añadir datos a la tabla
            addRow(tabla, "Nombre y Apellido:", usuario.getNombre() + " " + usuario.getApellido(), labelFont, contenidoFont);
            addRow(tabla, "Rol:", usuario.getRol().getNombre(), labelFont, contenidoFont);
            addRow(tabla, "Desempeño:", usuario.getDesempeno().name(), labelFont, contenidoFont);
            addRow(tabla, "Estado:", usuario.getEstado() == EstadoUsuario.ACTIVO ? "HABILITADO" : "INHABILITADO", labelFont, contenidoFont);
            addRow(tabla, "Actividades Asignadas:", String.valueOf(cantidadActividades), labelFont, contenidoFont);

            documento.add(tabla);

            // 🔹 Información adicional si existe
            java.util.Date fechaActual = new java.util.Date();
            Paragraph infoAdicional = new Paragraph("Reporte generado el " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaActual),
                    new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
            infoAdicional.setAlignment(Paragraph.ALIGN_CENTER);
            infoAdicional.setSpacingBefore(30);
            documento.add(infoAdicional);

        } finally {
            documento.close();
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

    @GetMapping("/coordinador/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        String email = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/coordinador/index?error=Usuario no encontrado";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        return "/coordinador/perfil";
    }

    @GetMapping("/coordinador/biblioteca")
    public String mostrarBibliotecas() {
        return "/coordinador/biblioteca";
    }
}