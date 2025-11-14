package APP.controllers;

import APP.models.clases.Usuario;
import APP.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Usuarios", description = "API para gestión de usuarios del sistema")
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Registrar un nuevo usuario",
               description = "Crea una cuenta de usuario nueva en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del usuario inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/registro", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("usuario", nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Iniciar sesión",
               description = "Autentica un usuario con email y contraseña")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "400", description = "Credenciales faltantes"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email y password son requeridos");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Usuario usuario = usuarioService.autenticar(email, password);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Login exitoso");
            response.put("usuario", usuario);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error en login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Actualizar perfil de usuario",
               description = "Actualiza los datos del perfil de un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPerfil(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody Usuario usuario) {
        try {
            usuario.setId(id);
            Usuario usuarioActualizado = usuarioService.actualizarPerfil(usuario);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Perfil actualizado exitosamente");
            response.put("usuario", usuarioActualizado);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar perfil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener usuario por ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Listar todos los usuarios
     * GET /api/usuarios
     */
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodos();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al listar usuarios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener ranking de usuarios por puntos
     * GET /api/usuarios/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<?> obtenerRanking() {
        try {
            List<Usuario> ranking = usuarioService.obtenerPorPuntos();
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener ranking: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

