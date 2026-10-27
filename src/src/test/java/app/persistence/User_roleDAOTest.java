package app.persistence;

import app.config.HibernateTestConfig;
import app.security.entities.User_role;
import app.testUtils.Populator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class User_roleDAOTest {

    private final EntityManagerFactory emf =
        HibernateTestConfig.getEntityManagerFactory();

    private User_roleDAO userRoleDAO;
    private Map<String, User_role> seeded;

    @BeforeEach
    void beforeEach() {
        seeded = Populator.userRolePopulate(emf);
        userRoleDAO = new User_roleDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        User_role existing = seeded.get("userRole1");

        User_role userRole = new User_role(
            existing.getUser(),
            existing.getRole()
        );

        User_role created = userRoleDAO.create(userRole);

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(existing.getUser(), created.getUser());
        assertEquals(existing.getRole(), created.getRole());
    }

    @Test
    void read() {
        User_role expected = seeded.get("userRole1");

        User_role actual =
            userRoleDAO.read(expected.getId());

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUser(), actual.getUser());
        assertEquals(expected.getRole(), actual.getRole());
    }

    @Test
    void readAll() {
        var userRoles = userRoleDAO.readAll();

        assertNotNull(userRoles);
        assertEquals(3, userRoles.size());
    }

    @Test
    void update() {
        User_role userRole = seeded.get("userRole3");

        // Assumes userRole3 is associated with the expected role.
        User_role updated = userRoleDAO.update(userRole);

        assertNotNull(updated);
        assertEquals(userRole.getId(), updated.getId());
        assertNotNull(updated.getUser());
        assertNotNull(updated.getRole());
    }

    @Test
    void delete() {
        User_role userRole = seeded.get("userRole1");

        userRoleDAO.delete(userRole.getId());

        User_role deleted =
            userRoleDAO.read(userRole.getId());

        assertNull(deleted);
    }
}