package app.persistence;

import app.config.HibernateTestConfig;
import app.entities.Subject;
import app.testUtils.Populator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubjectDAOTest {

    private final EntityManagerFactory emf =
        HibernateTestConfig.getEntityManagerFactory();

    private SubjectDAO subjectDAO;
    private Map<String, Subject> seeded;

    @BeforeEach
    void beforeEach() {
        seeded = Populator.subjectPopulate(emf);
        subjectDAO = new SubjectDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        Subject subject = new Subject("New Subject",2);

        Subject created = subjectDAO.create(subject);

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("New Subject", created.getName());
    }

    @Test
    void read() {
        Subject expected = seeded.get("subject1");

        Subject actual = subjectDAO.read(expected.getId());

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
    }

    @Test
    void readAll() {
        var subjects = subjectDAO.readAll();

        assertNotNull(subjects);
        assertEquals(3, subjects.size());
    }

    @Test
    void update() {
        Subject subject = seeded.get("subject1");

        // This requires a setter for name in Subject
        subject.setName("Updated Subject");

        Subject updated = subjectDAO.update(subject);

        assertNotNull(updated);
        assertEquals("Updated Subject", updated.getName());
    }

    @Test
    void delete() {
        Subject subject = seeded.get("subject1");

        subjectDAO.delete(subject.getId());

        Subject deleted = subjectDAO.read(subject.getId());

        assertNull(deleted);
    }
}