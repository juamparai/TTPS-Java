package APP.controllers;

import APP.models.clases.Avistamiento;
import APP.services.AvistamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avistamientos")
@CrossOrigin(origins = "*")
public class AvistamientoController {

    @Autowired
    private AvistamientoService avistamientoService;

    /**
     * Crear un nuevo avistamiento
     * POST /api/avistamientos
     */
    @PostMapping
    public ResponseEntity<?> crearAvistamiento(@RequestBody Avistamiento avistamiento) {
        try {
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
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear avistamiento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Listar todos los avistamientos
     * GET /api/avistamientos
     */
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

    /**
     * Obtener avistamiento por ID
     * GET /api/avistamientos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAvistamiento(@PathVariable Long id) {
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

    /**
     * Listar avistamientos de una mascota
     * GET /api/avistamientos/mascota/{mascotaId}
     */
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<?> listarAvistamientosDeMascota(@PathVariable Long mascotaId) {
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

