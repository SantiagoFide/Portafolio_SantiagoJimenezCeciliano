package TechShop.Santiago.controller;

import TechShop.Santiago.domain.Usuario;
import TechShop.Santiago.service.UsuarioService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/usuario_rol")
public class UsuarioRolController {

    private final UsuarioService usuarioService;

    public UsuarioRolController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 1. Vista inicial sin usuario seleccionado
    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {

        model.addAttribute("usuario", new Usuario());

        // Listas vacías para evitar errores en Thymeleaf
        model.addAttribute(
                "rolesAsignados",
                Collections.emptySet()
        );

        model.addAttribute(
                "rolesDisponibles",
                Collections.emptyList()
        );

        return "usuario_rol/mantenimiento";
    }

    // 2. Buscar usuario y mostrar sus roles
    @GetMapping("/buscar")
    public String buscarUsuario(
            @RequestParam("username") String username,
            Model model) {

        Usuario usuario = usuarioService
                .getUsuarioPorUsername(username)
                .orElse(null);

        model.addAttribute("usuario", usuario);

        if (usuario != null) {

            // Obtener todos los roles existentes
            List<String> todosRolesNombres
                    = usuarioService.getRolesNombres();

            // Mostrar únicamente los roles que el usuario NO tiene
            List<String> rolesDisponibles
                    = todosRolesNombres.stream()
                            .filter(rolNombre
                                    -> usuario.getRoles()
                                            .stream()
                                            .noneMatch(rolAsignado
                                                    -> rolAsignado
                                                            .getRol()
                                                            .equals(rolNombre)))
                            .toList();

            model.addAttribute(
                    "rolesAsignados",
                    usuario.getRoles()
            );

            model.addAttribute(
                    "rolesDisponibles",
                    rolesDisponibles
            );

        } else {

            // Si el usuario no existe dejamos las listas vacías
            model.addAttribute(
                    "rolesAsignados",
                    Collections.emptySet()
            );

            model.addAttribute(
                    "rolesDisponibles",
                    Collections.emptyList()
            );
        }

        return "usuario_rol/mantenimiento";
    }

    // 3. Agregar rol al usuario
    @GetMapping("/agregar")
    public String agregarRol(
            @RequestParam("username") String username,
            @RequestParam("nombreRol") String nombreRol) {

        usuarioService.asignarRolPorUsername(
                username,
                nombreRol
        );

        return "redirect:/usuario_rol/buscar?username="
                + username;
    }

    // 4. Eliminar rol del usuario
    @GetMapping("/eliminar")
    public String eliminarRol(
            @RequestParam("username") String username,
            @RequestParam("idRol") Integer idRol) {

        usuarioService.eliminarRol(
                username,
                idRol
        );

        return "redirect:/usuario_rol/buscar?username="
                + username;
    }
}