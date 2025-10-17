package models.clases;

import jakarta.persistence.*;

@Entity
public class Ubicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String barrio;
    double lat;
    double lng;
}
