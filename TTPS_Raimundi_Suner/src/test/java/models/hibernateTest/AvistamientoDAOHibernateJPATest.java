package models.hibernateTest;

import APP.models.clases.Avistamiento;
import org.junit.jupiter.api.*;
import APP.models.dao.AvistamientoDAO;

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
class AvistamientoDAOHibernateJPATest {

    @Autowired
    private AvistamientoDAO dao;

    @Test
    void testAlta() {
        Avistamiento a = new Avistamiento();
        a.setComentario("Primer avistamiento");

        Avistamiento saved = dao.save(a);

        Avistamiento found = dao.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Primer avistamiento", found.getComentario());
    }

    @Test
    void testModificacion() {
        Avistamiento a = new Avistamiento();
        a.setComentario("Comentario inicial");

        dao.save(a);

        a.setComentario("Comentario modificado");
        Avistamiento updated = dao.save(a);

        Avistamiento found = dao.findById(updated.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Comentario modificado", found.getComentario());
    }

    @Test
    void testBaja() {
        Avistamiento a = new Avistamiento();
        a.setComentario("Para borrar");

        dao.save(a);
        Long id = a.getId();

        dao.deleteById(id);

        Avistamiento deleted = dao.findById(id).orElse(null);
        assertNull(deleted);
    }
}
