package com.example.universidadeESN3.repository;


import com.example.delivery.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    List<Restaurante> findByNomeStartingWithIgnoreCase(String nome);
    List<Restaurante> findByAtivoTrue();
    List<Restaurante> findByEspecialidadeContainingIgnoreCase(String especialidade);
}
