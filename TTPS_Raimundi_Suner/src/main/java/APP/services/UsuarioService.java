package APP.services;

import APP.models.clases.Usuario;
import APP.models.dao.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        // Validar que el email no exista
        Usuario existente = usuarioDAO.findByEmail(usuario.getEmail()).orElse(null);
        if (existente != null) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Inicializar valores por defecto
        if (usuario.getPuntos() == 0 || usuario.getPuntos() < 0) {
            usuario.setPuntos(0);
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }

        return usuarioDAO.save(usuario);
    }

    @Transactional
    public Usuario actualizarPerfil(Usuario usuario) {
        if (!usuarioDAO.existsById(usuario.getId())) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        return usuarioDAO.save(usuario);
    }

    public Usuario autenticar(String email, String password) {
        Usuario usuario = usuarioDAO.authenticate(email, password).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        if (!usuario.getEstado()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }
        return usuario;
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioDAO.findById(id).orElse(null);
    }

    public Usuario obtenerPorEmail(String email) {
        return usuarioDAO.findByEmail(email).orElse(null);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioDAO.findAll();
    }

    public List<Usuario> obtenerPorBarrio(String barrio) {
        return usuarioDAO.findByBarrio(barrio);
    }

    public List<Usuario> obtenerPorCiudad(String ciudad) {
        return usuarioDAO.findByCiudad(ciudad);
    }

    public List<Usuario> obtenerPorPuntos() {
        return usuarioDAO.findAllOrderByPuntosDesc();
    }

    public void agregarPuntos(Long usuarioId, int puntos) {
        Usuario usuario = usuarioDAO.findById(usuarioId).orElse(null);
        if (usuario != null) {
            usuario.setPuntos(usuario.getPuntos() + puntos);
            usuarioDAO.save(usuario);
        }
    }

    public void eliminarUsuario(Long id) {
        usuarioDAO.deleteById(id);
    }
}
