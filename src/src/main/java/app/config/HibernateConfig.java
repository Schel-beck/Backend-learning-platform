
package app.config;

import app.utils.Utils;
import jakarta.persistence.EntityManagerFactory;

import java.util.Properties;

public final class HibernateConfig {

    private static volatile EntityManagerFactory emf;

    private HibernateConfig() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (HibernateConfig.class) {
                if (emf == null) {
                    emf = HibernateEmfBuilder.build(buildProps());
                }
            }
        }
        return emf;
    }

    private static Properties buildProps() {
        Properties props = HibernateBaseProperties.createBase();

        if (System.getenv("DEPLOYED") != null) {
            setDeployedProperties(props);
        } else {
            setDevProperties(props);
        }

        return props;
    }

    private static void setDeployedProperties(Properties props) {
        String dbName = System.getenv("DB_NAME");
        String connectionStr = System.getenv("CONNECTION_STR");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (dbName == null || connectionStr == null
                || username == null || password == null) {
            throw new IllegalStateException(
                    "Missing required database environment variables"
            );
        }

        props.setProperty(
                "hibernate.connection.url",
                connectionStr + dbName
        );
        props.setProperty("hibernate.connection.username", username);
        props.setProperty("hibernate.connection.password", password);

        // Avoid recreating the schema on application startup.
        props.setProperty("hibernate.hbm2ddl.auto", "update");
    }

    private static void setDevProperties(Properties props) {
        String dbName = Utils.getPropertyValue("DB_NAME", "config.properties");
        String username = Utils.getPropertyValue("DB_USERNAME", "config.properties");
        String password = Utils.getPropertyValue("DB_PASSWORD", "config.properties");

        props.setProperty(
                "hibernate.connection.url",
                "jdbc:postgresql://localhost:5432/" + dbName
        );
        props.setProperty("hibernate.connection.username", username);
        props.setProperty("hibernate.connection.password", password);
    }
}
