package models.DAO;

public interface RolDAO<T> extends GenericDAO<T>{
    public T getByNombre(String nombre);
}

