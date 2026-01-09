package models.hibernateTest;

import APP.models.clases.Ubicacion;
import org.junit.jupiter.api.*;
import APP.models.dao.UbicacionDAO;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.transaction.annotation.Transactional;
import config.TestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class UbicacionDAOHibernateJPATest {

    @Autowired
    private UbicacionDAO dao;

    @Test
    void testAlta() {
        Ubicacion u = new Ubicacion();
        u.setLat( -34.6037 );
        u.setLng( -58.3816 );
        u.setBarrio("Centro");

        Ubicacion saved = dao.save(u);

        Ubicacion found = dao.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Centro", found.getBarrio());
    }

    @Test
    void testModificacion() {
        Ubicacion u = new Ubicacion();
        u.setLat(0.0);
        u.setLng(0.0);
        u.setBarrio("Old");
        dao.save(u);

        u.setBarrio("NewBarrio");
        Ubicacion updated = dao.save(u);

        Ubicacion found = dao.findById(updated.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("NewBarrio", found.getBarrio());
    }

    @Test
    void testBaja() {
        Ubicacion u = new Ubicacion();
        u.setLat(1.0);
        u.setLng(1.0);
        dao.save(u);
        Long id = u.getId();

        dao.deleteById(id);

        Ubicacion deleted = dao.findById(id).orElse(null);
        assertNull(deleted);
    }
}
