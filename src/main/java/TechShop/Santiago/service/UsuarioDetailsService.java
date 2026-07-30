/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package TechShop.Santiago.service;

import TechShop.Santiago.domain.Usuario;
import TechShop.Santiago.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userDetailsService")
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final HttpSession session;

    public UsuarioDetailsService(
            UsuarioRepository usuarioRepository,
            HttpSession session) {

        this.usuarioRepository = usuarioRepository;
        this.session = session;
    }

    /*
     * Busca en la base de datos un usuario activo utilizando
     * el username escrito en el formulario de inicio de sesión.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado: " + username));

        // Guarda información del usuario dentro de la sesión
        session.setAttribute("imagenUsuario", usuario.getRutaImagen());
        session.setAttribute("usuario", usuario);

        // Convierte los roles de la base de datos a roles de Spring Security
        List<SimpleGrantedAuthority> roles = usuario.getRoles()
                .stream()
                .map(rol -> new SimpleGrantedAuthority(
                "ROLE_" + rol.getRol()))
                .toList();

        // Retorna el usuario que utilizará Spring Security
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                roles
        );
    }
}