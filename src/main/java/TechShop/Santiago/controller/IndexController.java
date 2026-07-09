/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TechShop.Santiago.controller;

import TechShop.Santiago.service.CategoriaService;
import TechShop.Santiago.service.ProductoService;
import java.util.Collections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IndexController {

    // Las últimas versiones de Spring recomiendan utilizar final y constructor
    // en lugar de @Autowired
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    // Spring inyecta automáticamente los servicios
    public IndexController(ProductoService productoService,
                           CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/")
    public String cargarPaginaInicio(Model model) {

        var lista = productoService.getProductos(true);
        model.addAttribute("productos", lista);

        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);

        return "/index";
    }

    @GetMapping("/consultas/{idCategoria}")
    public String listado(@PathVariable("idCategoria") Integer idCategoria,
                          Model model) {

        model.addAttribute("idCategoriaActual", idCategoria);

        var categoriaOptional = categoriaService.getCategoria(idCategoria);

        if (categoriaOptional.isEmpty()) {
            // Puede ser que no exista la categoría buscada
            model.addAttribute("productos", Collections.emptyList());
        } else {
            var categoria = categoriaOptional.get();
            var productos = categoria.getProductos();
            model.addAttribute("productos", productos);
        }

        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);

        return "/index";
    }
}