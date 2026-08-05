package TechShop.Santiago.controller;

import TechShop.Santiago.domain.Factura;
import TechShop.Santiago.domain.Item;
import TechShop.Santiago.domain.Usuario;
import TechShop.Santiago.service.CarritoService;
import TechShop.Santiago.service.FacturaService;
import TechShop.Santiago.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CarritoController {

    private final CarritoService carritoService;
    private final UsuarioService usuarioService;
    private final FacturaService facturaService;

    public CarritoController(
            CarritoService carritoService,
            UsuarioService usuarioService,
            FacturaService facturaService) {

        this.carritoService = carritoService;
        this.usuarioService = usuarioService;
        this.facturaService = facturaService;
    }

    @GetMapping("/carrito/listado")
    public String listado(
            HttpSession session,
            Model model) {

        List<Item> carrito = carritoService.obtenerCarrito(session);

        model.addAttribute("carritoItems", carrito);
        model.addAttribute(
                "totalCarrito",
                carritoService.calcularTotal(carrito)
        );

        return "/carrito/listado";
    }

    @PostMapping("/carrito/agregar")
    public ModelAndView agregar(
            @RequestParam("idProducto") Integer idProducto,
            HttpSession session,
            Model model) {

        try {

            List<Item> carrito
                    = carritoService.obtenerCarrito(session);

            carritoService.agregarProducto(
                    carrito,
                    idProducto
            );

            carritoService.guardarCarrito(
                    session,
                    carrito
            );

            model.addAttribute(
                    "carritoTotal",
                    carritoService.calcularTotal(carrito)
            );

            model.addAttribute(
                    "listaItems",
                    carrito
            );

            return new ModelAndView(
                    "/carrito/fragmentos :: verCarrito",
                    model.asMap()
            );

        } catch (RuntimeException e) {

            model.addAttribute(
                    "errorMensaje",
                    e.getMessage()
            );

            return new ModelAndView(
                    "/errores/fragmentos :: errorMensaje",
                    model.asMap()
            );
        }
    }

    @PostMapping("/carrito/eliminar/{idProducto}")
    public String eliminarItem(
            @PathVariable("idProducto") Integer idProducto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        List<Item> carrito
                = carritoService.obtenerCarrito(session);

        carritoService.eliminarItem(
                carrito,
                idProducto
        );

        carritoService.guardarCarrito(
                session,
                carrito
        );

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Producto eliminado del carrito."
        );

        return "redirect:/carrito/listado";
    }

    @GetMapping("/carrito/modificar/{idProducto}")
    public String modificar(
            @PathVariable("idProducto") Integer idProducto,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        List<Item> carrito
                = carritoService.obtenerCarrito(session);

        Item item = carritoService.buscarItem(
                carrito,
                idProducto
        );

        if (item == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El producto no se encuentra en el carrito."
            );

            return "redirect:/carrito/listado";
        }

        model.addAttribute("item", item);

        return "/carrito/modifica";
    }

    @PostMapping("/carrito/actualizar")
    public String actualizarCantidad(
            @RequestParam("producto.idProducto") Integer idProducto,
            @RequestParam("cantidad") int nuevaCantidad,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {

            List<Item> carrito
                    = carritoService.obtenerCarrito(session);

            carritoService.actualizarCantidad(
                    carrito,
                    idProducto,
                    nuevaCantidad
            );

            carritoService.guardarCarrito(
                    session,
                    carrito
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Cantidad actualizada correctamente."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/carrito/listado";
    }

    @GetMapping("/facturar/carrito")
    public String facturarCarrito(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {

            List<Item> carrito
                    = carritoService.obtenerCarrito(session);

            Authentication authentication
                    = SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getName())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Debe iniciar sesión para realizar la compra."
                );

                return "redirect:/login";
            }

            String username = authentication.getName();

            Usuario usuario = usuarioService
                    .getUsuarioPorUsername(username)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Usuario autenticado no encontrado."
                            )
                    );

            Factura factura
                    = carritoService.procesarCompra(
                            carrito,
                            usuario
                    );

            carritoService.limpiarCarrito(session);

            redirectAttributes.addFlashAttribute(
                    "idFactura",
                    factura.getIdFactura()
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Compra procesada con éxito. Factura Nro: "
                    + factura.getIdFactura()
            );

            return "redirect:/carrito/verFactura";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Error al procesar la compra: "
                    + e.getMessage()
            );

            return "redirect:/carrito/listado";
        }
    }

    @GetMapping("/carrito/verFactura")
    public String verFactura(
            @ModelAttribute("idFactura") Integer idFactura,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (idFactura == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró la factura."
            );

            return "redirect:/index";
        }

        try {

            Factura factura
                    = facturaService.getFacturaConVentas(idFactura);

            model.addAttribute(
                    "factura",
                    factura
            );

            return "/carrito/verFactura";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/index";
        }
    }
}