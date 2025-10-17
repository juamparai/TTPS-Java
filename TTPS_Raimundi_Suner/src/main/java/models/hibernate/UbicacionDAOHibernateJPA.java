package models.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import models.DAO.UbicacionDAO;
import models.clases.Ubicacion;

public class UbicacionDAOHibernateJPA extends GenericDAOHibernateJPA<Ubicacion> implements UbicacionDAO<Ubicacion> {
    public UbicacionDAOHibernateJPA() {
        super(Ubicacion.class);
    }

    @Override
    public List<Ubicacion> getByBarrio(String barrio) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Ubicacion> ubicaciones;
        try {
            ubicaciones = em.createQuery("SELECT u FROM " + this.getPersistentClass().getSimpleName() + " u WHERE u.barrio = :barrio", Ubicacion.class)
                    .setParameter("barrio", barrio)
                    .getResultList();
        } catch (Exception e) {
            ubicaciones = null;
        } finally {
            em.close();
        }
        return ubicaciones;
    }

}
