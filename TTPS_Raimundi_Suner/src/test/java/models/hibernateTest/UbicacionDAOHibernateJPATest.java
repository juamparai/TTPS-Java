package models.hibernateTest;

import models.clases.Ubicacion;
import org.junit.jupiter.api.*;
import models.hibernate.UbicacionDAOHibernateJPA;

import static org.junit.jupiter.api.Assertions.*;

class UbicacionDAOHibernateJPATest {

    private UbicacionDAOHibernateJPA dao;

    @BeforeEach
    void setup() {
        dao = new UbicacionDAOHibernateJPA();
    }

    @Test
    void testAlta() {
        Ubicacion u = new Ubicacion();
        u.setLat( -34.6037 );
        u.setLng( -58.3816 );
        u.setBarrio("Centro");

        Ubicacion saved = dao.persist(u);

        Ubicacion found = dao.get(saved.getId());
        assertNotNull(found);
        assertEquals("Centro", found.getBarrio());
    }

    @Test
    void testModificacion() {
        Ubicacion u = new Ubicacion();
        u.setLat(0.0);
        u.setLng(0.0);
        u.setBarrio("Old");
        dao.persist(u);

        u.setBarrio("NewBarrio");
        Ubicacion updated = dao.update(u);

        Ubicacion found = dao.get(updated.getId());
        assertEquals("NewBarrio", found.getBarrio());
    }

    @Test
    void testBaja() {
        Ubicacion u = new Ubicacion();
        u.setLat(1.0);
        u.setLng(1.0);
        dao.persist(u);
        Long id = u.getId();

        dao.delete(id.longValue());

        Ubicacion deleted = dao.get(id.longValue());
        assertNull(deleted);
    }
}
