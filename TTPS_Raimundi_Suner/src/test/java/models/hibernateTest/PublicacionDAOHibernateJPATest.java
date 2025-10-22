package models.hibernateTest;

import models.clases.Publicacion;
import org.junit.jupiter.api.*;
import models.hibernate.PublicacionDAOHibernateJPA;

import static org.junit.jupiter.api.Assertions.*;

class PublicacionDAOHibernateJPATest {

    private PublicacionDAOHibernateJPA dao;

    @BeforeEach
    void setup() {
        dao = new PublicacionDAOHibernateJPA();
    }

    @Test
    void testAlta() {
        Publicacion p = new Publicacion();
        p.setDescripcion("Nueva publicacion de prueba");

        Publicacion saved = dao.persist(p);

        Publicacion found = dao.get(saved.getId());
        assertNotNull(found);
        assertEquals("Nueva publicacion de prueba", found.getDescripcion());
    }

    @Test
    void testModificacion() {
        Publicacion p = new Publicacion();
        p.setDescripcion("Desc inicial");
        dao.persist(p);

        p.setDescripcion("Desc modificada");
        Publicacion updated = dao.update(p);

        Publicacion found = dao.get(updated.getId());
        assertEquals("Desc modificada", found.getDescripcion());
    }

    @Test
    void testBaja() {
        Publicacion p = new Publicacion();
        p.setDescripcion("Para borrar");
        dao.persist(p);
        Long id = p.getId();

        dao.delete(id.longValue());

        Publicacion deleted = dao.get(id.longValue());
        assertNull(deleted);
    }
}
