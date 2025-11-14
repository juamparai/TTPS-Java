package APP.dto;

public class AvistamientoDTO {
    private String comentario;
    private String fecha;
    private Long mascotaId;
    private Long usuarioId;
    private UbicacionDTO ubicacion;

    // Getters y Setters
    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Long getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Long mascotaId) {
        this.mascotaId = mascotaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public UbicacionDTO getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(UbicacionDTO ubicacion) {
        this.ubicacion = ubicacion;
    }
}

