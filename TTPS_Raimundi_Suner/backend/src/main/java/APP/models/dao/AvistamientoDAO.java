package APP.models.dao;

import APP.models.clases.Avistamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvistamientoDAO extends JpaRepository<Avistamiento, Long> {
    List<Avistamiento> findByFecha(LocalDate fecha);

    @Query("SELECT a FROM Avistamiento a WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Avistamiento> findByFechaRange(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT a FROM Avistamiento a WHERE a.mascota.id = :mascotaId")
    List<Avistamiento> findByMascota(@Param("mascotaId") Long mascotaId);

    @Query("SELECT a FROM Avistamiento a WHERE a.usuario.id = :usuarioId")
    List<Avistamiento> findByUsuario(@Param("usuarioId") Long usuarioId);
}
