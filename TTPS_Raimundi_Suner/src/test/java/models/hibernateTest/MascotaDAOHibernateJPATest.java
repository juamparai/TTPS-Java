package models.hibernateTest;

import models.clases.Mascota;
import org.junit.jupiter.api.*;
import models.hibernate.MascotaDAOHibernateJPA;

import static org.junit.jupiter.api.Assertions.*;

class MascotaDAOHibernateJPATest {

    private MascotaDAOHibernateJPA mascotaDAO;

    @BeforeEach
    void setup() {
        mascotaDAO = new MascotaDAOHibernateJPA();
    }

    @Test
    void testAlta() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Firulais");
        mascota.setTipo("Perro");

        Mascota saved = mascotaDAO.persist(mascota);

        Mascota found = mascotaDAO.get(saved.getId());
        assertNotNull(found);
        assertEquals("Firulais", found.getNombre());
    }

    @Test
    void testModificacion() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Michi");
        mascota.setTipo("Gato");

        mascotaDAO.persist(mascota);

        mascota.setNombre("Michu");
        Mascota updated = mascotaDAO.update(mascota);

        Mascota found = mascotaDAO.get(updated.getId());
        assertEquals("Michu", found.getNombre());
    }

    @Test
    void testBaja() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Rex");
        mascota.setTipo("Perro");

        mascotaDAO.persist(mascota);
        Long id = mascota.getId();

        mascotaDAO.delete(id);

        Mascota deleted = mascotaDAO.get(id);
        assertNull(deleted);
    }
}
