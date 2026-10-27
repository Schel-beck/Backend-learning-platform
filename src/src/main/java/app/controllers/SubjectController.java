package app.controllers;

import app.config.HibernateConfig;
import app.dto.SubjectDTO;
import app.entities.Subject;
import app.persistence.SubjectDAO;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller responsible for handling HTTP requests related to subjects.
 * Provides functionality for creating, reading, updating and deleting subjects,
 * as well as validating subject data and primary keys.
 */
public class SubjectController implements IController<SubjectDTO, Integer> {

    EntityManagerFactory emf;
    private SubjectDAO dao;
    private static final Logger log = LoggerFactory.getLogger(SubjectController.class);

    /**
     * Retrieves a subject by its ID and returns it as JSON.
     *
     * @param ctx the Javalin context containing the request and path parameter
     */
    @Override
    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        Subject subject = dao.read(id);

        if (subject == null) {
            log.info("Subject not found with id: {}", id);
            ctx.status(404).result("Subject not found");
            return;
        }

        ctx.json(subject);
    }

    /**
     * Retrieves all subjects and returns them as JSON.
     *
     * @param ctx the Javalin context
     */
    @Override
    public void readAll(Context ctx) {
        List<Subject> subjects = dao.readAll();

        ctx.json(subjects);
    }

    /**
     * Creates a new subject after validating the provided subject data.
     *
     * @param ctx the Javalin context containing the subject data
     */
    @Override
    public void create(Context ctx) {
        SubjectDTO jsonRequest = validateEntity(ctx);

        if (jsonRequest == null) {
            return;
        }

        Subject subject = new Subject(
                jsonRequest.getName(),
                jsonRequest.getNumberOfQuestions()
        );

        Subject createdSubject = dao.create(subject);

        SubjectDTO subjectDTO = new SubjectDTO(
                createdSubject.getId(),
                createdSubject.getName(),
                createdSubject.getNumberOfQuestions()
        );

        ctx.res().setStatus(201);
        ctx.json(subjectDTO, SubjectDTO.class);
    }

    /**
     * Updates an existing subject with the data provided in the request.
     *
     * @param ctx the Javalin context containing the subject ID and updated data
     */
    @Override
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        Subject subject = dao.read(id);

        if (subject == null) {
            log.info("Subject not found with id: {}", id);
            ctx.status(404).result("Subject not found");
            return;
        }

        SubjectDTO subjectDTO = validateEntity(ctx);

        if (subjectDTO == null) {
            return;
        }

        Subject updatedSubject = new Subject(
                id,
                subjectDTO.getName(),
                subjectDTO.getNumberOfQuestions()
        );

        updatedSubject = dao.update(updatedSubject);

        ctx.json(updatedSubject);
    }

    /**
     * Deletes an existing subject by its ID.
     *
     * @param ctx the Javalin context containing the subject ID
     */
    @Override
    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be positive")
                .get();

        Subject subject = dao.read(id);

        if (subject == null) {
            log.warn("Subject not found with id: {}", id);
            ctx.status(404).result("Subject not found");
            return;
        }

        dao.delete(id);
        ctx.status(204);
    }

    /**
     * Validates whether a primary key is valid and belongs to an existing subject.
     *
     * @param integer the subject ID to validate
     * @return true if the ID is valid and the subject exists, otherwise false
     */
    @Override
    public boolean validatePrimaryKey(Integer integer) {
        if (integer == null || integer <= 0) {
            return false;
        }

        return dao.read(integer) != null;
    }

    /**
     * Validates the subject data received in the request body.
     *
     * @param ctx the Javalin context containing the request body
     * @return a validated SubjectDTO, or null if the subject data is invalid
     */
    @Override
    public SubjectDTO validateEntity(Context ctx) {
        SubjectDTO dto = ctx.bodyAsClass(SubjectDTO.class);

        if (dto.getName() == null || dto.getName().isBlank()) {
            log.warn("Subject name is required");
            ctx.status(400).result("Subject name is required");
            return null;
        }

        if (dto.getNumberOfQuestions() <= 0) {
            log.warn("Number of questions has to be larger than 0");
            ctx.status(400).result("Number of questions has to be larger than 0");
            return null;
        }

        return dto;
    }

    public SubjectController(EntityManagerFactory emf) {
        this.emf = emf;
        this.dao = new SubjectDAO(emf);
    }
}