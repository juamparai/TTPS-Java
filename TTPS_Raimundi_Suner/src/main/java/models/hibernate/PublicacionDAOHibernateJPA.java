package models.hibernate;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import models.DAO.PublicacionDAO;
import models.clases.Publicacion;
import models.clases.EstadoPublicacion;
import org.springframework.stereotype.Repository;

@Repository("publicacionDAO")
public class PublicacionDAOHibernateJPA extends GenericDAOHibernateJPA<Publicacion> implements PublicacionDAO<Publicacion> {

    public PublicacionDAOHibernateJPA() {
        super(Publicacion.class);
    }

    @Override
    public List<Publicacion> getByUsuario(Long usuarioId) {
        if (usuarioId == null) return Collections.emptyList();
        try {
            return em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.usuario.id = :usuarioId", Publicacion.class)
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Publicacion> getByMascota(Long mascotaId) {
        if (mascotaId == null) return Collections.emptyList();
        try {
            return em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.mascota.id = :mascotaId", Publicacion.class)
                    .setParameter("mascotaId", mascotaId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Publicacion> getByEstado(String estado) {
        if (estado == null) return Collections.emptyList();
        try {
            EstadoPublicacion est;
            try {
                est = EstadoPublicacion.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException iae) {
                return Collections.emptyList();
            }
            return em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.estadoPublicacion = :estado", Publicacion.class)
                    .setParameter("estado", est)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Publicacion> getByFecha(LocalDate fecha) {
        if (fecha == null) return Collections.emptyList();
        try {
            return em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.fecha = :fecha", Publicacion.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Publicacion> getByFechaRange(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) return Collections.emptyList();
        try {
            return em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.fecha BETWEEN :inicio AND :fin", Publicacion.class)
                    .setParameter("inicio", fechaInicio)
                    .setParameter("fin", fechaFin)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Publicacion> getPublicacionesActivas() {
        try {
            return em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.estadoPublicacion = :activa", Publicacion.class)
                    .setParameter("activa", EstadoPublicacion.ACTIVA)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
