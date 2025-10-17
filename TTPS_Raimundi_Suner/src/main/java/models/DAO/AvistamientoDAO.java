package models.DAO;

import java.time.LocalDate;
import java.util.List;

public interface AvistamientoDAO<T> extends GenericDAO<T>{
    public List<T> getByFecha(LocalDate fecha);
    public List<T> getByFechaRange(LocalDate fechaInicio, LocalDate fechaFin);
    public List<T> getByMascota(Long mascotaId);
    public List<T> getByUsuario(Long usuarioId);
}

