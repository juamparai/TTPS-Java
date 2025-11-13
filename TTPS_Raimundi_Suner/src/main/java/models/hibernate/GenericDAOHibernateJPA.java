package models.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import models.DAO.GenericDAO;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.PersistenceContext;

@Transactional
public class GenericDAOHibernateJPA<T> implements GenericDAO<T> {
    protected Class<T> persistentClass;

    @PersistenceContext
    protected EntityManager em; // injected when running under Spring

    public GenericDAOHibernateJPA(Class<T> clase) {
        this.persistentClass = clase;
    }
    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    // Helper: obtain an EntityManager. If running under Spring, use injected em (do not close).
    // Otherwise create a new one from EMF.getTestEMF() which must be closed by caller when appropriate.
    protected EntityManager getEmOrCreate() {
        if (this.em != null) return this.em;
        return EMF.getTestEMF().createEntityManager();
    }

    protected void closeIfCreated(EntityManager emInstance) {
        if (this.em == null && emInstance != null && emInstance.isOpen()) {
            emInstance.close();
        }
    }

    @Override
    public T persist(T entity) {
        if (this.em != null) {
            em.persist(entity);
            return entity;
        }
        EntityManager emLocal = getEmOrCreate();
        EntityTransaction tx = null;
        try {
            tx = emLocal.getTransaction();
            tx.begin();
            emLocal.persist(entity);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            closeIfCreated(emLocal);
        }
        return entity;
    }

    @Override
    public T update(T entity) {
        if (this.em != null) {
            return em.merge(entity);
        }
        EntityManager emLocal = getEmOrCreate();
        EntityTransaction tx = emLocal.getTransaction();
        try {
            tx.begin();
            T merged = emLocal.merge(entity);
            tx.commit();
            return merged;
        } finally {
            if (tx != null && tx.isActive()) tx.rollback();
            closeIfCreated(emLocal);
        }
    }

    @Override
    public void delete(T entity) {
        if (this.em != null) {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            return;
        }
        EntityManager emLocal = getEmOrCreate();
        EntityTransaction tx = null;
        try {
            tx = emLocal.getTransaction();
            tx.begin();
            emLocal.remove(emLocal.contains(entity) ? entity : emLocal.merge(entity));
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            closeIfCreated(emLocal);
        }
    }

    @Override
    public void delete(Long id) {
        if (this.em != null) {
            T entity = em.find(getPersistentClass(), id);
            if (entity != null) em.remove(entity);
            return;
        }
        EntityManager emLocal = getEmOrCreate();
        EntityTransaction tx = null;
        try {
            tx = emLocal.getTransaction();
            tx.begin();
            T entity = emLocal.find(getPersistentClass(), id);
            if (entity != null) {
                emLocal.remove(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            closeIfCreated(emLocal);
        }
    }

    @Override
    public T get(Long id) {
        if (this.em != null) return em.find(getPersistentClass(), id);
        EntityManager emLocal = getEmOrCreate();
        try {
            return emLocal.find(getPersistentClass(), id);
        } finally {
            closeIfCreated(emLocal);
        }
    }

    @Override
    public List<T> getAll(String columnOrder) {
        if (this.em != null) {
            Query consulta = em.createQuery("SELECT e FROM "+ getPersistentClass().getSimpleName() + " e order by e." + columnOrder);
            return (List<T>) consulta.getResultList();
        }
        EntityManager emLocal = getEmOrCreate();
        try {
            Query consulta = emLocal.createQuery("SELECT e FROM "+ getPersistentClass().getSimpleName() + " e order by e." + columnOrder);
            return (List<T>) consulta.getResultList();
        } finally {
            closeIfCreated(emLocal);
        }
    }
}
