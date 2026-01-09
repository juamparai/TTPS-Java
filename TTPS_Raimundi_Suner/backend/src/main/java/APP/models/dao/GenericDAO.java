package APP.models.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface GenericDAO<T> {
        public void delete(T entity);
        public void delete(Long id);
        public T get(Long id);
        public List<T> getAll(String columnOrder);
        public T persist(T entity);
        public T update(T entity);
    }
