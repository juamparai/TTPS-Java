package APP.controllers;

import APP.dto.AvistamientoDTO;
import APP.models.clases.Avistamiento;
import APP.models.clases.Mascota;
import APP.models.clases.Usuario;
import APP.models.clases.Ubicacion;
import APP.services.AvistamientoService;
import APP.services.MascotaService;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Avistamientos", description = "API para gestión de avistamientos de mascotas")
@RestController
@RequestMapping("/api/avistamientos")
@CrossOrigin(origins = "*")
public class AvistamientoController {

    @Autowired
    private AvistamientoService avistamientoService;

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Crear un nuevo avistamiento",
               description = "Registra un avistamiento de una mascota. Usa mascotaId y usuarioId.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Avistamiento creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del avistamiento inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> crearAvistamiento(@RequestBody AvistamientoDTO dto) {
        try {
            Avistamiento avistamiento = new Avistamiento();
            avistamiento.setComentario(dto.getComentario());

            if (dto.getFecha() != null) {
                avistamiento.setFecha(LocalDate.parse(dto.getFecha()));
            }

            if (dto.getMascotaId() != null) {
                Mascota mascota = mascotaService.obtenerPorId(dto.getMascotaId());
                if (mascota == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Mascota no encontrada con ID: " + dto.getMascotaId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
                avistamiento.setMascota(mascota);
            }

            if (dto.getUsuarioId() != null) {
                Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
                if (usuario == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Usuario no encontrado con ID: " + dto.getUsuarioId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
                avistamiento.setUsuario(usuario);
            }

            if (dto.getUbicacion() != null) {
                Ubicacion ubicacion = new Ubicacion();
                ubicacion.setLat(dto.getUbicacion().getLat());
                ubicacion.setLng(dto.getUbicacion().getLng());
                ubicacion.setBarrio(dto.getUbicacion().getBarrio());
                ubicacion.setDireccion(dto.getUbicacion().getDireccion());
                avistamiento.setUbicacion(ubicacion);
            }

            Avistamiento nuevoAvistamiento = avistamientoService.crearAvistamiento(avistamiento);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Avistamiento creado exitosamente");
            response.put("avistamiento", nuevoAvistamiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear avistamiento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Listar todos los avistamientos",
               description = "Obtiene el listado completo de todos los avistamientos registrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de avistamientos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<?> listarAvistamientos() {
        try {
            List<Avistamiento> avistamientos = avistamientoService.obtenerTodos();
            return ResponseEntity.ok(avistamientos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener avistamientos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Obtener un avistamiento por ID",
               description = "Recupera los detalles completos de un avistamiento específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avistamiento encontrado"),
        @ApiResponse(responseCode = "404", description = "Avistamiento no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAvistamiento(
            @Parameter(description = "ID del avistamiento") @PathVariable Long id) {
        try {
            Avistamiento avistamiento = avistamientoService.obtenerPorId(id);
            if (avistamiento == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Avistamiento no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(avistamiento);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener avistamiento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Listar avistamientos de una mascota",
               description = "Obtiene todos los avistamientos registrados para una mascota específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de avistamientos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<?> listarAvistamientosDeMascota(
            @Parameter(description = "ID de la mascota") @PathVariable Long mascotaId) {
        try {
            List<Avistamiento> avistamientos = avistamientoService.obtenerPorMascota(mascotaId);
            return ResponseEntity.ok(avistamientos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener avistamientos de la mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Listar avistamientos de un usuario
     * GET /api/avistamientos/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarAvistamientosDeUsuario(@PathVariable Long usuarioId) {
        try {
            List<Avistamiento> avistamientos = avistamientoService.obtenerPorUsuario(usuarioId);
            return ResponseEntity.ok(avistamientos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener avistamientos del usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar un avistamiento
     * DELETE /api/avistamientos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAvistamiento(@PathVariable Long id) {
        try {
            avistamientoService.eliminarAvistamiento(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Avistamiento eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar avistamiento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

