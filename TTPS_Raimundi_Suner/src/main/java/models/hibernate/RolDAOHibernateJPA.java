package models.hibernate;

import models.DAO.RolDAO;
import models.clases.Rol;
import org.springframework.stereotype.Repository;

@Repository("rolDAO")
public class RolDAOHibernateJPA extends GenericDAOHibernateJPA<Rol> implements RolDAO<Rol> {

    public RolDAOHibernateJPA() {
        super(Rol.class);
    }

    @Override
    public Rol getByNombre(String nombre) {
        Rol rol;
        try {
            rol = em.createQuery("SELECT r FROM " + this.getPersistentClass().getSimpleName() + " r WHERE r.nombre = :nombre", Rol.class)
                    .setParameter("nombre", nombre).getSingleResult();
        } catch (Exception e) {
            rol = null;
        }
        return rol;
    }

}
