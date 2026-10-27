package app.persistence;
import app.testUtils.Populator;
import app.config.HibernateTestConfig;
import app.entities.Subject;
import app.entities.Question;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionDAOTest {
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private QuestionDAO questionDAO;
    private Map<String, Question> seeded;

    @BeforeEach
    void beforeEach() {
        Map<String, Subject> subjects = Populator.subjectPopulate(emf);
        Subject subject = subjects.get("subject1");
        seeded = Populator.questionPopulate(emf);
        questionDAO = new QuestionDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        Question question = new Question("What is the capital of Denmark?",
            seeded.get("question1").getSubject()
            );
        Question created = questionDAO.create(question);

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(
            "What is the capital of Denmark?",
            created.getName()
        );
    }

    @Test
    void read() {
        Question expected = seeded.get("question1");

        Question actual = questionDAO.read(expected.getId());

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
    }

    @Test
    void readAll() {
        var questions = questionDAO.readAll();

        assertNotNull(questions);
        assertEquals(3, questions.size());
    }

    @Test
    void update() {
        Question question = seeded.get("question1");

        // This requires a setter for name in Question
        question.setName("Updated question");

        Question updated = questionDAO.update(question);

        assertNotNull(updated);
        assertEquals("Updated question", updated.getName());
    }

    @Test
    void delete() {
        Question question = seeded.get("question1");

        questionDAO.delete(question.getId());

        Question deleted = questionDAO.read(question.getId());

        assertNull(deleted);
    }
    /*
    @Test
    void readAllFromOneSubject() {
        List<Question> questions = questionDAO.readAllFromOneSubject(1);

        assertNotNull(questions);
        assertEquals(3,questions);
    }

     */
}