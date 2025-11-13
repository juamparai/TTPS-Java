package models.hibernateTest;

import APP.models.clases.Mascota;
import org.junit.jupiter.api.*;
import APP.models.dao.MascotaDAO;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import config.TestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class MascotaDAOHibernateJPATest {

    @Autowired
    private MascotaDAO dao;

    @Test
    void testAlta() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Firulais");
        mascota.setTipo("Perro");

        Mascota saved = dao.save(mascota);

        Mascota found = dao.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Firulais", found.getNombre());
    }

    @Test
    void testModificacion() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Michi");
        mascota.setTipo("Gato");

        dao.save(mascota);

        mascota.setNombre("Michu");
        Mascota updated = dao.save(mascota);

        Mascota found = dao.findById(updated.getId()).orElse(null);
        assertEquals("Michu", found.getNombre());
    }

    @Test
    void testBaja() {
        Mascota mascota = new Mascota();
        mascota.setNombre("Rex");
        mascota.setTipo("Perro");

        dao.save(mascota);
        Long id = mascota.getId();

        dao.deleteById(id);

        Mascota deleted = dao.findById(id).orElse(null);
        assertNull(deleted);
    }
}
