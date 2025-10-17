package models.hibernate;

import models.DAO.RolDAO;
import models.clases.Rol;

public class RolDAOHibernateJPA extends GenericDAOHibernateJPA<Rol> implements RolDAO<Rol> {

    public RolDAOHibernateJPA() {
        super(Rol.class);
    }


}
