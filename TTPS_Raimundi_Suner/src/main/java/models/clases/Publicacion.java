package models.clases;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    LocalDate fecha;
    LocalDate fechaCierre;
    @Column(length = 2000)
    String descripcion;
    @Enumerated(EnumType.STRING)
    Estado estadoPublicacion;
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    Mascota mascota;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    Usuario usuario;
}
