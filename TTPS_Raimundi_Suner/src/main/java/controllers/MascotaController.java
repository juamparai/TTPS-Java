package controllers;

import models.clases.Mascota;
import models.clases.EstadoMascota;
import services.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "*")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    /**
     * Crear una nueva mascota
     * POST /api/mascotas
     */
    @PostMapping
    public ResponseEntity<?> crearMascota(@RequestBody Mascota mascota) {
        try {
            Mascota nuevaMascota = mascotaService.crearMascota(mascota);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Mascota creada exitosamente");
            response.put("mascota", nuevaMascota);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Editar una mascota existente
     * PUT /api/mascotas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editarMascota(@PathVariable Long id, @RequestBody Mascota mascota) {
        try {
            mascota.setId(id);
            Mascota mascotaActualizada = mascotaService.actualizarMascota(mascota);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Mascota actualizada exitosamente");
            response.put("mascota", mascotaActualizada);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar una mascota
     * DELETE /api/mascotas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(@PathVariable Long id) {
        try {
            mascotaService.eliminarMascota(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Mascota eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener una mascota por ID
     * GET /api/mascotas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMascota(@PathVariable Long id) {
        try {
            Mascota mascota = mascotaService.obtenerPorId(id);
            if (mascota == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mascota no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(mascota);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Listar mascotas de un usuario
     * GET /api/mascotas/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarMascotasDeUsuario(@PathVariable Long usuarioId) {
        try {
            List<Mascota> mascotas = mascotaService.obtenerPorUsuario(usuarioId);
            return ResponseEntity.ok(mascotas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener mascotas del usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Listar todas las mascotas perdidas
     * GET /api/mascotas/perdidas
     */
    @GetMapping("/perdidas")
    public ResponseEntity<?> listarMascotasPerdidas() {
        try {
            List<Mascota> mascotasPerdidas = mascotaService.obtenerMascotasPerdidas();
            return ResponseEntity.ok(mascotasPerdidas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener mascotas perdidas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Listar todas las mascotas
     * GET /api/mascotas
     */
    @GetMapping
    public ResponseEntity<?> listarTodasLasMascotas() {
        try {
            List<Mascota> mascotas = mascotaService.obtenerTodas();
            return ResponseEntity.ok(mascotas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener mascotas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Cambiar estado de una mascota
     * PATCH /api/mascotas/{id}/estado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String estadoStr = body.get("estado");
            if (estadoStr == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Estado es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            EstadoMascota estado = EstadoMascota.valueOf(estadoStr);
            mascotaService.cambiarEstado(id, estado);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Estado actualizado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Estado inválido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

