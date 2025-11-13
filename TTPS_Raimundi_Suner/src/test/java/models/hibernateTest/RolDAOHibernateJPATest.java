package models.hibernateTest;

import models.clases.Rol;
import org.junit.jupiter.api.*;
import models.DAO.RolDAO;

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
    private RolDAO<Rol> dao;

    @Test
    void testAlta() {
        Rol r = new Rol();
        r.setNombre("TEST_ROLE");

        Rol saved = dao.persist(r);

        Rol found = dao.get(saved.getId());
        assertNotNull(found);
        assertEquals("TEST_ROLE", found.getNombre());
    }

    @Test
    void testModificacion() {
        Rol r = new Rol();
        r.setNombre("ROLE_A");
        dao.persist(r);

        r.setNombre("ROLE_B");
        Rol updated = dao.update(r);

        Rol found = dao.get(updated.getId());
        assertEquals("ROLE_B", found.getNombre());
    }

    @Test
    void testBaja() {
        Rol r = new Rol();
        r.setNombre("ROLE_DEL");
        dao.persist(r);
        Long id = r.getId();

        dao.delete(id);

        Rol deleted = dao.get(id);
        assertNull(deleted);
    }
}
