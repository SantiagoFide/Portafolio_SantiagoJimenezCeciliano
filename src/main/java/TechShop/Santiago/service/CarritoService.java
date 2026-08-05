package TechShop.Santiago.service;

import TechShop.Santiago.domain.EstadoFactura;
import TechShop.Santiago.domain.Factura;
import TechShop.Santiago.domain.Item;
import TechShop.Santiago.domain.Producto;
import TechShop.Santiago.domain.Usuario;
import TechShop.Santiago.domain.Venta;
import TechShop.Santiago.repository.FacturaRepository;
import TechShop.Santiago.repository.ProductoRepository;
import TechShop.Santiago.repository.VentaRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private static final String ATTRIBUTE_CARRITO = "carrito";

    private final ProductoRepository productoRepository;
    private final FacturaRepository facturaRepository;
    private final VentaRepository ventaRepository;

    public CarritoService(
            ProductoRepository productoRepository,
            FacturaRepository facturaRepository,
            VentaRepository ventaRepository) {

        this.productoRepository = productoRepository;
        this.facturaRepository = facturaRepository;
        this.ventaRepository = ventaRepository;
    }

    public List<Item> obtenerCarrito(HttpSession session) {

        @SuppressWarnings("unchecked")
        List<Item> carrito
                = (List<Item>) session.getAttribute(ATTRIBUTE_CARRITO);

        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(ATTRIBUTE_CARRITO, carrito);
        }

        return carrito;
    }

    public void guardarCarrito(
            HttpSession session,
            List<Item> carrito) {

        session.setAttribute(ATTRIBUTE_CARRITO, carrito);
    }

    public void agregarProducto(
            List<Item> carrito,
            Integer idProducto) {

        Producto producto = productoRepository
                .findById(idProducto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Producto no encontrado."
                        )
                );

        Optional<Item> itemExistente = carrito.stream()
                .filter(item ->
                        item.getProducto()
                                .getIdProducto()
                                .equals(idProducto)
                )
                .findFirst();

        int cantidad = 1;

        if (itemExistente.isPresent()) {

            Item item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;

            if (nuevaCantidad > producto.getExistencias()) {
                throw new IllegalStateException(
                        "Stock insuficiente para agregar "
                        + cantidad
                        + " unidad."
                );
            }

            item.setCantidad(nuevaCantidad);

        } else {

            if (cantidad > producto.getExistencias()) {
                throw new IllegalStateException(
                        "Stock insuficiente para agregar "
                        + cantidad
                        + " unidad."
                );
            }

            Item nuevoItem = new Item();
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioHistorico(producto.getPrecio());

            carrito.add(nuevoItem);
        }
    }

    public Item buscarItem(
            List<Item> carrito,
            Integer idProducto) {

        if (carrito == null) {
            return null;
        }

        return carrito.stream()
                .filter(item ->
                        item.getProducto()
                                .getIdProducto()
                                .equals(idProducto)
                )
                .findFirst()
                .orElse(null);
    }

    public void eliminarItem(
            List<Item> carrito,
            Integer idProducto) {

        if (carrito == null) {
            return;
        }

        carrito.removeIf(item ->
                item.getProducto()
                        .getIdProducto()
                        .equals(idProducto)
        );
    }

    public void actualizarCantidad(
            List<Item> carrito,
            Integer idProducto,
            int nuevaCantidad) {

        if (nuevaCantidad <= 0) {
            eliminarItem(carrito, idProducto);
            return;
        }

        Item item = buscarItem(carrito, idProducto);

        if (item == null) {
            throw new IllegalArgumentException(
                    "El producto no está en el carrito."
            );
        }

        Producto producto = productoRepository
                .findById(idProducto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Producto no encontrado."
                        )
                );

        if (nuevaCantidad > producto.getExistencias()) {
            throw new IllegalStateException(
                    "No hay suficiente stock disponible."
            );
        }

        item.setProducto(producto);
        item.setCantidad(nuevaCantidad);
    }

    public int contarUnidades(List<Item> carrito) {

        if (carrito == null || carrito.isEmpty()) {
            return 0;
        }

        return carrito.stream()
                .mapToInt(Item::getCantidad)
                .sum();
    }

    public BigDecimal calcularTotal(List<Item> carrito) {

        if (carrito == null || carrito.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return carrito.stream()
                .map(Item::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void limpiarCarrito(HttpSession session) {

        List<Item> carrito = obtenerCarrito(session);
        carrito.clear();

        guardarCarrito(session, carrito);
    }

    @Transactional
    public Factura procesarCompra(
            List<Item> carrito,
            Usuario usuario) {

        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalStateException(
                    "El carrito está vacío para procesar la compra."
            );
        }

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio para procesar la compra."
            );
        }

        Factura factura = new Factura();
        factura.setUsuario(usuario);
        factura.setFecha(LocalDateTime.now());
        factura.setTotal(calcularTotal(carrito));
        factura.setEstado(EstadoFactura.Pagada);
        factura.setFechaCreacion(LocalDateTime.now());
        factura.setFechaModificacion(LocalDateTime.now());

        factura = facturaRepository.save(factura);

        List<Venta> ventas = new ArrayList<>();

        for (Item item : carrito) {

            Integer idProducto
                    = item.getProducto().getIdProducto();

            Producto producto = productoRepository
                    .findById(idProducto)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Producto no encontrado: "
                                    + idProducto
                            )
                    );

            if (item.getCantidad() > producto.getExistencias()) {
                throw new IllegalStateException(
                        "Fallo en la compra: el producto "
                        + producto.getDescripcion()
                        + " no tiene suficiente stock."
                );
            }

            Venta venta = new Venta();
            venta.setFactura(factura);
            venta.setProducto(producto);
            venta.setPrecioHistorico(item.getPrecioHistorico());
            venta.setCantidad(item.getCantidad());
            venta.setFechaCreacion(LocalDateTime.now());
            venta.setFechaModificacion(LocalDateTime.now());

            venta = ventaRepository.save(venta);
            ventas.add(venta);

            producto.setExistencias(
                    producto.getExistencias()
                    - item.getCantidad()
            );

            productoRepository.save(producto);
        }

        factura.setVentas(ventas);

        return factura;
    }
}