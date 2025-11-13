package services;

import models.clases.Usuario;
import models.DAO.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioDAO<Usuario> usuarioDAO;

    public Usuario registrarUsuario(Usuario usuario) {
        // Validar que el email no exista
        Usuario existente = usuarioDAO.getByEmail(usuario.getEmail());
        if (existente != null) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Inicializar valores por defecto
        if (usuario.getPuntos() == 0) {
            usuario.setPuntos(0);
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }

        return usuarioDAO.persist(usuario);
    }

    public Usuario actualizarPerfil(Usuario usuario) {
        Usuario existente = usuarioDAO.get(usuario.getId());
        if (existente == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        return usuarioDAO.update(usuario);
    }

    public Usuario autenticar(String email, String password) {
        Usuario usuario = usuarioDAO.authenticate(email, password);
        if (usuario == null) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        if (!usuario.getEstado()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }
        return usuario;
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioDAO.get(id);
    }

    public Usuario obtenerPorEmail(String email) {
        return usuarioDAO.getByEmail(email);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioDAO.getAll("id");
    }

    public List<Usuario> obtenerPorBarrio(String barrio) {
        return usuarioDAO.getByBarrio(barrio);
    }

    public List<Usuario> obtenerPorCiudad(String ciudad) {
        return usuarioDAO.getByCiudad(ciudad);
    }

    public List<Usuario> obtenerPorPuntos() {
        return usuarioDAO.getByPuntos();
    }

    public void agregarPuntos(Long usuarioId, int puntos) {
        Usuario usuario = usuarioDAO.get(usuarioId);
        if (usuario != null) {
            usuario.setPuntos(usuario.getPuntos() + puntos);
            usuarioDAO.update(usuario);
        }
    }

    public void eliminarUsuario(Long id) {
        usuarioDAO.delete(id);
    }
}

