package models.hibernate;
import java.util.List;
import jakarta.persistence.EntityManager;
import models.DAO.UsuarioDAO;
import models.clases.Usuario;

public class UsuarioDAOHibernateJPA extends GenericDAOHibernateJPA<Usuario> implements UsuarioDAO<Usuario> {
    public UsuarioDAOHibernateJPA() {
        super(Usuario.class);
    }

    @Override
    public Usuario getByDni(int dni) {
        EntityManager em = EMF.getEMF().createEntityManager();
        Usuario usr;
        try {
            usr = (Usuario) em.createQuery("SELECT m FROM " +
                            this.getPersistentClass().getSimpleName() + " m WHERE m.dni = :dni")
                    .setParameter("dni", dni).getSingleResult();
        } catch (Exception e) {
            usr = null;
        } finally {
            em.close();
        }
        return usr;
    }

    @Override
    public Usuario getByEmail(String mail) {
        EntityManager em = EMF.getEMF().createEntityManager();
        Usuario usr;
        try {
            usr = (Usuario) em.createQuery("SELECT m FROM " +
                            this.getPersistentClass().getSimpleName() + " m WHERE m.email = :email")
                    .setParameter("email", mail).getSingleResult();
        } catch (Exception e) {
            usr = null;
        } finally {
            em.close();
        }
        return usr;
    }

    @Override
    public Usuario authenticate(String email, String password) {
        EntityManager em = EMF.getEMF().createEntityManager();
        Usuario usr;
        try {
            usr = (Usuario) em.createQuery("SELECT m FROM " +
                            this.getPersistentClass().getSimpleName() + " m WHERE m.email = :email AND m.password = :password")
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (Exception e) {
            usr = null;
        } finally {
            em.close();
        }
        return usr;
    }

    @Override
    public List<Usuario> getByNombreAndApellido(String nombre, String apellido) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Usuario> usuarios;
        try {
            usuarios = em.createQuery("SELECT m FROM " +
                            this.getPersistentClass().getSimpleName() + " m WHERE m.nombre = :nombre AND m.apellido = :apellido", Usuario.class)
                    .setParameter("nombre", nombre)
                    .setParameter("apellido", apellido)
                    .getResultList();
        } catch (Exception e) {
            usuarios = null;
        } finally {
            em.close();
        }
        return usuarios;
    }

    @Override
    public List<Usuario> getByBarrio(String barrio) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Usuario> usuarios;
        try {
            usuarios = em.createQuery("SELECT m FROM " +
                            this.getPersistentClass().getSimpleName() + " m WHERE m.barrio = :barrio", Usuario.class)
                    .setParameter("barrio", barrio)
                    .getResultList();
        } catch (Exception e) {
            usuarios = null;
        } finally {
            em.close();
        }
        return usuarios;
    }

    @Override
    public List<Usuario> getByCiudad(String ciudad) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Usuario> usuarios;
        try {
            usuarios = em.createQuery("SELECT m FROM " +
                            this.getPersistentClass().getSimpleName() + " m WHERE m.ciudad = :ciudad", Usuario.class)
                    .setParameter("ciudad", ciudad)
                    .getResultList();
        } catch (Exception e) {
            usuarios = null;
        } finally {
            em.close();
        }
        return usuarios;
    }

    @Override
    public List<Usuario> getAll(String columnOrder) {
        EntityManager em = EMF.getEMF().createEntityManager();
        List<Usuario> usuarios;
        try {
            usuarios = em.createQuery("SELECT m FROM " +
                            this.getPersistentClass().getSimpleName() +
                            " m ORDER BY m." + columnOrder, Usuario.class)
                    .getResultList();
        } catch (Exception e) {
            usuarios = null;
        } finally {
            em.close();
        }
        return usuarios;
    }

}

