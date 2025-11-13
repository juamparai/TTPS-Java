package APP.models.dao;

import APP.models.clases.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PublicacionDAO extends JpaRepository<Publicacion, Long> {
    @Query("SELECT p FROM Publicacion p WHERE p.usuario.id = :usuarioId")
    List<Publicacion> findByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT p FROM Publicacion p WHERE p.mascota.id = :mascotaId")
    List<Publicacion> findByMascota(@Param("mascotaId") Long mascotaId);

    List<Publicacion> findByEstadoPublicacion(String estado);
    List<Publicacion> findByFecha(LocalDate fecha);

    @Query("SELECT p FROM Publicacion p WHERE p.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Publicacion> findByFechaRange(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT p FROM Publicacion p WHERE p.estadoPublicacion = 'ACTIVA'")
    List<Publicacion> findPublicacionesActivas();
}
