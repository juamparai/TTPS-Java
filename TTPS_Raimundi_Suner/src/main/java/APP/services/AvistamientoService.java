package APP.services;

import APP.models.clases.Avistamiento;
import APP.models.dao.AvistamientoDAO;
import APP.models.dao.MascotaDAO;
import APP.models.dao.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AvistamientoService {

    @Autowired
    private AvistamientoDAO avistamientoDAO;

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

        return avistamientoDAO.save(avistamiento);
    }

    public Avistamiento actualizarAvistamiento(Avistamiento avistamiento) {
        if (!avistamientoDAO.existsById(avistamiento.getId())) {
            throw new IllegalArgumentException("Avistamiento no encontrado");
        }
        return avistamientoDAO.save(avistamiento);
    }

    public void eliminarAvistamiento(Long id) {
        avistamientoDAO.deleteById(id);
    }

    public Avistamiento obtenerPorId(Long id) {
        return avistamientoDAO.findById(id).orElse(null);
    }

    public List<Avistamiento> obtenerTodos() {
        return avistamientoDAO.findAll();
    }

    public List<Avistamiento> obtenerPorFecha(LocalDate fecha) {
        return avistamientoDAO.findByFecha(fecha);
    }

    public List<Avistamiento> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return avistamientoDAO.findByFechaRange(fechaInicio, fechaFin);
    }

    public List<Avistamiento> obtenerPorMascota(Long mascotaId) {
        return avistamientoDAO.findByMascota(mascotaId);
    }

    public List<Avistamiento> obtenerPorUsuario(Long usuarioId) {
        return avistamientoDAO.findByUsuario(usuarioId);
    }
}
