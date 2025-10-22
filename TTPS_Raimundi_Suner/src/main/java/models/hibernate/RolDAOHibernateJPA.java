package models.hibernate;

import models.DAO.RolDAO;
import models.clases.Rol;

public class RolDAOHibernateJPA extends GenericDAOHibernateJPA<Rol> implements RolDAO<Rol> {

    public RolDAOHibernateJPA() {
        super(Rol.class);
    }

    @Override
    public Rol getByNombre(String nombre) {
        var em = EMF.getEMF().createEntityManager();
        Rol rol;
        try {
            rol = (Rol) em.createQuery("SELECT r FROM " +
                            this.getPersistentClass().getSimpleName() + " r WHERE r.nombre = :nombre")
                    .setParameter("nombre", nombre).getSingleResult();
        } catch (Exception e) {
            rol = null;
        } finally {
            em.close();
        }
        return rol;
    }


}
