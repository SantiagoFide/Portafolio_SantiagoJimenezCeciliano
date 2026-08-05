package TechShop.Santiago.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    // Producto agregado al carrito
    private Producto producto;

    // Cantidad seleccionada
    private int cantidad;

    // Precio del producto al momento de agregarlo al carrito
    private BigDecimal precioHistorico;

    // Calcula el subtotal del producto
    public BigDecimal getSubTotal() {
        return precioHistorico.multiply(BigDecimal.valueOf(cantidad));
    }
}