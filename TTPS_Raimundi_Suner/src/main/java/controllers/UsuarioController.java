package controllers;

import models.clases.Usuario;
import models.hibernate.UsuarioDAOHibernateJPA;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UsuarioController {

    private final UsuarioDAOHibernateJPA usuarioDAO;

    public UsuarioController() {
        this.usuarioDAO = new UsuarioDAOHibernateJPA();
    }

    /**
     * POST /usuario - Crear nuevo usuario
     * @return 201 Created si es creado exitosamente
     */
    @PostMapping("/usuario")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        try {
            usuarioDAO.persist(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * POST /autenticacion - Autenticar usuario
     * @return 200 OK con header token: {idUsuario}+'123456' si es exitoso, 403 Forbidden si falla
     */
    @PostMapping("/autenticacion")
    public ResponseEntity<Usuario> autenticar(@RequestBody Usuario credenciales) {
        Usuario usuario = usuarioDAO.authenticate(credenciales.getEmail(), credenciales.getPassword());

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String token = usuario.getId() + "123456";

        return ResponseEntity.ok()
                .header("token", token)
                .body(usuario);
    }

    /**
     * GET /usuario/:id - Obtener usuario por ID
     * @return 200 OK, 401 Unauthorized si no hay token, 404 Not Found si no existe
     */
    @GetMapping("/usuario/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(
            @PathVariable Long id,
            @RequestHeader(value = "token", required = false) String token) {

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuario = usuarioDAO.get(id);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(usuario);
    }

    /**
     * PUT /usuario/:id - Actualizar usuario
     * En el header enviar token: {idUsuario}+'123456'
     * @return 200 OK si actualiza, 401 Unauthorized si token inválido, 404 Not Found si no existe
     */
    @PutMapping("/usuario/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable Long id,
            @RequestHeader(value = "token", required = false) String token,
            @RequestBody Usuario usuarioActualizado) {

        String expectedToken = id + "123456";
        if (token == null || !token.equals(expectedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuario = usuarioDAO.get(id);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Actualizar campos
        if (usuarioActualizado.getNombre() != null) {
            usuario.setNombre(usuarioActualizado.getNombre());
        }
        if (usuarioActualizado.getApellido() != null) {
            usuario.setApellido(usuarioActualizado.getApellido());
        }
        if (usuarioActualizado.getEmail() != null) {
            usuario.setEmail(usuarioActualizado.getEmail());
        }
        if (usuarioActualizado.getPassword() != null) {
            usuario.setPassword(usuarioActualizado.getPassword());
        }
        if (usuarioActualizado.getTelefono() != null) {
            usuario.setTelefono(usuarioActualizado.getTelefono());
        }
        if (usuarioActualizado.getBarrio() != null) {
            usuario.setBarrio(usuarioActualizado.getBarrio());
        }
        if (usuarioActualizado.getCiudad() != null) {
            usuario.setCiudad(usuarioActualizado.getCiudad());
        }
        if (usuarioActualizado.getEstado() != null) {
            usuario.setEstado(usuarioActualizado.getEstado());
        }

        usuarioDAO.update(usuario);

        return ResponseEntity.ok(usuario);
    }
}
