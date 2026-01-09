package APP.models.dao;

import APP.models.clases.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolDAO extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
