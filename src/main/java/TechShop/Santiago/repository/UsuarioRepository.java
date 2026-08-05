/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package TechShop.Santiago.repository;

import TechShop.Santiago.domain.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    public Optional<Usuario> findByUsernameAndActivoTrue(String username);

    public List<Usuario> findByActivoTrue();

    public Optional<Usuario> findByUsername(String username);

    public Optional<Usuario> findByUsernameAndPassword(String username, String password);

    public Optional<Usuario> findByUsernameOrCorreo(String username, String correo);

    public boolean existsByUsernameOrCorreo(String username, String correo);

}