package APP.services;

import APP.models.clases.Usuario;
import APP.models.dao.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_COM_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.com$", Pattern.CASE_INSENSITIVE);

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario es requerido");
        }

        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        usuario.setNombre(usuario.getNombre().trim());

        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es requerido");
        }
        usuario.setApellido(usuario.getApellido().trim());

        if (usuario.getTelefono() == null || usuario.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es requerido");
        }
        usuario.setTelefono(usuario.getTelefono().trim());

        if (usuario.getBarrio() == null || usuario.getBarrio().trim().isEmpty()) {
            throw new IllegalArgumentException("El barrio es requerido");
        }
        usuario.setBarrio(usuario.getBarrio().trim());

        if (usuario.getCiudad() == null || usuario.getCiudad().trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad es requerida");
        }
        usuario.setCiudad(usuario.getCiudad().trim());

        String email = usuario.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido");
        }
        email = email.trim();
        if (!EMAIL_COM_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Formato de email inválido (debe ser *@*.com)");
        }
        usuario.setEmail(email);

        String password = usuario.getPassword();
        if (password == null) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }
        if (password.length() <= 6) {
            throw new IllegalArgumentException("La contraseña debe tener más de 6 caracteres");
        }

        // Validar que el email no exista
        Usuario existente = usuarioDAO.findByEmail(usuario.getEmail()).orElse(null);
        if (existente != null) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Guardar contraseña hasheada
        usuario.setPassword(passwordEncoder.encode(password));

        // Inicializar valores por defecto
        if (usuario.getPuntos() == 0 || usuario.getPuntos() < 0) {
            usuario.setPuntos(0);
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }

        try {
            return usuarioDAO.save(usuario);
        } catch (DataIntegrityViolationException e) {
            // Respaldo por si el UNIQUE constraint se dispara por condición de carrera
            throw new IllegalArgumentException("El email ya está registrado");
        }
    }

    @Transactional
    public Usuario actualizarPerfil(Usuario usuario) {
        if (!usuarioDAO.existsById(usuario.getId())) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        // Validación: si se proporciona un email nuevo, asegurar que no esté en uso por otro usuario
        String nuevoEmail = usuario.getEmail();
        if (nuevoEmail != null) {
            nuevoEmail = nuevoEmail.trim();
            if (!nuevoEmail.isEmpty()) {
                if (!EMAIL_COM_PATTERN.matcher(nuevoEmail).matches()) {
                    throw new IllegalArgumentException("Formato de email inválido (debe ser *@*.com)");
                }
                Usuario existente = usuarioDAO.findByEmail(nuevoEmail).orElse(null);
                if (existente != null && !existente.getId().equals(usuario.getId())) {
                    throw new IllegalArgumentException("El email ya está registrado por otro usuario");
                }
                usuario.setEmail(nuevoEmail);
            }
        }

        // Si se actualiza password, validar y hashear
        if (usuario.getPassword() != null) {
            if (usuario.getPassword().length() <= 6) {
                throw new IllegalArgumentException("La contraseña debe tener más de 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        try {
            return usuarioDAO.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("El email ya está registrado por otro usuario");
        }
    }

    public Usuario autenticar(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        email = email.trim();
        if (!EMAIL_COM_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        Usuario usuario = usuarioDAO.findByEmail(email).orElse(null);
        if (usuario == null || usuario.getPassword() == null) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String storedPassword = usuario.getPassword();
        boolean isBcrypt = storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$");
        boolean passwordOk;

        if (isBcrypt) {
            passwordOk = passwordEncoder.matches(password, storedPassword);
        } else {
            // Compatibilidad con usuarios creados antes del hashing: si la contraseña coincide en texto plano,
            // la migramos a BCrypt en el momento del login.
            passwordOk = storedPassword.equals(password);
            if (passwordOk) {
                usuario.setPassword(passwordEncoder.encode(password));
                usuarioDAO.save(usuario);
            }
        }

        if (!passwordOk) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        if (Boolean.FALSE.equals(usuario.getEstado())) {
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
