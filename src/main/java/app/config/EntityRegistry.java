package app.config;
import app.entities.Course;
import app.entities.Person;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(Person.class);
        configuration.addAnnotatedClass(Course.class);
        // TODO: Add more entities here...
    }
}