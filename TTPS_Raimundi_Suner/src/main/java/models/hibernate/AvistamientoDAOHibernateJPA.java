package models.hibernate;

import java.util.List;
import java.time.LocalDate;
import jakarta.persistence.EntityManager;
import models.DAO.AvistamientoDAO;
import models.clases.Avistamiento;
import org.springframework.stereotype.Repository;

@Repository("avistamientoDAO")
public class AvistamientoDAOHibernateJPA extends GenericDAOHibernateJPA<Avistamiento> implements AvistamientoDAO<Avistamiento> {
    public AvistamientoDAOHibernateJPA() {
        super(Avistamiento.class);
    }

    @Override
    public List<Avistamiento> getByFecha(LocalDate fecha) {
        List<Avistamiento> lista;
        try {
            lista = em.createQuery("SELECT a FROM " + this.getPersistentClass().getSimpleName() + " a WHERE a.fecha = :fecha", Avistamiento.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Avistamiento> getByFechaRange(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Avistamiento> lista;
        try {
            lista = em.createQuery("SELECT a FROM " + this.getPersistentClass().getSimpleName() + " a WHERE a.fecha BETWEEN :inicio AND :fin", Avistamiento.class)
                    .setParameter("inicio", fechaInicio)
                    .setParameter("fin", fechaFin)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Avistamiento> getByMascota(Long mascotaId) {
        List<Avistamiento> lista;
        try {
            lista = em.createQuery("SELECT a FROM " + this.getPersistentClass().getSimpleName() + " a WHERE a.mascota.id = :mascotaId", Avistamiento.class)
                    .setParameter("mascotaId", mascotaId)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }

    @Override
    public List<Avistamiento> getByUsuario(Long usuarioId) {
        List<Avistamiento> lista;
        try {
            lista = em.createQuery("SELECT a FROM " + this.getPersistentClass().getSimpleName() + " a WHERE a.usuario.id = :usuarioId", Avistamiento.class)
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
        } catch (Exception e) {
            lista = null;
        }
        return lista;
    }
}
