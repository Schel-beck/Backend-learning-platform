package app.routes;

import app.config.HibernateConfig;
import app.config.HibernateTestConfig;
import app.controllers.SubjectController;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
@NoArgsConstructor
public class SubjectRoute {
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private SubjectController subjectController = new SubjectController(emf);

    public EndpointGroup getRoutes() {

        return () -> {
            post("/subjects", subjectController::create);
            get("/subjects", subjectController::readAll);
            get("/subjects/{id}", subjectController::read);
            put("/subjects/{id}", subjectController::update);
            delete("/subjects/{id}", subjectController::delete);
        };
    }

    public SubjectRoute(SubjectController subjectController) {
        this.subjectController = subjectController;
    }
}
