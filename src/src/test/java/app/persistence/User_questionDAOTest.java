package app.persistence;

import app.config.HibernateTestConfig;
import app.entities.User_question;
import app.testUtils.Populator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class User_questionDAOTest {

    private final EntityManagerFactory emf =
        HibernateTestConfig.getEntityManagerFactory();

    private User_questionDAO userQuestionDAO;
    private Map<String, User_question> seeded;

    @BeforeEach
    void beforeEach() {
        seeded = Populator.userQuestionPopulate(emf);
        userQuestionDAO = new User_questionDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        User_question existing = seeded.get("userQuestion1");

        User_question userQuestion = new User_question(
            existing.getUser(),
            existing.getQuestion(),
            false
        );

        User_question created = userQuestionDAO.create(userQuestion);

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(existing.getUser(), created.getUser());
        assertEquals(existing.getQuestion(), created.getQuestion());
        assertFalse(created.isAnswered());
    }

    @Test
    void read() {
        User_question expected = seeded.get("userQuestion1");

        User_question actual =
            userQuestionDAO.read(expected.getId());

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.isAnswered(), actual.isAnswered());
    }

    @Test
    void readAll() {
        var userQuestions = userQuestionDAO.readAll();

        assertNotNull(userQuestions);
        assertEquals(3, userQuestions.size());
    }

    @Test
    void update() {
        User_question userQuestion =
            seeded.get("userQuestion3");

        User_question updated =
            userQuestionDAO.update(userQuestion);

        assertNotNull(updated);
        assertTrue(updated.isAnswered());
    }

    @Test
    void delete() {
        User_question userQuestion =
            seeded.get("userQuestion1");

        userQuestionDAO.delete(userQuestion.getId());

        User_question deleted =
            userQuestionDAO.read(userQuestion.getId());

        assertNull(deleted);
    }

}
