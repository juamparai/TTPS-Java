package models.clases;

import jakarta.persistence.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String nombre;
    String tamanio;
    String raza;
    String tipo; // perro, gato, etc
    String color;
    @Column(length = 1000)
    String descripcion;
    LocalDate fechaNac;
    @Transient
    List<BufferedImage> fotos;
    @Enumerated(EnumType.STRING)
    EstadoMascota estadoMascota;
    @OneToMany(mappedBy = "mascota")
    List<Avistamiento> avistamientos; //0 o mas
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    Usuario usuario;
}
