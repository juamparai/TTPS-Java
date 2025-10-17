package models.DAO;

import java.util.List;

public interface UbicacionDAO<T> extends GenericDAO<T>{
    public List<T> getByBarrio(String barrio);
}
