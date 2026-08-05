/**
 * Agrega un producto al carrito con cantidad inicial de 1.
 *
 * @param {HTMLFormElement} formulario Formulario que contiene el ID del producto.
 */
function addCart(formulario) {
    const idProducto = $(formulario)
            .find('input[name="idProducto"]')
            .val();

    const ruta = $(formulario).attr('action') || '/carrito/agregar';

    const csrfToken = $("meta[name='_csrf']").attr("content");
    const csrfHeader = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: ruta,
        type: 'POST',
        data: {
            idProducto: idProducto
        },

        beforeSend: function (xhr) {
            if (csrfHeader && csrfToken) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        },

        success: function (response) {
            $("#resultBlock").html(response);
            console.log("Producto agregado al carrito.");
        },

        error: function (xhr) {
            const mensaje = xhr.responseText || "Error en la conexión.";

            alert(
                    "Error al agregar el producto: "
                    + mensaje
            );
        }
    });
}

/**
 * Muestra una vista previa de la imagen seleccionada.
 *
 * @param {HTMLInputElement} input Campo de selección del archivo.
 */
function mostrarImagen(input) {
    if (!input.files || !input.files[0]) {
        return;
    }

    const imagen = input.files[0];
    const maximo = 512 * 1024;

    if (imagen.size > maximo) {
        alert(
                "La imagen seleccionada es muy grande. "
                + "No debe superar los 512 KB."
        );

        input.value = "";
        return;
    }

    const lector = new FileReader();

    lector.onload = function (evento) {
        $("#blah")
                .attr("src", evento.target.result)
                .height(200);
    };

    lector.readAsDataURL(imagen);
}

document.addEventListener("DOMContentLoaded", function () {
    const confirmModal = document.getElementById("confirmModal");

    if (confirmModal) {
        confirmModal.addEventListener(
                "show.bs.modal",
                function (event) {

                    const button = event.relatedTarget;

                    if (!button) {
                        return;
                    }

                    const modalId = document.getElementById("modalId");
                    const modalDescripcion = document.getElementById(
                            "modalDescripcion"
                    );

                    if (modalId) {
                        modalId.value = button.getAttribute("data-bs-id");
                    }

                    if (modalDescripcion) {
                        modalDescripcion.textContent
                                = button.getAttribute(
                                        "data-bs-descripcion"
                                );
                    }
                }
        );
    }

    setTimeout(function () {
        document.querySelectorAll(".toast").forEach(function (toastElement) {
            toastElement.classList.remove("show");
        });
    }, 4000);
});