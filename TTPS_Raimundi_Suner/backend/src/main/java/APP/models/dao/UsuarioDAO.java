package APP.models.dao;

import APP.models.clases.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByNombreAndApellido(String nombre, String apellido);

    List<Usuario> findByBarrio(String barrio);
    List<Usuario> findByCiudad(String ciudad);

    @Query("SELECT u FROM Usuario u ORDER BY u.puntos DESC")
    List<Usuario> findAllOrderByPuntosDesc();
}
