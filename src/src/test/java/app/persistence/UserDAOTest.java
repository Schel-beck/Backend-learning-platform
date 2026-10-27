package app.persistence;

import app.testUtils.Populator;
import app.config.HibernateTestConfig;
import app.security.entities.User;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private UserDAO userDAO;
    private Map<String, User> seeded;

    @BeforeEach
    void beforeEach() {

        seeded = Populator.userPopulate(emf);
        userDAO = new UserDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }
    @Test
    void create() {
        User user = new User(
            "test@email.com",
            "test1234",0
        );

        User created = userDAO.create(user);

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("test@email.com", created.getEmail());
        assertEquals("test1234", created.getPassWord());
    }

    @Test
    void read() {
        User expected = seeded.get("user1");

        User actual = userDAO.read(expected.getId());

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getEmail(), actual.getEmail());
    }

    @Test
    void readAll() {
        List<User> users = userDAO.readAll();

        assertNotNull(users);
        assertEquals(3, users.size());
    }

    @Test
    void update() {
        User user = seeded.get("user1");

        user.setEmail("updatedUser");

        User updated = userDAO.update(user);

        assertNotNull(updated);
        assertEquals("updatedUser", updated.getEmail());

        User found = userDAO.read(user.getId());
        assertEquals("updatedUser", found.getEmail());
    }

    @Test
    void delete() {
        User user = seeded.get("user1");

        userDAO.delete(user.getId());

        User deleted = userDAO.read(user.getId());

        assertNull(deleted);
    }
}