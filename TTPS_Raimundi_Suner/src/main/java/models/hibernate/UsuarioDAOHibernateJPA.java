package models.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import models.DAO.UsuarioDAO;
import models.clases.Usuario;
import org.springframework.stereotype.Repository;

@Repository("usuarioDAO")
public class UsuarioDAOHibernateJPA extends GenericDAOHibernateJPA<Usuario> implements UsuarioDAO<Usuario> {
    public UsuarioDAOHibernateJPA() {
        super(Usuario.class);
    }

    @Override
    public Usuario getByDni(int dni) {
        Usuario usr;
        try {
            usr = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.dni = :dni", Usuario.class)
                    .setParameter("dni", dni).getSingleResult();
        } catch (Exception e) {
            usr = null;
        }
        return usr;
    }

    @Override
    public Usuario getByEmail(String mail) {
        Usuario usr;
        try {
            usr = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.email = :email", Usuario.class)
                    .setParameter("email", mail).getSingleResult();
        } catch (Exception e) {
            usr = null;
        }
        return usr;
    }

    @Override
    public Usuario authenticate(String email, String password) {
        Usuario usr;
        try {
            usr = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.email = :email AND m.password = :password", Usuario.class)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (Exception e) {
            usr = null;
        }
        return usr;
    }

    @Override
    public List<Usuario> getByNombreAndApellido(String nombre, String apellido) {
        List<Usuario> usuarios;
        try {
            usuarios = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.nombre = :nombre AND m.apellido = :apellido", Usuario.class)
                    .setParameter("nombre", nombre)
                    .setParameter("apellido", apellido)
                    .getResultList();
        } catch (Exception e) {
            usuarios = null;
        }
        return usuarios;
    }

    @Override
    public List<Usuario> getByBarrio(String barrio) {
        List<Usuario> usuarios;
        try {
            usuarios = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.barrio = :barrio", Usuario.class)
                    .setParameter("barrio", barrio)
                    .getResultList();
        } catch (Exception e) {
            usuarios = null;
        }
        return usuarios;
    }

    @Override
    public List<Usuario> getByCiudad(String ciudad) {
        List<Usuario> usuarios;
        try {
            usuarios = em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m WHERE m.ciudad = :ciudad", Usuario.class)
                    .setParameter("ciudad", ciudad)
                    .getResultList();
        } catch (Exception e) {
            usuarios = null;
        }
        return usuarios;
    }

    @Override
    public List<Usuario> getAll(String columnOrder) {
        try {
            return em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m ORDER BY m." + columnOrder, Usuario.class)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Usuario> getByPuntos() {
        try {
            return em.createQuery("SELECT m FROM " + this.getPersistentClass().getSimpleName() + " m ORDER BY m.puntos DESC", Usuario.class)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }

}
