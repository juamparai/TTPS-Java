package models.DAO;

import java.util.List;

public interface UsuarioDAO<T> extends GenericDAO<T>{
    public T getByDni(int dni);
    public T getByEmail(String email);
    public List<T> getByNombreAndApellido(String nombre, String apellido);
    public T authenticate(String email, String password);
    public List<T> getByBarrio(String barrio);
    public List<T> getByCiudad(String ciudad);
}
