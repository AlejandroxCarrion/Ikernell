package com.ikernell.controller;

import com.ikernell.model.PreguntasUsuarios;
import com.ikernell.model.Usuario;
import com.ikernell.repository.PreguntaUsuarioRepositorio;
import com.ikernell.repository.UsuarioRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class PreguntasController {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PreguntaUsuarioRepositorio preguntasRepositorio;

    public PreguntasController(PreguntaUsuarioRepositorio preguntasRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.preguntasRepositorio = preguntasRepositorio;
    }

    @GetMapping("/preguntas")
    public String mostrarPreguntas(Model model) {
        List<PreguntasUsuarios> preguntas = preguntasRepositorio.findAll();
        model.addAttribute("preguntas", preguntas);
        return "preguntas"; 
    }

    @PostMapping("/preguntas/enviar")
    public String enviarPregunta(@RequestParam String nombreUsuario, @RequestParam String pregunta) {

        Optional<Usuario> usuarioExistente = usuarioRepositorio.findByNombre(nombreUsuario);
    
        Usuario usuario = null;
        if (usuarioExistente.isPresent()) {
            usuario = usuarioExistente.get();
        }

        PreguntasUsuarios nuevaPregunta = new PreguntasUsuarios(nombreUsuario, pregunta, null, usuario);
        preguntasRepositorio.save(nuevaPregunta);

        return "redirect:/preguntas?success=Pregunta registrada";
    }

    @PostMapping("/preguntas/responder")
    public String responderPregunta(@RequestParam Integer id, @RequestParam String respuesta) {
        Optional<PreguntasUsuarios> preguntaOptional = preguntasRepositorio.findById(id);
        if (preguntaOptional.isPresent()) {
            PreguntasUsuarios pregunta = preguntaOptional.get();
            pregunta.setRespuesta(respuesta);
            preguntasRepositorio.save(pregunta);
        }
        return "redirect:/preguntas";
    }
}
