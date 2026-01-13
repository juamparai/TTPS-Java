package APP.controllers;

import APP.dto.MascotaDTO;
import APP.models.clases.Mascota;
import APP.models.clases.EstadoMascota;
import APP.models.clases.Usuario;
import APP.services.FileStorageService;
import APP.services.MascotaService;
import APP.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Mascotas", description = "API para gestión de mascotas perdidas y encontradas")
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseEntity<?> validarDtoCrear(MascotaDTO dto) {
        if (dto == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Cuerpo de la petición (MascotaDTO) es requerido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "El nombre de la mascota es requerido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (dto.getUsuarioId() != null && dto.getUsuarioId() <= 0) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "usuarioId debe ser un número positivo");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (dto.getEstadoMascota() != null) {
            try {
                EstadoMascota.valueOf(dto.getEstadoMascota());
            } catch (IllegalArgumentException ex) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "estadoMascota inválido: " + dto.getEstadoMascota());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
            if (usuario == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no encontrado con ID: " + dto.getUsuarioId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }

        return null;
    }

    private Mascota mapearDtoAMascota(MascotaDTO dto) {
        Mascota mascota = new Mascota();
        mascota.setNombre(dto.getNombre());
        mascota.setTipo(dto.getTipo());
        mascota.setRaza(dto.getRaza());
        mascota.setColor(dto.getColor());
        mascota.setTamanio(dto.getTamanio());
        mascota.setDescripcion(dto.getDescripcion());
        mascota.setFechaNac(dto.getFechaNac());

        if (dto.getEstadoMascota() != null) {
            mascota.setEstadoMascota(EstadoMascota.valueOf(dto.getEstadoMascota()));
        }

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
            mascota.setUsuario(usuario);
        }

        // Permite setear imagenUrl si viene (opcional), pero normalmente se setea desde el upload
        if (dto.getImagenUrl() != null && !dto.getImagenUrl().trim().isEmpty()) {
            mascota.setImagenUrl(dto.getImagenUrl().trim());
        }

        return mascota;
    }

    private void aplicarDtoEnMascotaExistente(Mascota mascota, MascotaDTO dto) {
        mascota.setNombre(dto.getNombre());
        mascota.setTipo(dto.getTipo());
        mascota.setRaza(dto.getRaza());
        mascota.setColor(dto.getColor());
        mascota.setTamanio(dto.getTamanio());
        mascota.setDescripcion(dto.getDescripcion());
        mascota.setFechaNac(dto.getFechaNac());

        if (dto.getEstadoMascota() != null) {
            mascota.setEstadoMascota(EstadoMascota.valueOf(dto.getEstadoMascota()));
        }

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
            mascota.setUsuario(usuario);
        }
    }

    /**
     * Helper method para marcar si una mascota pertenece al usuario autenticado
     */
    private void marcarPropiedad(Mascota mascota, HttpServletRequest request) {
        Long authenticatedUserId = (Long) request.getAttribute("authenticatedUserId");
        if (authenticatedUserId != null && mascota != null) {
            mascota.setEsMia(authenticatedUserId.equals(mascota.getUsuarioId()));
        } else {
            mascota.setEsMia(false);
        }
    }

    /**
     * Helper method para marcar una lista de mascotas
     */
    private void marcarPropiedadLista(List<Mascota> mascotas, HttpServletRequest request) {
        for (Mascota mascota : mascotas) {
            marcarPropiedad(mascota, request);
        }
    }

    @Operation(summary = "Crear una nueva mascota",
               description = "Registra una nueva mascota en el sistema. Usa usuarioId para asociarla a un usuario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Mascota creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de la mascota inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> crearMascota(@RequestBody MascotaDTO dto) {
        try {
            ResponseEntity<?> validationError = validarDtoCrear(dto);
            if (validationError != null) return validationError;

            Mascota mascota = mapearDtoAMascota(dto);

            Mascota nuevaMascota = mascotaService.crearMascota(mascota);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Mascota creada exitosamente");
            response.put("mascota", nuevaMascota);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("detalle", "Validación fallida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear mascota: " + e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Crear una nueva mascota con imagen",
            description = "Registra una nueva mascota y permite subir una imagen (jpg/jpeg/png).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mascota creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o imagen inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> crearMascotaConImagen(
            @RequestParam("mascota") String mascotaJson,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        try {
            MascotaDTO dto;
            try {
                dto = objectMapper.readValue(mascotaJson, MascotaDTO.class);
            } catch (JsonProcessingException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Formato inválido en el campo 'mascota' (JSON)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            ResponseEntity<?> validationError = validarDtoCrear(dto);
            if (validationError != null) return validationError;

            Mascota mascota = mapearDtoAMascota(dto);

            if (imagen != null && !imagen.isEmpty()) {
                String imagenUrl = fileStorageService.storeMascotaImage(imagen);
                mascota.setImagenUrl(imagenUrl);
            }

            Mascota nuevaMascota = mascotaService.crearMascota(mascota);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Mascota creada exitosamente");
            response.put("mascota", nuevaMascota);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("detalle", "Validación fallida");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear mascota: " + e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Actualizar una mascota existente",
               description = "Actualiza los datos de una mascota específica por su ID. Usa usuarioId para cambiar el propietario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mascota actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Mascota no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editarMascota(
            @Parameter(description = "ID de la mascota a actualizar") @PathVariable Long id,
            @RequestBody MascotaDTO dto) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de mascota inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (dto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición (MascotaDTO) es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Mascota mascota = mascotaService.obtenerPorId(id);
            if (mascota == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mascota no encontrada con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El nombre de la mascota es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            mascota.setNombre(dto.getNombre());
            mascota.setTipo(dto.getTipo());
            mascota.setRaza(dto.getRaza());
            mascota.setColor(dto.getColor());
            mascota.setTamanio(dto.getTamanio());
            mascota.setDescripcion(dto.getDescripcion());

            if (dto.getEstadoMascota() != null) {
                try {
                    mascota.setEstadoMascota(EstadoMascota.valueOf(dto.getEstadoMascota()));
                } catch (IllegalArgumentException ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "estadoMascota inválido: " + dto.getEstadoMascota());
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
                if (usuario == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Usuario no encontrado con ID: " + dto.getUsuarioId());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
                mascota.setUsuario(usuario);
            }

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

    @Operation(summary = "Actualizar una mascota con imagen",
            description = "Actualiza los datos de una mascota y permite reemplazar la imagen (jpg/jpeg/png).")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editarMascotaConImagen(
            @PathVariable Long id,
            @RequestParam("mascota") String mascotaJson,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de mascota inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            MascotaDTO dto;
            try {
                dto = objectMapper.readValue(mascotaJson, MascotaDTO.class);
            } catch (JsonProcessingException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Formato inválido en el campo 'mascota' (JSON)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            ResponseEntity<?> validationError = validarDtoCrear(dto);
            if (validationError != null) return validationError;

            Mascota mascota = mascotaService.obtenerPorId(id);
            if (mascota == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mascota no encontrada con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            aplicarDtoEnMascotaExistente(mascota, dto);

            if (imagen != null && !imagen.isEmpty()) {
                String imagenUrl = fileStorageService.storeMascotaImage(imagen);
                mascota.setImagenUrl(imagenUrl);
            }

            Mascota mascotaActualizada = mascotaService.actualizarMascota(mascota);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Mascota actualizada exitosamente");
            response.put("mascota", mascotaActualizada);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar mascota: " + e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Eliminar una mascota",
               description = "Elimina permanentemente una mascota del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mascota eliminada exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(
            @Parameter(description = "ID de la mascota a eliminar") @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de mascota inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

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

    @Operation(summary = "Obtener una mascota por ID",
               description = "Recupera los detalles completos de una mascota específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mascota encontrada"),
        @ApiResponse(responseCode = "404", description = "Mascota no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMascota(
            @Parameter(description = "ID de la mascota") @PathVariable Long id,
            HttpServletRequest request) {
        try {
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de mascota inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            Mascota mascota = mascotaService.obtenerPorId(id);
            if (mascota == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mascota no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            marcarPropiedad(mascota, request);
            return ResponseEntity.ok(mascota);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Listar mascotas de un usuario",
               description = "Obtiene todas las mascotas registradas por un usuario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de mascotas obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarMascotasDeUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
            HttpServletRequest request) {
        try {
            if (usuarioId == null || usuarioId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de usuario inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            List<Mascota> mascotas = mascotaService.obtenerPorUsuario(usuarioId);
            marcarPropiedadLista(mascotas, request);
            return ResponseEntity.ok(mascotas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener mascotas del usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Listar todas las mascotas perdidas",
               description = "Obtiene todas las mascotas que están marcadas con estado PERDIDO")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de mascotas perdidas obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/perdidas")
    public ResponseEntity<?> listarMascotasPerdidas(HttpServletRequest request) {
        try {
            List<Mascota> mascotasPerdidas = mascotaService.obtenerMascotasPerdidas();
            marcarPropiedadLista(mascotasPerdidas, request);
            return ResponseEntity.ok(mascotasPerdidas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener mascotas perdidas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Listar todas las mascotas",
               description = "Obtiene el listado completo de todas las mascotas registradas en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de mascotas obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<?> listarTodasLasMascotas(HttpServletRequest request) {
        try {
            List<Mascota> mascotas = mascotaService.obtenerTodas();
            marcarPropiedadLista(mascotas, request);
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
            if (id == null || id <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID de mascota inválido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (body == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cuerpo de la petición es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            String estadoStr = body.get("estado");
            if (estadoStr == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Estado es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            EstadoMascota estado;
            try {
                estado = EstadoMascota.valueOf(estadoStr);
            } catch (IllegalArgumentException ex) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Estado inválido: " + estadoStr);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

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
