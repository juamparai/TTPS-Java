package models.hibernateTest;

import models.clases.Badge;
import org.junit.jupiter.api.*;
import models.DAO.BadgeDAO;

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
    private BadgeDAO<Badge> dao;

    @Test
    void testAlta() {
        Badge b = new Badge();
        b.setNombre("Helper");
        b.setDescripcion("Badge de ayuda");

        Badge saved = dao.persist(b);

        Badge found = dao.get(saved.getId());
        assertNotNull(found);
        assertEquals("Helper", found.getNombre());
    }

    @Test
    void testModificacion() {
        Badge b = new Badge();
        b.setNombre("Member");
        dao.persist(b);

        b.setNombre("MemberPlus");
        Badge updated = dao.update(b);

        Badge found = dao.get(updated.getId());
        assertEquals("MemberPlus", found.getNombre());
    }

    @Test
    void testBaja() {
        Badge b = new Badge();
        b.setNombre("ToDelete");
        dao.persist(b);
        Long id = b.getId();

        dao.delete(id);

        Badge deleted = dao.get(id);
        assertNull(deleted);
    }
}
