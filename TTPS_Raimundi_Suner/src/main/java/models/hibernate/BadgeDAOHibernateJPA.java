package models.hibernate;

import jakarta.persistence.EntityManager;
import models.DAO.BadgeDAO;
import models.clases.Badge;
import org.springframework.stereotype.Repository;

@Repository("badgeDAO")
public class BadgeDAOHibernateJPA extends GenericDAOHibernateJPA<Badge> implements BadgeDAO<Badge> {
    public BadgeDAOHibernateJPA() {
        super(Badge.class);
    }

    @Override
    public Badge getByNombre(String nombre) {
        Badge badge;
        try {
            badge = em.createQuery("SELECT b FROM " + this.getPersistentClass().getSimpleName() + " b WHERE b.nombre = :nombre", Badge.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (Exception e) {
            badge = null;
        }
        return badge;
    }
}
