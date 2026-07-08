// Funcion para hacer un preview de una imagen
function mostrarImagen(input) {
    if (input.files && input.files[0]) {

        const imagen = input.files[0];
        const maximo = 512 * 1024; // 512 KB

        if (imagen.size <= maximo) {
            var lector = new FileReader();

            lector.onload = function (e) {
                $('#blah').attr('src', e.target.result).height(200);
            };

            lector.readAsDataURL(input.files[0]);

        } else {
            alert("La imagen seleccionada es muy grande... no debe superar los 512 KB!");
        }
    }
}

// Para insertar informacion en el modal segun el registro
document.addEventListener('DOMContentLoaded', function () {
    const confirmModal = document.getElementById('confirmModal');

    confirmModal.addEventListener('show.bs.modal', function (event) {
        const button = event.relatedTarget;

        document.getElementById('modalId').value = button.getAttribute('data-bs-id');
        document.getElementById('modalDescripcion').textContent = button.getAttribute('data-bs-description');
    });
});

// Para quitar toast
setTimeout(() => {
    document.querySelectorAll('.toast').forEach(t => t.classList.remove('show'));
}, 4000);