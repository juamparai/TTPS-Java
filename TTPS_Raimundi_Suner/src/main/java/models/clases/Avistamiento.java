package models.clases;

import jakarta.persistence.*;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Avistamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    LocalDate fecha;
    @Column(length = 1000)
    String comentario;
    @Transient
    List<BufferedImage> fotos;
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    Mascota mascota;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    Usuario usuario;
    @ManyToOne
    @JoinColumn(name = "ubicacion_id")
    Ubicacion ubicacion;

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

    public String getComentario() {
        return comentario;
    }
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public List<BufferedImage> getFotos() {
        return fotos;
    }

    public void setFotos(List<BufferedImage> fotos) {
        this.fotos = fotos;
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

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

}

