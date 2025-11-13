package models.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import models.DAO.MascotaDAO;
import models.clases.Mascota;
import models.clases.EstadoMascota;
import org.springframework.stereotype.Repository;

@Repository("mascotaDAO")
public class MascotaDAOHibernateJPA extends GenericDAOHibernateJPA<Mascota> implements MascotaDAO<Mascota> {
    public MascotaDAOHibernateJPA() {
        super(Mascota.class);
    }

    @Override
    public List<Mascota> getByNombre(String nombre) {
        List<Mascota> lista;
        try {
            lista = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.nombre = :nombre", Mascota.class)
                    .setParameter("nombre", nombre)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Mascota> getByTipo(String tipo) {
        List<Mascota> lista;
        try {
            lista = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.tipo = :tipo", Mascota.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Mascota> getByRaza(String raza) {
        List<Mascota> lista;
        try {
            lista = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.raza = :raza", Mascota.class)
                    .setParameter("raza", raza)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Mascota> getByColor(String color) {
        List<Mascota> lista;
        try {
            lista = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.color = :color", Mascota.class)
                    .setParameter("color", color)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Mascota> getByEstado(String estado) {
        List<Mascota> lista;
        try {
            EstadoMascota est = EstadoMascota.valueOf(estado);
            lista = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.estadoMascota = :estado", Mascota.class)
                    .setParameter("estado", est)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Mascota> getByUsuario(Long usuarioId) {
        List<Mascota> lista;
        try {
            lista = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.usuario.id = :usuarioId", Mascota.class)
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }
}
