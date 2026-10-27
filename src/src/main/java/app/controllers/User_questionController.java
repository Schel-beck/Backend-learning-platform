package app.controllers;

import app.entities.Question;
import app.entities.User_question;
import app.persistence.QuestionDAO;
import app.persistence.UserDAO;
import app.persistence.User_questionDAO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller responsible for handling HTTP requests related to user questions.
 * Provides functionality for creating, reading, updating and deleting user
 * questions, as well as validating request data and primary keys.
 *
 * A user question represents the relationship between a user and a question
 * and stores whether the question has been answered.
 */
public class User_questionController
        implements IController<User_question, Integer> {

    private final User_questionDAO dao;
    private final UserDAO userDAO;
    private final QuestionDAO questionDAO;

    private static final Logger log =
            LoggerFactory.getLogger(User_questionController.class);

    /**
     * Constructs a User_questionController and initializes its DAOs.
     *
     * @param emf the EntityManagerFactory used for database operations
     */
    public User_questionController(EntityManagerFactory emf) {
        this.dao = new User_questionDAO(emf);
        this.userDAO = new UserDAO(emf);
        this.questionDAO = new QuestionDAO(emf);
    }

    /**
     * Retrieves a user question by its ID and returns it as JSON.
     *
     * @param ctx the Javalin context containing the request and path parameter
     */
    @Override
    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        User_question obj = dao.read(id);

        if (obj == null) {
            log.info("User question not found with id: {}", id);
            ctx.status(404).result("User question not found");
            return;
        }

        ctx.json(toResponse(obj));
    }

    /**
     * Retrieves all user questions and returns them as a JSON list.
     *
     * @param ctx the Javalin context
     */
    @Override
    public void readAll(Context ctx) {
        List<UserQuestionResponse> results = dao.readAll()
                .stream()
                .map(this::toResponse)
                .toList();

        ctx.json(results);
    }

    /**
     * Creates a new user question using the user ID, question ID and
     * answered status supplied in the request body.
     *
     * @param ctx the Javalin context containing the request body
     */
    @Override
    public void create(Context ctx) {
        User_question obj = validateEntity(ctx);

        if (obj == null) {
            return;
        }

        User_question created = dao.create(obj);

        ctx.status(201).json(toResponse(created));
    }

    /**
     * Updates an existing user question with the data supplied in the
     * request body.
     *
     * @param ctx the Javalin context containing the user question ID
     *            and updated data
     */
    @Override
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        User_question existing = dao.read(id);

        if (existing == null) {
            log.info("User question not found with id: {}", id);
            ctx.status(404).result("User question not found");
            return;
        }

        User_question updatedData = validateEntity(ctx);

        if (updatedData == null) {
            return;
        }

        existing.setUser(updatedData.getUser());
        existing.setQuestion(updatedData.getQuestion());
        existing.setAnswered(updatedData.isAnswered());

        User_question updated = dao.update(existing);

        ctx.json(toResponse(updated));
    }

    /**
     * Deletes a user question by its ID.
     *
     * @param ctx the Javalin context containing the user question ID
     */
    @Override
    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        if (!validatePrimaryKey(id)) {
            log.info("User question not found with id: {}", id);
            ctx.status(404).result("User question not found");
            return;
        }

        dao.delete(id);

        ctx.status(204);
    }

    /**
     * Validates whether a primary key is positive and belongs to an
     * existing user question.
     *
     * @param id the user question ID to validate
     * @return true if the ID is valid and the user question exists;
     *         otherwise false
     */
    @Override
    public boolean validatePrimaryKey(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        return dao.read(id) != null;
    }

    /**
     * Validates the user question data received in the request body.
     * Checks that the user ID, question ID and answered status are present
     * and valid, and that the referenced user and question exist.
     *
     * @param ctx the Javalin context containing the request body
     * @return a validated User_question entity, or null if the request
     *         data is invalid or a referenced entity does not exist
     */
    @Override
    public User_question validateEntity(Context ctx) {
        UserQuestionRequest request;

        try {
            request = ctx.bodyAsClass(UserQuestionRequest.class);
        } catch (Exception e) {
            ctx.status(400).result("Invalid request body");
            return null;
        }

        if (request.userId() == null || request.userId() <= 0) {
            ctx.status(400).result("A valid userId is required");
            return null;
        }

        if (request.questionId() == null || request.questionId() <= 0) {
            ctx.status(400).result("A valid questionId is required");
            return null;
        }

        if (request.answered() == null) {
            ctx.status(400).result("The answered field is required");
            return null;
        }

        var user = userDAO.read(request.userId());

        if (user == null) {
            log.info("User not found with id: {}", request.userId());
            ctx.status(404).result("User not found");
            return null;
        }

        Question question = questionDAO.read(request.questionId());

        if (question == null) {
            log.info("Question not found with id: {}", request.questionId());
            ctx.status(404).result("Question not found");
            return null;
        }

        return new User_question(
                user,
                question,
                request.answered()
        );
    }

    /**
     * Converts a User_question entity into a response DTO containing
     * its ID, associated user ID, associated question ID and answered status.
     *
     * @param obj the user question entity to convert
     * @return a UserQuestionResponse containing the relevant data
     */
    private UserQuestionResponse toResponse(User_question obj) {
        return new UserQuestionResponse(
                obj.getId(),
                obj.getUser().getId(),
                obj.getQuestion().getId(),
                obj.isAnswered()
        );
    }

    /**
     * Represents the request body used to create or update a user question.
     *
     * @param userId the ID of the existing user
     * @param questionId the ID of the existing question
     * @param answered whether the question has been answered
     */
    public record UserQuestionRequest(
            Integer userId,
            Integer questionId,
            Boolean answered
    ) {}

    /**
     * Represents the JSON response returned for a user question.
     *
     * @param id the user question ID
     * @param userId the associated user ID
     * @param questionId the associated question ID
     * @param answered whether the question has been answered
     */
    public record UserQuestionResponse(
            int id,
            int userId,
            int questionId,
            boolean answered
    ) {}
}

