package services;

import models.clases.Mascota;
import models.clases.EstadoMascota;
import models.DAO.MascotaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MascotaService {

    @Autowired
    private MascotaDAO<Mascota> mascotaDAO;

    public Mascota crearMascota(Mascota mascota) {
        // Validaciones básicas
        if (mascota.getNombre() == null || mascota.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la mascota es requerido");
        }
        return mascotaDAO.persist(mascota);
    }

    public Mascota actualizarMascota(Mascota mascota) {
        Mascota existente = mascotaDAO.get(mascota.getId());
        if (existente == null) {
            throw new IllegalArgumentException("Mascota no encontrada");
        }
        return mascotaDAO.update(mascota);
    }

    public void eliminarMascota(Long id) {
        mascotaDAO.delete(id);
    }

    public Mascota obtenerPorId(Long id) {
        return mascotaDAO.get(id);
    }

    public List<Mascota> obtenerTodas() {
        return mascotaDAO.getAll("id");
    }

    public List<Mascota> obtenerPorNombre(String nombre) {
        return mascotaDAO.getByNombre(nombre);
    }

    public List<Mascota> obtenerPorTipo(String tipo) {
        return mascotaDAO.getByTipo(tipo);
    }

    public List<Mascota> obtenerPorRaza(String raza) {
        return mascotaDAO.getByRaza(raza);
    }

    public List<Mascota> obtenerPorColor(String color) {
        return mascotaDAO.getByColor(color);
    }

    public List<Mascota> obtenerPorEstado(String estado) {
        return mascotaDAO.getByEstado(estado);
    }

    public List<Mascota> obtenerPorUsuario(Long usuarioId) {
        return mascotaDAO.getByUsuario(usuarioId);
    }

    public List<Mascota> obtenerMascotasPerdidas() {
        // Retorna mascotas con estado PERDIDA_PROPIA o PERDIDA_AJENA
        List<Mascota> perdidas = mascotaDAO.getByEstado(EstadoMascota.PERDIDA_PROPIA.name());
        List<Mascota> perdidasAjenas = mascotaDAO.getByEstado(EstadoMascota.PERDIDA_AJENA.name());
        if (perdidas != null && perdidasAjenas != null) {
            perdidas.addAll(perdidasAjenas);
        }
        return perdidas;
    }

    public void cambiarEstado(Long mascotaId, EstadoMascota nuevoEstado) {
        Mascota mascota = mascotaDAO.get(mascotaId);
        if (mascota != null) {
            mascota.setEstadoMascota(nuevoEstado);
            mascotaDAO.update(mascota);
        }
    }
}

