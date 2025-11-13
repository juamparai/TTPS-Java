package APP.services;

import APP.models.clases.Mascota;
import APP.models.clases.EstadoMascota;
import APP.models.dao.MascotaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MascotaService {

    @Autowired
    private MascotaDAO mascotaDAO;

    public Mascota crearMascota(Mascota mascota) {
        // Validaciones básicas
        if (mascota.getNombre() == null || mascota.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la mascota es requerido");
        }
        return mascotaDAO.save(mascota);
    }

    public Mascota actualizarMascota(Mascota mascota) {
        if (!mascotaDAO.existsById(mascota.getId())) {
            throw new IllegalArgumentException("Mascota no encontrada");
        }
        return mascotaDAO.save(mascota);
    }

    public void eliminarMascota(Long id) {
        mascotaDAO.deleteById(id);
    }

    public Mascota obtenerPorId(Long id) {
        return mascotaDAO.findById(id).orElse(null);
    }

    public List<Mascota> obtenerTodas() {
        return mascotaDAO.findAll();
    }

    public List<Mascota> obtenerPorNombre(String nombre) {
        return mascotaDAO.findByNombre(nombre);
    }

    public List<Mascota> obtenerPorTipo(String tipo) {
        return mascotaDAO.findByTipo(tipo);
    }

    public List<Mascota> obtenerPorRaza(String raza) {
        return mascotaDAO.findByRaza(raza);
    }

    public List<Mascota> obtenerPorColor(String color) {
        return mascotaDAO.findByColor(color);
    }

    public List<Mascota> obtenerPorEstado(String estado) {
        return mascotaDAO.findByEstado(estado);
    }

    public List<Mascota> obtenerPorUsuario(Long usuarioId) {
        return mascotaDAO.findByUsuario(usuarioId);
    }

    public List<Mascota> obtenerMascotasPerdidas() {
        // Retorna mascotas con estado PERDIDA_PROPIA o PERDIDA_AJENA
        List<Mascota> perdidas = mascotaDAO.findByEstado(EstadoMascota.PERDIDA_PROPIA.name());
        List<Mascota> perdidasAjenas = mascotaDAO.findByEstado(EstadoMascota.PERDIDA_AJENA.name());
        if (perdidas != null && perdidasAjenas != null) {
            perdidas.addAll(perdidasAjenas);
        }
        return perdidas;
    }

    public void cambiarEstado(Long mascotaId, EstadoMascota nuevoEstado) {
        Mascota mascota = mascotaDAO.findById(mascotaId).orElse(null);
        if (mascota != null) {
            mascota.setEstadoMascota(nuevoEstado);
            mascotaDAO.save(mascota);
        }
    }
}
