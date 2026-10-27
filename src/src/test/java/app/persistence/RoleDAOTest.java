package app.persistence;

import app.config.HibernateTestConfig;
import app.security.entities.Role;
import app.testUtils.Populator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoleDAOTest {

    private final EntityManagerFactory emf =
        HibernateTestConfig.getEntityManagerFactory();

    private RoleDAO roleDAO;
    private Map<String, Role> seeded;

    @BeforeEach
    void beforeEach() {
        seeded = Populator.rolePopulate(emf);
        roleDAO = new RoleDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        Role role = new Role("NEW_ROLE");

        Role created = roleDAO.create(role);

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("NEW_ROLE", created.getRole());
    }

    @Test
    void read() {
        Role expected = seeded.get("role1");

        Role actual = roleDAO.read(expected.getId());

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRole(), actual.getRole());
    }

    @Test
    void readAll() {
        var roles = roleDAO.readAll();

        assertNotNull(roles);
        assertEquals(3, roles.size());
    }

    @Test
    void update() {
        Role role = seeded.get("role1");

        // Requires a setter for role in Role
        role.setRole("UPDATED_ROLE");

        Role updated = roleDAO.update(role);

        assertNotNull(updated);
        assertEquals("UPDATED_ROLE", updated.getRole());
    }

    @Test
    void delete() {
        Role role = seeded.get("role1");

        roleDAO.delete(role.getId());

        Role deleted = roleDAO.read(role.getId());

        assertNull(deleted);
    }
}