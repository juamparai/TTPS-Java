package APP.models.dao;

import APP.models.clases.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaDAO extends JpaRepository<Mascota, Long> {
    List<Mascota> findByNombre(String nombre);
    List<Mascota> findByTipo(String tipo);
    List<Mascota> findByRaza(String raza);
    List<Mascota> findByColor(String color);

    @Query("SELECT m FROM Mascota m WHERE m.estadoMascota = :estado")
    List<Mascota> findByEstado(@Param("estado") String estado);

    @Query("SELECT m FROM Mascota m WHERE m.usuario.id = :usuarioId")
    List<Mascota> findByUsuario(@Param("usuarioId") Long usuarioId);
}
