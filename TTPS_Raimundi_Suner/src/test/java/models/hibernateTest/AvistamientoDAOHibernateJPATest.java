package models.hibernateTest;

import models.clases.Avistamiento;
import org.junit.jupiter.api.*;
import models.hibernate.AvistamientoDAOHibernateJPA;

import static org.junit.jupiter.api.Assertions.*;

class AvistamientoDAOHibernateJPATest {

    private AvistamientoDAOHibernateJPA dao;

    @BeforeEach
    void setup() {
        dao = new AvistamientoDAOHibernateJPA();
    }

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
