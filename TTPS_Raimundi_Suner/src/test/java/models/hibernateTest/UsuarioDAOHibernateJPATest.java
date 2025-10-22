package models.hibernateTest;

import models.clases.Usuario;
import org.junit.jupiter.api.*;
import models.hibernate.UsuarioDAOHibernateJPA;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDAOHibernateJPATest {

    private UsuarioDAOHibernateJPA dao;

    @BeforeEach
    void setup() {
        dao = new UsuarioDAOHibernateJPA();
    }

    @Test
    void testAlta() {
        Usuario u = new Usuario();
        u.setNombre("UsuarioTest");
        u.setPuntos(0);

        Usuario saved = dao.persist(u);

        Usuario found = dao.get(saved.getId());
        assertNotNull(found);
        assertEquals("UsuarioTest", found.getNombre());
    }

    @Test
    void testModificacion() {
        Usuario u = new Usuario();
        u.setNombre("UserA");
        u.setPuntos(5);
        dao.persist(u);

        u.setNombre("UserB");
        Usuario updated = dao.update(u);

        Usuario found = dao.get(updated.getId());
        assertEquals("UserB", found.getNombre());
    }

    @Test
    void testBaja() {
        Usuario u = new Usuario();
        u.setNombre("ToDelete");
        u.setPuntos(1);
        dao.persist(u);
        Long id = u.getId();

        dao.delete(id.longValue());

        Usuario deleted = dao.get(id.longValue());
        assertNull(deleted);
    }
}
