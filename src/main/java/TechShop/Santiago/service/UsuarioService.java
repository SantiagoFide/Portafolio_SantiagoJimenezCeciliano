package TechShop.Santiago.service;

import TechShop.Santiago.domain.Rol;
import TechShop.Santiago.domain.Usuario;
import TechShop.Santiago.repository.RolRepository;
import TechShop.Santiago.repository.UsuarioRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            FirebaseStorageService firebaseStorageService,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {

        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }

        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameYPassword(
            String username,
            String password) {

        return usuarioRepository.findByUsernameAndPassword(
                username,
                password
        );
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameOCorreo(
            String username,
            String correo) {

        return usuarioRepository.findByUsernameOrCorreo(
                username,
                correo
        );
    }

    @Transactional(readOnly = true)
    public boolean existeUsuarioPorUsernameOCorreo(
            String username,
            String correo) {

        return usuarioRepository.existsByUsernameOrCorreo(
                username,
                correo
        );
    }

    @Transactional
    public void save(
            Usuario usuario,
            MultipartFile imagenFile,
            boolean encriptaClave) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no puede ser nulo."
            );
        }

        validarCorreoDuplicado(usuario);

        boolean asignarRol = usuario.getIdUsuario() == null;

        if (asignarRol) {
            prepararNuevoUsuario(usuario, encriptaClave);
        } else {
            prepararUsuarioExistente(usuario, encriptaClave);
        }

        usuario = usuarioRepository.save(usuario);

        guardarImagen(usuario, imagenFile);

        if (asignarRol) {
            asignarRolPorUsername(
                    usuario.getUsername(),
                    "USER"
            );
        }
    }

    private void validarCorreoDuplicado(Usuario usuario) {

        Long idUsuarioActual = usuario.getIdUsuario();

        Optional<Usuario> usuarioDuplicado
                = usuarioRepository.findByUsernameOrCorreo(
                        null,
                        usuario.getCorreo()
                );

        if (usuarioDuplicado.isPresent()) {

            Usuario usuarioEncontrado = usuarioDuplicado.get();

            boolean usuarioNuevo = idUsuarioActual == null;

            boolean usuarioDiferente
                    = !usuarioEncontrado
                            .getIdUsuario()
                            .equals(idUsuarioActual);

            if (usuarioNuevo || usuarioDiferente) {

                throw new DataIntegrityViolationException(
                        "El correo ya está en uso por otro usuario."
                );
            }
        }
    }

    private void prepararNuevoUsuario(
            Usuario usuario,
            boolean encriptaClave) {

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria para nuevos usuarios."
            );
        }

        if (encriptaClave) {

            usuario.setPassword(
                    passwordEncoder.encode(
                            usuario.getPassword()
                    )
            );
        }
    }

    private void prepararUsuarioExistente(
            Usuario usuario,
            boolean encriptaClave) {

        Usuario usuarioExistente = usuarioRepository
                .findById(
                        usuario.getIdUsuario().intValue()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario a modificar no encontrado."
                        )
                );

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            usuario.setPassword(
                    usuarioExistente.getPassword()
            );

        } else if (encriptaClave) {

            usuario.setPassword(
                    passwordEncoder.encode(
                            usuario.getPassword()
                    )
            );
        }

        if (usuario.getRutaImagen() == null
                || usuario.getRutaImagen().isBlank()) {

            usuario.setRutaImagen(
                    usuarioExistente.getRutaImagen()
            );
        }
    }

    private void guardarImagen(
            Usuario usuario,
            MultipartFile imagenFile) {

        if (imagenFile == null || imagenFile.isEmpty()) {
            return;
        }

        try {

            String rutaImagen
                    = firebaseStorageService.uploadImage(
                            imagenFile,
                            "usuario",
                            usuario.getIdUsuario().intValue()
                    );

            usuario.setRutaImagen(rutaImagen);

            usuarioRepository.save(usuario);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo guardar la imagen del usuario.",
                    e
            );
        }
    }

    @Transactional
    public void delete(Integer idUsuario) {

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "El ID del usuario es obligatorio."
            );
        }

        if (!usuarioRepository.existsById(idUsuario)) {

            throw new IllegalArgumentException(
                    "El usuario con ID "
                    + idUsuario
                    + " no existe."
            );
        }

        try {

            usuarioRepository.deleteById(idUsuario);

        } catch (DataIntegrityViolationException e) {

            throw new IllegalStateException(
                    "No se puede eliminar el usuario. "
                    + "Tiene datos asociados.",
                    e
            );
        }
    }

    @Transactional
    public Usuario asignarRolPorUsername(
            String username,
            String rolStr) {

        Usuario usuario = usuarioRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado: "
                                + username
                        )
                );

        Rol rol = rolRepository
                .findByRol(rolStr)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Rol no encontrado: "
                                + rolStr
                        )
                );

        if (!usuario.getRoles().contains(rol)) {

            usuario.getRoles().add(rol);
        }

        return usuarioRepository.save(usuario);
    }

    // =====================================================
    // GESTIÓN DE ROLES
    // =====================================================

    @Transactional(readOnly = true)
    public List<String> getRolesNombres() {

        return rolRepository
                .findAll()
                .stream()
                .map(Rol::getRol)
                .toList();
    }

    @Transactional
    public Usuario eliminarRol(
            String username,
            Integer idRol) {

        Optional<Usuario> usuarioOpt
                = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {

            throw new RuntimeException(
                    "Usuario no encontrado: "
                    + username
            );
        }

        Usuario usuario = usuarioOpt.get();

        usuario.getRoles().removeIf(
                rol -> rol.getIdRol().equals(idRol)
        );

        return usuarioRepository.save(usuario);
    }
}