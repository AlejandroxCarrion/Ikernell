package com.ikernell.service;

import com.ikernell.model.Usuario;
import com.ikernell.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));

        if (usuario.getRol() == null) {
            throw new RuntimeException("El usuario no tiene un rol asignado.");
        }

        int rolId = usuario.getRol().getId();
        String rolNombre = "ROLE_USER";

        if (rolId == 1) {
            rolNombre = "ROLE_LIDER";
        } else if (rolId == 2) {
            rolNombre = "ROLE_COORDINADOR";
        } else if (rolId == 3) {
            rolNombre = "ROLE_DESARROLLADOR";
        }

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getContrasena(),
                Collections.singletonList(new SimpleGrantedAuthority(rolNombre))
        );
    }
}
