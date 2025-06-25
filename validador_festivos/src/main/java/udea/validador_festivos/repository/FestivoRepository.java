package udea.validador_festivos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import udea.validador_festivos.entity.Festivo;

public interface FestivoRepository extends JpaRepository<Festivo, Long> {

        List<Festivo> findByDiaAndMes(int dia, int mes);
        
}
