/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package TechShop.Santiago.controller;

import TechShop.Santiago.domain.Usuario;
import TechShop.Santiago.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public UsuarioController(
            UsuarioService usuarioService,
            MessageSource messageSource) {

        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String inicio(Model model) {

        var usuarios = usuarioService.getUsuarios(false);

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());

        return "/usuario/listado";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid Usuario usuario,
            BindingResult bindingResult,
            @RequestParam(
                    name = "imagenFile",
                    required = false
            ) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    messageSource.getMessage(
                            "usuario.error04",
                            null,
                            Locale.getDefault()
                    )
            );

            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }

            return "redirect:/usuario/modificar/"
                    + usuario.getIdUsuario();
        }

        try {

            usuarioService.save(
                    usuario,
                    imagenFile,
                    true
            );

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    messageSource.getMessage(
                            "mensaje.actualizado",
                            null,
                            Locale.getDefault()
                    )
            );

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }

            return "redirect:/usuario/modificar/"
                    + usuario.getIdUsuario();

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }

            return "redirect:/usuario/modificar/"
                    + usuario.getIdUsuario();
        }

        return "redirect:/usuario/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam Integer idUsuario,
            RedirectAttributes redirectAttributes) {

        try {

            usuarioService.delete(idUsuario);

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    messageSource.getMessage(
                            "mensaje.eliminado",
                            null,
                            Locale.getDefault()
                    )
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    obtenerMensaje(
                            "usuario.error01",
                            "El usuario no existe."
                    )
            );

        } catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    obtenerMensaje(
                            "usuario.error02",
                            "No se puede eliminar el usuario porque tiene datos asociados."
                    )
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    obtenerMensaje(
                            "usuario.error03",
                            "Ocurrió un error al eliminar el usuario."
                    )
            );
        }

        return "redirect:/usuario/listado";
    }

    @GetMapping("/modificar/{idUsuario}")
    public String modificar(
            @PathVariable Integer idUsuario,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt
                = usuarioService.getUsuario(idUsuario);

        if (usuarioOpt.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no fue encontrado."
            );

            return "redirect:/usuario/listado";
        }

        Usuario usuario = usuarioOpt.get();

        /*
         * Se deja vacía para que la contraseña encriptada
         * no aparezca en el formulario.
         */
        usuario.setPassword("");

        model.addAttribute("usuario", usuario);

        return "/usuario/modifica";
    }

    private String obtenerMensaje(
            String clave,
            String mensajePredeterminado) {

        try {

            return messageSource.getMessage(
                    clave,
                    null,
                    Locale.getDefault()
            );

        } catch (NoSuchMessageException e) {

            return mensajePredeterminado;
        }
    }
}