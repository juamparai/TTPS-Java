package models.hibernateTest;

import APP.models.clases.Badge;
import org.junit.jupiter.api.*;
import APP.models.dao.BadgeDAO;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import config.TestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BadgeDAOHibernateJPATest {

    @Autowired
    private BadgeDAO dao;

    @Test
    void testAlta() {
        Badge b = new Badge();
        b.setNombre("Helper");
        b.setDescripcion("Badge de ayuda");

        Badge saved = dao.save(b);

        Badge found = dao.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Helper", found.getNombre());
    }

    @Test
    void testModificacion() {
        Badge b = new Badge();
        b.setNombre("Member");
        dao.save(b);

        b.setNombre("MemberPlus");
        Badge updated = dao.save(b);

        Badge found = dao.findById(updated.getId()).orElse(null);
        assertEquals("MemberPlus", found.getNombre());
    }

    @Test
    void testBaja() {
        Badge b = new Badge();
        b.setNombre("ToDelete");
        dao.save(b);
        Long id = b.getId();

        dao.deleteById(id);

        Badge deleted = dao.findById(id).orElse(null);
        assertNull(deleted);
    }
}
