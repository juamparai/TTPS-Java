package models.hibernateTest;

import APP.models.clases.Usuario;
import org.junit.jupiter.api.*;
import APP.models.dao.UsuarioDAO;

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
    private UsuarioDAO dao;

    @Test
    void testAlta() {
        Usuario u = new Usuario();
        u.setNombre("UsuarioTest");
        u.setPuntos(0);

        Usuario saved = dao.save(u);

        Usuario found = dao.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("UsuarioTest", found.getNombre());
    }

    @Test
    void testModificacion() {
        Usuario u = new Usuario();
        u.setNombre("UserA");
        u.setPuntos(5);
        dao.save(u);

        u.setNombre("UserB");
        Usuario updated = dao.save(u);

        Usuario found = dao.findById(updated.getId()).orElse(null);
        assertEquals("UserB", found.getNombre());
    }

    @Test
    void testBaja() {
        Usuario u = new Usuario();
        u.setNombre("ToDelete");
        u.setPuntos(1);
        dao.save(u);
        Long id = u.getId();

        dao.deleteById(id);

        Usuario deleted = dao.findById(id).orElse(null);
        assertNull(deleted);
    }
}