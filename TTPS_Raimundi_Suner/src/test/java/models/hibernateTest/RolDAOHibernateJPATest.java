package models.hibernateTest;

import models.clases.Rol;
import org.junit.jupiter.api.*;
import models.hibernate.RolDAOHibernateJPA;

import static org.junit.jupiter.api.Assertions.*;

class RolDAOHibernateJPATest {

    private RolDAOHibernateJPA dao;

    @BeforeEach
    void setup() {
        dao = new RolDAOHibernateJPA();
    }

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

        dao.delete(id.longValue());

        Rol deleted = dao.get(id.longValue());
        assertNull(deleted);
    }
}
