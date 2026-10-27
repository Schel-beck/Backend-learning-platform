package app.routes;

import app.controllers.QuestionController;
import io.javalin.apibuilder.EndpointGroup;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
@NoArgsConstructor
public class QuestionRoute {
    private QuestionController questionController;

    protected EndpointGroup getRoutes() {

        return () -> {
            post("/subjects/{id}/questions", questionController::create);
            get("/questions", questionController::readAll);
            get("/questions/{id}", questionController::read);
            put("/questions/{id}", questionController::update);
            delete("/questions/{id}", questionController::delete);
        };
    }

    public QuestionRoute(QuestionController questionController) {
        this.questionController = questionController;
    }
}
