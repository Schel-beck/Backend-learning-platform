package app.controllers;

import app.config.HibernateConfig;
import app.dto.QuestionDTO;
import app.entities.Question;
import app.entities.Subject;
import app.persistence.QuestionDAO;
import app.persistence.SubjectDAO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller responsible for handling HTTP requests related to questions.
 * Provides functionality for creating, reading, updating and deleting questions,
 * as well as validating question data and primary keys.
 */
public class QuestionController implements IController<QuestionDTO, Integer> {

    EntityManagerFactory emf;
    private QuestionDAO dao;
    private SubjectDAO subjectDAO;
    private static final Logger log = LoggerFactory.getLogger(QuestionController.class);

    public QuestionController(EntityManagerFactory emf) {
        this.emf = emf;
        this.subjectDAO = new SubjectDAO(emf);
        this.dao = new QuestionDAO(emf);
    }

    /**
     * Retrieves a question by its ID and returns it as JSON.
     *
     * @param ctx the Javalin context containing the request and path parameter
     */
    @Override
    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        Question question = dao.read(id);

        if (question == null) {
            log.info("Question not found with id: {}", id);
            ctx.status(404).result("Question not found");
            return;
        }

        ctx.json(question);
    }

    /**
     * Retrieves all questions and returns them as JSON.
     *
     * @param ctx the Javalin context
     */
    @Override
    public void readAll(Context ctx) {
        List<Question> questions = dao.readAll();

        ctx.json(questions);
    }

    /**
     * Creates a new question and associates it with an existing subject.
     *
     * @param ctx the Javalin context containing the question data and subject ID
     */
    @Override
    public void create(Context ctx) {
        // request
        QuestionDTO jsonRequest = validateEntity(ctx);

        if (jsonRequest == null) {
            return;
        }

        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        Subject subject = subjectDAO.read(id);

        if (subject == null) {
            log.info("Subject not found with id: {}", id);
            ctx.status(404).result("Subject not found");
            return;
        }

        Question question = new Question(jsonRequest.getName(), subject);
        question = dao.create(question);

        QuestionDTO questionDTO = new QuestionDTO(
                question.getId(),
                question.getName()
        );

        // response
        ctx.res().setStatus(201);
        ctx.json(questionDTO, QuestionDTO.class);
    }

    /**
     * Updates an existing question with the data provided in the request.
     *
     * @param ctx the Javalin context containing the question ID and updated data
     */
    @Override
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        Question existingQuestion = dao.read(id);

        if (existingQuestion == null) {
            ctx.status(404).result("Question not found");
            return;
        }

        QuestionDTO dto = validateEntity(ctx);

        if (dto == null) {
            return;
        }

        existingQuestion.setName(dto.getName());

        Question updatedQuestion = dao.update(existingQuestion);

        ctx.json(updatedQuestion);
    }

    /**
     * Deletes an existing question by its ID.
     *
     * @param ctx the Javalin context containing the question ID
     */
    @Override
    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        Question question = dao.read(id);

        if (question == null) {
            ctx.status(404).result("Question not found");
            return;
        }

        dao.delete(id);

        ctx.status(204);
    }

    /**
     * Validates whether a primary key is valid and belongs to an existing question.
     *
     * @param id the question ID to validate
     * @return true if the ID is valid and the question exists, otherwise false
     */
    @Override
    public boolean validatePrimaryKey(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        return dao.read(id) != null;
    }

    /**
     * Validates the question data received in the request body.
     *
     * @param ctx the Javalin context containing the request body
     * @return a validated QuestionDTO, or null if the question data is invalid
     */
    @Override
    public QuestionDTO validateEntity(Context ctx) {
        QuestionDTO dto = ctx.bodyAsClass(QuestionDTO.class);

        if (dto.getName() == null || dto.getName().isBlank()) {
            ctx.status(400).result("Question name is required");
            return null;
        }

        return dto;
    }
}