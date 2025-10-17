package models.clases;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String nombre;
    String apellido;
    @Column(unique = true)
    String email;
    String password;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rol_id")
    Rol rol;
    String telefono;//para saber +54 +51 etc
    String barrio;
    String ciudad;
    Boolean estado;
    int puntos;
    @OneToMany(mappedBy = "usuario")
    List<Publicacion> publicaciones;
    @OneToMany(mappedBy = "usuario")
    List<Mascota> mascotas;
    @OneToMany(mappedBy = "usuario")
    List<Avistamiento> avistamientos; //0 o mas
    @ManyToMany
    @JoinTable(
        name = "usuario_badge",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    List<Badge> medallero;



    public int getId() {
        return id;
    }
}
