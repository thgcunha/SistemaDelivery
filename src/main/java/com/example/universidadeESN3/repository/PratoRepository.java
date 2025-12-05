package com.example.universidadeESN3.repository;



import com.example.universidadeESN3.entity.Prato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PratoRepository extends JpaRepository<Prato, Long> {
    List<Prato> findByNomeStartingWithIgnoreCase(String nome);
    List<Prato> findByAtivoTrue();
    List<Prato> findByDisponivelTrue();
    List<Prato> findByRestauranteId(Long restauranteId);
    List<Prato> findByCategoriaContainingIgnoreCase(String categoria);
}
