package APP.models.dao;

import APP.models.clases.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbicacionDAO extends JpaRepository<Ubicacion, Long> {
    List<Ubicacion> findByBarrio(String barrio);
}
