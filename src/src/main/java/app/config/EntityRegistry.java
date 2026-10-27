package app.config;
import app.entities.*;
import app.security.entities.Role;
import app.security.entities.User;
import app.security.entities.User_role;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Subject.class);
        configuration.addAnnotatedClass(Question.class);
        configuration.addAnnotatedClass(User_question.class);
        configuration.addAnnotatedClass(Role.class);
        configuration.addAnnotatedClass(User_role.class);
        // TODO: Add more entities here...
    }
}