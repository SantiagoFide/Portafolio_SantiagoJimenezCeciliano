/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package TechShop.Santiago.repository;

import TechShop.Santiago.domain.Constante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostanteRepository extends JpaRepository<Constante, Integer> {

    public Optional<Constante> findByAtributo(String atributo);
}
