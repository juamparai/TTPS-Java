package models.DAO;

import java.util.List;

public interface MascotaDAO<T> extends GenericDAO<T>{
    public List<T> getByNombre(String nombre);
    public List<T> getByTipo(String tipo);
    public List<T> getByRaza(String raza);
    public List<T> getByColor(String color);
    public List<T> getByEstado(String estado);
    public List<T> getByUsuario(Long usuarioId);
}

