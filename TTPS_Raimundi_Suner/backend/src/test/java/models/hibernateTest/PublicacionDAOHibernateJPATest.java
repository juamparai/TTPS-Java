package models.hibernateTest;

import APP.models.clases.Publicacion;
import org.junit.jupiter.api.*;
import APP.models.dao.PublicacionDAO;

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
class PublicacionDAOHibernateJPATest {

    @Autowired
    private PublicacionDAO dao;

    @Test
    void testAlta() {
        Publicacion p = new Publicacion();
        p.setDescripcion("Nueva publicacion de prueba");

        Publicacion saved = dao.save(p);

        Publicacion found = dao.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Nueva publicacion de prueba", found.getDescripcion());
    }

    @Test
    void testModificacion() {
        Publicacion p = new Publicacion();
        p.setDescripcion("Desc inicial");
        dao.save(p);

        p.setDescripcion("Desc modificada");
        Publicacion updated = dao.save(p);

        Publicacion found = dao.findById(updated.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Desc modificada", found.getDescripcion());
    }

    @Test
    void testBaja() {
        Publicacion p = new Publicacion();
        p.setDescripcion("Para borrar");
        dao.save(p);
        Long id = p.getId();

        dao.deleteById(id);

        Publicacion deleted = dao.findById(id).orElse(null);
        assertNull(deleted);
    }
}
