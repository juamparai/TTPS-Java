package APP.controllers;

import APP.dto.PublicacionDTO;
import APP.models.clases.Publicacion;
import APP.models.clases.EstadoPublicacion;
import APP.models.clases.Mascota;
import APP.models.clases.Usuario;
import APP.services.PublicacionService;
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

@Tag(name = "Publicaciones", description = "API para gestión de publicaciones de mascotas")
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    @Autowired
    private PublicacionService publicacionService;

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Crear una nueva publicación",
               description = "Crea una publicación para una mascota. Usa mascotaId y usuarioId.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Publicación creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> crearPublicacion(@RequestBody PublicacionDTO dto) {
        try {
            if (dto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición (PublicacionDTO) es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (dto.getFecha() != null) {
                try {
                    LocalDate.parse(dto.getFecha());
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de fecha inválido: " + dto.getFecha());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (dto.getFechaCierre() != null) {
                try {
                    LocalDate.parse(dto.getFechaCierre());
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de fechaCierre inválido: " + dto.getFechaCierre());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (dto.getEstadoPublicacion() != null) {
                try {
                    EstadoPublicacion.valueOf(dto.getEstadoPublicacion());
                } catch (IllegalArgumentException ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "estadoPublicacion inválido: " + dto.getEstadoPublicacion());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (dto.getMascotaId() != null && dto.getMascotaId() <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "mascotaId debe ser un número positivo");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (dto.getMascotaId() != null) {
                Mascota mascota = mascotaService.obtenerPorId(dto.getMascotaId());
                if (mascota == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Mascota no encontrada con ID: " + dto.getMascotaId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (dto.getUsuarioId() != null && dto.getUsuarioId() <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "usuarioId debe ser un número positivo");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (dto.getUsuarioId() != null) {
                Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
                if (usuario == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Usuario no encontrado con ID: " + dto.getUsuarioId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            Publicacion publicacion = new Publicacion();
            publicacion.setDescripcion(dto.getDescripcion());

            if (dto.getFecha() != null) {
                publicacion.setFecha(LocalDate.parse(dto.getFecha()));
            }

            if (dto.getFechaCierre() != null) {
                publicacion.setFechaCierre(LocalDate.parse(dto.getFechaCierre()));
            }

            if (dto.getEstadoPublicacion() != null) {
                publicacion.setEstadoPublicacion(EstadoPublicacion.valueOf(dto.getEstadoPublicacion()));
            }

            if (dto.getMascotaId() != null) {
                Mascota mascota = mascotaService.obtenerPorId(dto.getMascotaId());
                publicacion.setMascota(mascota);
            }

            if (dto.getUsuarioId() != null) {
                Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
                publicacion.setUsuario(usuario);
            }

            // Mapear coordenadas y municipio
            publicacion.setLat(dto.getLat());
            publicacion.setLng(dto.getLng());
            publicacion.setMunicipioId(dto.getMunicipioId());

            Publicacion nuevaPublicacion = publicacionService.crearPublicacion(publicacion);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Publicación creada exitosamente");
            response.put("publicacion", nuevaPublicacion);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear publicación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Actualizar una publicación",
               description = "Actualiza una publicación existente")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPublicacion(
            @Parameter(description = "ID de la publicación") @PathVariable Long id,
            @RequestBody PublicacionDTO dto) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de publicación inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (dto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición (PublicacionDTO) es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Publicacion publicacion = publicacionService.obtenerPorId(id);
            if (publicacion == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Publicación no encontrada con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            if (dto.getFecha() != null) {
                try {
                    LocalDate.parse(dto.getFecha());
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de fecha inválido: " + dto.getFecha());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (dto.getFechaCierre() != null) {
                try {
                    LocalDate.parse(dto.getFechaCierre());
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de fechaCierre inválido: " + dto.getFechaCierre());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (dto.getEstadoPublicacion() != null) {
                try {
                    publicacion.setEstadoPublicacion(EstadoPublicacion.valueOf(dto.getEstadoPublicacion()));
                } catch (IllegalArgumentException ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "estadoPublicacion inválido: " + dto.getEstadoPublicacion());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            publicacion.setDescripcion(dto.getDescripcion());

            if (dto.getFecha() != null) {
                publicacion.setFecha(LocalDate.parse(dto.getFecha()));
            }

            if (dto.getFechaCierre() != null) {
                publicacion.setFechaCierre(LocalDate.parse(dto.getFechaCierre()));
            }

            if (dto.getLat() != null) {
                publicacion.setLat(dto.getLat());
            }

            if (dto.getLng() != null) {
                publicacion.setLng(dto.getLng());
            }

            if (dto.getMunicipioId() != null) {
                publicacion.setMunicipioId(dto.getMunicipioId());
            }

            if (dto.getMascotaId() != null) {
                if (dto.getMascotaId() <= 0) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "mascotaId debe ser un número positivo");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
                Mascota mascota = mascotaService.obtenerPorId(dto.getMascotaId());
                if (mascota != null) {
                    publicacion.setMascota(mascota);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Mascota no encontrada con ID: " + dto.getMascotaId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            if (dto.getUsuarioId() != null) {
                if (dto.getUsuarioId() <= 0) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "usuarioId debe ser un número positivo");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
                Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
                if (usuario != null) {
                    publicacion.setUsuario(usuario);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Usuario no encontrado con ID: " + dto.getUsuarioId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            Publicacion actualizada = publicacionService.actualizarPublicacion(publicacion);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Publicación actualizada exitosamente");
            response.put("publicacion", actualizada);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar publicación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Obtener publicación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPublicacion(@Parameter(description = "ID de la publicación") @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de publicación inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Publicacion publicacion = publicacionService.obtenerPorId(id);
            if (publicacion == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Publicación no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(publicacion);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener publicación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Listar todas las publicaciones")
    @GetMapping
    public ResponseEntity<?> listarPublicaciones() {
        try {
            List<Publicacion> publicaciones = publicacionService.obtenerTodas();
            return ResponseEntity.ok(publicaciones);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al listar publicaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Eliminar publicación")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPublicacion(@Parameter(description = "ID de la publicación") @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de publicación inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            publicacionService.eliminarPublicacion(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Publicación eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar publicación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
