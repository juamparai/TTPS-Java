package APP.models.clases;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String nombre;
    String tamanio;
    String raza;
    String tipo; // perro, gato, etc
    String color;
    @Column(length = 1000)
    String descripcion;
    LocalDate fechaNac;
    @Transient
    @JsonIgnore
    List<BufferedImage> fotos;
    @Enumerated(EnumType.STRING)
    EstadoMascota estadoMascota;
    @OneToMany(mappedBy = "mascota")
    @JsonIgnore
    List<Avistamiento> avistamientos; //0 o mas

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<Publicacion> publicaciones;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    Usuario usuario;

    // Campo transitorio para indicar si la mascota pertenece al usuario autenticado
    @Transient
    private Boolean esMia;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTamanio() {
        return tamanio;
    }

    public void setTamanio(String tamanio) {
        this.tamanio = tamanio;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    public List<BufferedImage> getFotos() {
        return fotos;
    }

    public void setFotos(List<BufferedImage> fotos) {
        this.fotos = fotos;
    }

    public EstadoMascota getEstadoMascota() {
        return estadoMascota;
    }

    public void setEstadoMascota(EstadoMascota estadoMascota) {
        this.estadoMascota = estadoMascota;
    }

    public List<Avistamiento> getAvistamientos() {
        return avistamientos;
    }

    public void setAvistamientos(List<Avistamiento> avistamientos) {
        this.avistamientos = avistamientos;
    }

    public List<Publicacion> getPublicaciones() {
        return publicaciones;
    }

    public void setPublicaciones(List<Publicacion> publicaciones) {
        this.publicaciones = publicaciones;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Boolean getEsMia() {
        return esMia;
    }

    public void setEsMia(Boolean esMia) {
        this.esMia = esMia;
    }

    // Helper para obtener el ID del usuario propietario
    @JsonIgnore
    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }
}

