package models.hibernate;

import jakarta.persistence.EntityManager;
import models.DAO.BadgeDAO;
import models.clases.Badge;

public class BadgeDAOHibernateJPA extends GenericDAOHibernateJPA<Badge> implements BadgeDAO<Badge> {
    public BadgeDAOHibernateJPA() {
        super(Badge.class);
    }

    @Override
    public Badge getByNombre(String nombre) {
        EntityManager em = EMF.getEMF().createEntityManager();
        Badge badge;
        try {
            badge = (Badge) em.createQuery("SELECT b FROM " + this.getPersistentClass().getSimpleName() + " b WHERE b.nombre = :nombre")
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (Exception e) {
            badge = null;
        } finally {
            em.close();
        }
        return badge;
    }
}

