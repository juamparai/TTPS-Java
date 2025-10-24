package models.hibernateTest;

import models.clases.Usuario;
import org.junit.jupiter.api.*;
import models.hibernate.UsuarioDAOHibernateJPA;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class UsuarioDAOHibernateJPATest {

    private UsuarioDAOHibernateJPA dao;

    @BeforeEach
    void setup() {
        dao = new UsuarioDAOHibernateJPA();
        // Limpiar la tabla de usuarios para que las pruebas sean deterministas
        List<Usuario> existentes = dao.getAll("id");
        if (existentes != null) {
            for (Usuario u : existentes) {
                try {
                    dao.delete(u.getId());
                } catch (Exception ignored) {
                }
            }
        }
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

    @Test
    void testGetByPuntos() {
        Usuario u1 = new Usuario();
        u1.setNombre("User1");
        u1.setPuntos(15);
        dao.persist(u1);

        Usuario u2 = new Usuario();
        u2.setNombre("User2");
        u2.setPuntos(20);
        dao.persist(u2);

        List<Usuario> usuarios = dao.getByPuntos();
        assertEquals(2, usuarios.size());
        assertEquals("User2", usuarios.get(0).getNombre());
        assertEquals("User1", usuarios.get(1).getNombre());
    }
}