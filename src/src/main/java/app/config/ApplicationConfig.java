package app.config;
import app.controllers.NotificationController;
import app.controllers.QuestionController;
import app.controllers.SubjectController;
import app.persistence.QuestionDAO;
import app.routes.*;
import app.security.routes.SecurityRoutes;
import io.javalin.Javalin;

import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig {

    public static Javalin startServer(int port,EntityManagerFactory emf) {
        QuestionDAO questionDAO = new QuestionDAO(emf);


        QuestionController questionController = new QuestionController(emf);
        SubjectController subjectController = new SubjectController(emf);
        SecurityRoutes securityRoutes = new SecurityRoutes();
        NotificationController notificationController = new NotificationController();


        Routes routes = new Routes(
                new SubjectRoute(subjectController),
                new QuestionRoute(questionController),
                new SecurityRoutes(),
                new InteractiveChatRoute(),
                new User_questionRoutes(),
                new NotificationRoute(notificationController));

        Javalin app = Javalin.create(config -> {
            config.routes.apiBuilder(routes.getRoutes());
            config.routes.apiBuilder(securityRoutes.getSecurityRoutes());
            config.bundledPlugins.enableRouteOverview("/routes");
        });


        return app.start(port);
    }

    public static void stopServer(Javalin app) {
        app.stop();
    }
}