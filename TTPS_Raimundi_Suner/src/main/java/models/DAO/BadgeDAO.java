package models.DAO;

public interface BadgeDAO<T> extends GenericDAO<T>{
    public T getByNombre(String nombre);
}

