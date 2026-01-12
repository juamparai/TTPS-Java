package APP.dto;

import APP.models.clases.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de usuario (entrada/salida). La contraseña no se incluye en las respuestas.")
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String telefono;
    private String provinciaId;
    private String departamentoId;
    private String localidadId;
    private Boolean estado;
    private Integer puntos;
    private Long rolId;

    public UsuarioDTO() {
    }

    // Conversión desde entidad a DTO (oculta la contraseña)
    public static UsuarioDTO fromUsuario(Usuario u) {
        if (u == null) return null;
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setEmail(u.getEmail());
        dto.setPassword(null); // no exponer
        dto.setTelefono(u.getTelefono());
        dto.setProvinciaId(u.getProvinciaId());
        dto.setDepartamentoId(u.getDepartamentoId());
        dto.setLocalidadId(u.getLocalidadId());
        dto.setEstado(u.getEstado());
        dto.setPuntos(u.getPuntos());
        if (u.getRol() != null) dto.setRolId(u.getRol().getId());
        return dto;
    }

    // Conversión desde DTO a entidad (incluye password si viene)
    public Usuario toUsuario() {
        Usuario u = new Usuario();
        u.setId(this.id);
        u.setNombre(this.nombre);
        u.setApellido(this.apellido);
        u.setEmail(this.email);
        u.setPassword(this.password);
        u.setTelefono(this.telefono);
        u.setProvinciaId(this.provinciaId);
        u.setDepartamentoId(this.departamentoId);
        u.setLocalidadId(this.localidadId);
        u.setEstado(this.estado);
        if (this.puntos != null) u.setPuntos(this.puntos);
        // Nota: mapeo de Rol solo por id no está implementado aquí (dejamos nulo)
        return u;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getProvinciaId() { return provinciaId; }
    public void setProvinciaId(String provinciaId) { this.provinciaId = provinciaId; }

    public String getDepartamentoId() { return departamentoId; }
    public void setDepartamentoId(String departamentoId) { this.departamentoId = departamentoId; }

    public String getLocalidadId() { return localidadId; }
    public void setLocalidadId(String localidadId) { this.localidadId = localidadId; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }

    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
}
