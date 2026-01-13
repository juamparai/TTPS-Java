package APP.models.clases;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    LocalDate fecha;
    LocalDate fechaCierre;
    @Column(length = 2000)
    String descripcion;
    @Enumerated(EnumType.STRING)
    EstadoPublicacion estadoPublicacion;
    Double lat;
    Double lng;
    String municipioId;
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    Mascota mascota;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    Usuario usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoPublicacion getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public void setEstadoPublicacion(EstadoPublicacion estadoPublicacion) {
        this.estadoPublicacion = estadoPublicacion;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getMunicipioId() {
        return municipioId;
    }

    public void setMunicipioId(String municipioId) {
        this.municipioId = municipioId;
    }

    // Getters para serialización JSON
    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }

    public Long getMascotaId() {
        return mascota != null ? mascota.getId() : null;
    }

    public String getMascotaNombre() {
        return mascota != null ? mascota.getNombre() : null;
    }

    public String getMascotaTipo() {
        return mascota != null ? mascota.getTipo() : null;
    }
}
