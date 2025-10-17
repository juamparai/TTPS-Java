package models.hibernate;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.EntityManager;
import models.DAO.PublicacionDAO;
import models.clases.Publicacion;
import models.clases.EstadoPublicacion;

public class PublicacionDAOHibernateJPA extends GenericDAOHibernateJPA<Publicacion> implements PublicacionDAO<Publicacion> {

    public PublicacionDAOHibernateJPA() {
        super(Publicacion.class);
    }

    @Override
    public List<Publicacion> getByUsuario(Long usuarioId) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Publicacion> publicaciones;
        try {
            publicaciones = em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.usuario.id = :usuarioId", Publicacion.class)
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
        } catch (Exception e) {
            publicaciones = null;
        } finally {
            em.close();
        }
        return publicaciones;
    }

    @Override
    public List<Publicacion> getByMascota(Long mascotaId) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Publicacion> publicaciones;
        try {
            publicaciones = em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.mascota.id = :mascotaId", Publicacion.class)
                    .setParameter("mascotaId", mascotaId)
                    .getResultList();
        } catch (Exception e) {
            publicaciones = null;
        } finally {
            em.close();
        }
        return publicaciones;
    }

    @Override
    public List<Publicacion> getByEstado(String estado) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Publicacion> publicaciones;
        try {
            EstadoPublicacion est = EstadoPublicacion.valueOf(estado);
            publicaciones = em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.estadoPublicacion = :estado", Publicacion.class)
                    .setParameter("estado", est)
                    .getResultList();
        } catch (Exception e) {
            publicaciones = null;
        } finally {
            em.close();
        }
        return publicaciones;
    }

    @Override
    public List<Publicacion> getByFecha(LocalDate fecha) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Publicacion> publicaciones;
        try {
            publicaciones = em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.fecha = :fecha", Publicacion.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } catch (Exception e) {
            publicaciones = null;
        } finally {
            em.close();
        }
        return publicaciones;
    }

    @Override
    public List<Publicacion> getByFechaRange(LocalDate fechaInicio, LocalDate fechaFin) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Publicacion> publicaciones;
        try {
            publicaciones = em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.fecha BETWEEN :inicio AND :fin", Publicacion.class)
                    .setParameter("inicio", fechaInicio)
                    .setParameter("fin", fechaFin)
                    .getResultList();
        } catch (Exception e) {
            publicaciones = null;
        } finally {
            em.close();
        }
        return publicaciones;
    }

    @Override
    public List<Publicacion> getPublicacionesActivas() {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Publicacion> publicaciones;
        try {
            publicaciones = em.createQuery("SELECT p FROM " + this.getPersistentClass().getSimpleName() +
                    " p WHERE p.estadoPublicacion = :activa", Publicacion.class)
                    .setParameter("activa", EstadoPublicacion.ACTIVA)
                    .getResultList();
        } catch (Exception e) {
            publicaciones = null;
        } finally {
            em.close();
        }
        return publicaciones;
    }
}
