package app;

import app.config.HibernateConfig;
import app.controllers.InteractiveChatController;
import app.controllers.NotificationController;
import app.controllers.QuestionController;
import app.controllers.SubjectController;
import app.routes.*;
import app.security.routes.SecurityRoutes;
import app.service.Importer;
import app.service.InteractiveChat;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        SubjectController subjectController = new SubjectController(emf);
        QuestionController questionController = new QuestionController(emf);
        SubjectRoute subjectRoute = new SubjectRoute(subjectController);
        SecurityRoutes securityRoutes = new SecurityRoutes();
        QuestionRoute questionRoute = new QuestionRoute(questionController);
        InteractiveChatRoute interactiveChatRoute = new InteractiveChatRoute();
        User_questionRoutes userQuestionRoutes = new User_questionRoutes();
        NotificationRoute notificationRoute = new NotificationRoute(new NotificationController());
        Routes routes = new Routes(
                subjectRoute,
                questionRoute,
                securityRoutes,
                interactiveChatRoute,
                userQuestionRoutes,
                notificationRoute
        );
        //Importer importer = new Importer();
        //importer.importIntoDatabase();


        Javalin app = Javalin.create(config -> {
            config.routes.apiBuilder(routes.getRoutes());
        }).start(7070);
    }
}