package models.hibernate;

import java.util.List;
import models.DAO.UbicacionDAO;
import models.clases.Ubicacion;
import org.springframework.stereotype.Repository;

@Repository("ubicacionDAO")
public class UbicacionDAOHibernateJPA extends GenericDAOHibernateJPA<Ubicacion> implements UbicacionDAO<Ubicacion> {
    public UbicacionDAOHibernateJPA() {
        super(Ubicacion.class);
    }

    @Override
    public List<Ubicacion> getByBarrio(String barrio) {
        List<Ubicacion> ubicaciones;
        try {
            ubicaciones = em.createQuery("SELECT u FROM " + this.getPersistentClass().getSimpleName() + " u WHERE u.barrio = :barrio", Ubicacion.class)
                    .setParameter("barrio", barrio)
                    .getResultList();
        } catch (Exception e) {
            ubicaciones = null;
        }
        return ubicaciones;
    }

}
