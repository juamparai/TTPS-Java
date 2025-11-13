package models.hibernateTest;

import models.clases.Usuario;
import org.junit.jupiter.api.*;
import models.DAO.UsuarioDAO;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import config.TestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class UsuarioDAOHibernateJPATest {

    @Autowired
    private UsuarioDAO<Usuario> dao;

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

        dao.delete(id);

        Usuario deleted = dao.get(id);
        assertNull(deleted);
    }
}