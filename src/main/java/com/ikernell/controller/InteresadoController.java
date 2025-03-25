package com.ikernell.controller;

import com.ikernell.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;


@Controller
public class InteresadoController {
    @Autowired
    private EmailService emailService;


    @GetMapping({"/"})
    public String mostrarIndex() {
        return "inicio";
    }

    @GetMapping({"/noticias"})
    public String noticias() {
        return "noticias";
    }

    @GetMapping({"/soporte"})
    public String soporte() {
        return "soporte";
    }

    @GetMapping({"/servicios"})
    public String sercivios() {
        return "servicios";
    }

     @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String redireccionarSegunRol(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Coordinador_proyecto"))) {
            return "redirect:/coordinador/index";
        } else if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Lider_proyecto"))) {
            return "redirect:/lider/dashboard";
        } else if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Desarrollador"))) {
            return "redirect:/desarrollador/index";
        }

        return "redirect:/";
    }

    @PostMapping("/enviar-pregunta")
    public String enviarPregunta(
            @RequestParam("nombre") String nombre,
            @RequestParam("email") String email,
            @RequestParam("pregunta") String pregunta,
            RedirectAttributes redirectAttributes) {

        try {
            // Usar el servicio de email para enviar la pregunta
            emailService.enviarCorreoPregunta(nombre, email, pregunta);

            // Agregar mensaje de éxito
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "¡Gracias por tu pregunta! Te responderemos a la brevedad posible.");

        } catch (IOException e) {
            // Manejar el error y mostrar mensaje al usuario
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Lo sentimos, hubo un problema al enviar tu pregunta. Por favor, inténtalo de nuevo más tarde.");
        }

        // Redirigir de vuelta a la página de soporte
        return "redirect:/soporte";
    }

}
