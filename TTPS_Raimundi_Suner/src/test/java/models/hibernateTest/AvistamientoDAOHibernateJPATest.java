package models.hibernateTest;

import models.clases.Avistamiento;
import org.junit.jupiter.api.*;
import models.DAO.AvistamientoDAO;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import config.TestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class AvistamientoDAOHibernateJPATest {

    @Autowired
    private AvistamientoDAO<Avistamiento> dao;

    @Test
    void testAlta() {
        Avistamiento a = new Avistamiento();
        a.setComentario("Primer avistamiento");

        Avistamiento saved = dao.persist(a);

        Avistamiento found = dao.get(saved.getId());
        assertNotNull(found);
        assertEquals("Primer avistamiento", found.getComentario());
    }

    @Test
    void testModificacion() {
        Avistamiento a = new Avistamiento();
        a.setComentario("Comentario inicial");

        dao.persist(a);

        a.setComentario("Comentario modificado");
        Avistamiento updated = dao.update(a);

        Avistamiento found = dao.get(updated.getId());
        assertEquals("Comentario modificado", found.getComentario());
    }

    @Test
    void testBaja() {
        Avistamiento a = new Avistamiento();
        a.setComentario("Para borrar");

        dao.persist(a);
        Long id = a.getId();

        dao.delete(id);

        Avistamiento deleted = dao.get(id);
        assertNull(deleted);
    }
}
