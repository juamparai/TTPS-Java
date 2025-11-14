package APP.services;

import APP.models.clases.Publicacion;
import APP.models.dao.PublicacionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class PublicacionService {

    @Autowired
    private PublicacionDAO publicacionDAO;

    @Transactional
    public Publicacion crearPublicacion(Publicacion publicacion) {
        return publicacionDAO.save(publicacion);
    }

    @Transactional
    public Publicacion actualizarPublicacion(Publicacion publicacion) {
        if (!publicacionDAO.existsById(publicacion.getId())) {
            throw new IllegalArgumentException("Publicación no encontrada");
        }
        return publicacionDAO.save(publicacion);
    }

    @Transactional
    public void eliminarPublicacion(Long id) {
        publicacionDAO.deleteById(id);
    }

    public Publicacion obtenerPorId(Long id) {
        return publicacionDAO.findById(id).orElse(null);
    }

    public List<Publicacion> obtenerTodas() {
        return publicacionDAO.findAll();
    }
}

