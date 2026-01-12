package APP.controllers;

import APP.dto.UsuarioDTO;
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
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Tag(name = "Usuarios", description = "API para gestión de usuarios del sistema")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Pattern EMAIL_COM_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.com$", Pattern.CASE_INSENSITIVE);

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
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioDTO usuarioDto) {
        try {
            if (usuarioDto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición (Usuario) es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validaciones de formato únicamente (el resto lo maneja el service)
            if (usuarioDto.getEmail() != null) {
                String email = usuarioDto.getEmail().trim();
                if (!EMAIL_COM_PATTERN.matcher(email).matches()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de email inválido (debe ser *@*.com)");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (usuarioDto.getPassword() != null && usuarioDto.getPassword().length() <= 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La contraseña debe tener más de 6 caracteres");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Usuario usuarioEntity = usuarioDto.toUsuario();
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuarioEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("usuario", UsuarioDTO.fromUsuario(nuevoUsuario));
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
    public ResponseEntity<?> login(@RequestBody UsuarioDTO credentials) {
        try {
            if (credentials == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición (credentials) es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            String email = credentials.getEmail();
            String password = credentials.getPassword();

            // Validaciones de formato únicamente
            if (email != null) {
                email = email.trim();
                if (!EMAIL_COM_PATTERN.matcher(email).matches()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de email inválido (debe ser *@*.com)");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (password != null && password.length() <= 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La contraseña debe tener más de 6 caracteres");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Usuario usuario = usuarioService.autenticar(email, password);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Login exitoso");
            response.put("usuario", UsuarioDTO.fromUsuario(usuario));
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
            @RequestBody UsuarioDTO usuarioDto) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de usuario inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (usuarioDto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición (Usuario) es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            usuarioDto.setId(id);

            // Solo validaciones de formato (el service aplicará el resto)
            if (usuarioDto.getEmail() != null) {
                String email = usuarioDto.getEmail().trim();
                if (!EMAIL_COM_PATTERN.matcher(email).matches()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de email inválido (debe ser *@*.com)");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (usuarioDto.getPassword() != null && usuarioDto.getPassword().length() <= 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La contraseña debe tener más de 6 caracteres");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Usuario usuarioEntity = usuarioDto.toUsuario();
            Usuario usuarioActualizado = usuarioService.actualizarPerfil(usuarioEntity);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Perfil actualizado exitosamente");
            response.put("usuario", UsuarioDTO.fromUsuario(usuarioActualizado));
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
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de usuario inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(UsuarioDTO.fromUsuario(usuario));
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
            List<UsuarioDTO> dtos = usuarios.stream().map(UsuarioDTO::fromUsuario).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
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
            List<UsuarioDTO> dtos = ranking.stream().map(UsuarioDTO::fromUsuario).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener ranking: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Cambiar contraseña de usuario",
               description = "Permite a un usuario cambiar su contraseña actual por una nueva")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Contraseña actual incorrecta"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}/cambiar-password")
    public ResponseEntity<?> cambiarPassword(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody Map<String, String> passwords) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de usuario inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (passwords == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            String currentPassword = passwords.get("currentPassword");
            String newPassword = passwords.get("newPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La contraseña actual es requerida");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La nueva contraseña es requerida");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (newPassword.length() <= 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La nueva contraseña debe tener más de 6 caracteres");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Obtener el usuario
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Verificar que la contraseña actual sea correcta usando PasswordEncoder
            usuarioService.verificarPassword(usuario, currentPassword);

            // Actualizar la contraseña (el servicio la hasheará)
            usuario.setPassword(newPassword);
            usuarioService.actualizarPerfil(usuario);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Contraseña actualizada exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            // Si el error es de contraseña incorrecta, retornar 401
            if (e.getMessage().toLowerCase().contains("incorrecta") ||
                e.getMessage().toLowerCase().contains("incorrect")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al cambiar contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
