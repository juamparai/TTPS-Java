package services;

import models.clases.Avistamiento;
import models.DAO.AvistamientoDAO;
import models.DAO.MascotaDAO;
import models.DAO.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AvistamientoService {

    @Autowired
    private AvistamientoDAO<Avistamiento> avistamientoDAO;

    @Autowired
    private MascotaDAO mascotaDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    public Avistamiento crearAvistamiento(Avistamiento avistamiento) {
        // Validaciones
        if (avistamiento.getMascota() == null) {
            throw new IllegalArgumentException("La mascota es requerida");
        }
        if (avistamiento.getUsuario() == null) {
            throw new IllegalArgumentException("El usuario es requerido");
        }
        if (avistamiento.getFecha() == null) {
            avistamiento.setFecha(LocalDate.now());
        }

        return avistamientoDAO.persist(avistamiento);
    }

    public Avistamiento actualizarAvistamiento(Avistamiento avistamiento) {
        Avistamiento existente = avistamientoDAO.get(avistamiento.getId());
        if (existente == null) {
            throw new IllegalArgumentException("Avistamiento no encontrado");
        }
        return avistamientoDAO.update(avistamiento);
    }

    public void eliminarAvistamiento(Long id) {
        avistamientoDAO.delete(id);
    }

    public Avistamiento obtenerPorId(Long id) {
        return avistamientoDAO.get(id);
    }

    public List<Avistamiento> obtenerTodos() {
        return avistamientoDAO.getAll("fecha");
    }

    public List<Avistamiento> obtenerPorFecha(LocalDate fecha) {
        return avistamientoDAO.getByFecha(fecha);
    }

    public List<Avistamiento> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return avistamientoDAO.getByFechaRange(fechaInicio, fechaFin);
    }

    public List<Avistamiento> obtenerPorMascota(Long mascotaId) {
        return avistamientoDAO.getByMascota(mascotaId);
    }

    public List<Avistamiento> obtenerPorUsuario(Long usuarioId) {
        return avistamientoDAO.getByUsuario(usuarioId);
    }
}

