package models.hibernateTest;

import APP.models.clases.Rol;
import org.junit.jupiter.api.*;
import APP.models.dao.RolDAO;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import config.TestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class RolDAOHibernateJPATest {

    @Autowired
    private RolDAO dao;

    @Test
    void testAlta() {
        Rol r = new Rol();
        r.setNombre("TEST_ROLE");

        Rol saved = dao.save(r);

        Rol found = dao.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("TEST_ROLE", found.getNombre());
    }

    @Test
    void testModificacion() {
        Rol r = new Rol();
        r.setNombre("ROLE_A");
        dao.save(r);

        r.setNombre("ROLE_B");
        Rol updated = dao.save(r);

        Rol found = dao.findById(updated.getId()).orElse(null);
        assertEquals("ROLE_B", found.getNombre());
    }

    @Test
    void testBaja() {
        Rol r = new Rol();
        r.setNombre("ROLE_DEL");
        dao.save(r);
        Long id = r.getId();

        dao.deleteById(id);

        Rol deleted = dao.findById(id).orElse(null);
        assertNull(deleted);
    }
}
