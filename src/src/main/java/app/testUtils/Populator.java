package app.testUtils;

import app.entities.Question;
import app.entities.Subject;
import app.security.entities.Role;
import app.security.entities.User;
import app.entities.User_question;
import app.security.entities.User_role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.*;

public class Populator {
    public static Map<String, User > userPopulate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            User user1 = new User("Rmil@gmail.com","1234",0);
            User user2 = new User("Emil@gmail.com","1234",1);
            User user3 = new User("3mil@gmail.com","1234",2);


            try {
                em.createNativeQuery("TRUNCATE TABLE a_user RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(user1);
                em.persist(user2);
                em.persist(user3);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, User> seeded = new LinkedHashMap<>();
            seeded.put("user1", user1);
            seeded.put("user2", user2);
            seeded.put("user3", user3);
            return seeded;
        }
    }



    public static Map<String, Subject> subjectPopulate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            Subject subject1 = new Subject( "History", 10 );
            Subject subject2 = new Subject( "Mathematics", 15 );
            Subject subject3 = new Subject( "Music", 20 );


            try {
                em.createNativeQuery("TRUNCATE TABLE subject RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(subject1);
                em.persist(subject2);
                em.persist(subject3);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, Subject> seeded = new LinkedHashMap<>();
            seeded.put("subject1", subject1);
            seeded.put("subject2", subject2);
            seeded.put("subject3", subject3);
            return seeded;
        }
    }
    public static Map<String, Question> questionPopulate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            Subject subject = new Subject( "History", 10 );

            em.getTransaction().begin();

            Set<User_question> userQuestionList = new HashSet<>();
            Question question1 = new Question( "Who was the first king of Denmark?", subject);
            Question question2 = new Question( "What is the capital of Denmark?", subject);
            Question question3 = new Question( "When did Denmark become a kingdom?", subject);

            try {
                em.createNativeQuery("TRUNCATE TABLE question RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(question1);
                em.persist(question2);
                em.persist(question3);
                em.persist(subject);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, Question> seeded = new LinkedHashMap<>();
            seeded.put("question1", question1);
            seeded.put("question2", question2);
            seeded.put("question3", question3);
            return seeded;
        }
    }
    public static Map<String, User_question> userQuestionPopulate(
            EntityManagerFactory emf) {

        Subject subject = new Subject("History", 10);

        User user1 = new User("Rmil@gmail.com", "1234", 0);
        User user2 = new User("Emil@gmail.com", "1234", 1);
        User user3 = new User("3mil@gmail.com", "1234", 2);

        Question question1 = new Question(
                "Who was the first king of Denmark?",
                subject
        );

        Question question2 = new Question(
                "What is the capital of Denmark?",
                subject
        );

        Question question3 = new Question(
                "When did Denmark become a kingdom?",
                subject
        );

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            try {
                em.createNativeQuery(
                        "TRUNCATE TABLE user_question RESTART IDENTITY CASCADE"
                ).executeUpdate();

                em.persist(subject);

                em.persist(user1);
                em.persist(user2);
                em.persist(user3);

                em.persist(question1);
                em.persist(question2);
                em.persist(question3);

                // Now create the relationships
                User_question userQuestion1 =
                        new User_question(user1, question1, false);

                User_question userQuestion2 =
                        new User_question(user2, question2, false);

                User_question userQuestion3 =
                        new User_question(user1, question3, true);

                em.persist(userQuestion1);
                em.persist(userQuestion2);
                em.persist(userQuestion3);

                em.flush();

                em.getTransaction().commit();

                Map<String, User_question> seeded = new LinkedHashMap<>();
                seeded.put("userQuestion1", userQuestion1);
                seeded.put("userQuestion2", userQuestion2);
                seeded.put("userQuestion3", userQuestion3);

                return seeded;

            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }
    public static Map<String, Role> rolePopulate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            Role role1 = new Role("ADMIN");
            Role role2 = new Role("USER");
            Role role3 = new Role("MODERATOR");

            try {
                em.createNativeQuery(
                        "TRUNCATE TABLE roles RESTART IDENTITY CASCADE"
                ).executeUpdate();

                em.persist(role1);
                em.persist(role2);
                em.persist(role3);

                em.flush();

            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }

            em.getTransaction().commit();

            Map<String, Role> seeded = new LinkedHashMap<>();
            seeded.put("role1", role1);
            seeded.put("role2", role2);
            seeded.put("role3", role3);

            return seeded;
        }
    }
    public static Map<String, User_role> userRolePopulate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            try {
                em.createNativeQuery(
                        "TRUNCATE TABLE user_role RESTART IDENTITY CASCADE"
                ).executeUpdate();

                em.createNativeQuery(
                        "TRUNCATE TABLE a_user RESTART IDENTITY CASCADE"
                ).executeUpdate();

                em.createNativeQuery(
                        "TRUNCATE TABLE roles RESTART IDENTITY CASCADE"
                ).executeUpdate();

                User user1 = new User("Rmil@gmail.com", "1234", 0);
                User user2 = new User("Emil@gmail.com", "1234", 1);
                User user3 = new User("3mil@gmail.com", "1234", 2);

                Role role1 = new Role("ADMIN");
                Role role2 = new Role("USER");
                Role role3 = new Role("MODERATOR");

                em.persist(user1);
                em.persist(user2);
                em.persist(user3);

                em.persist(role1);
                em.persist(role2);
                em.persist(role3);

                em.flush();

                User_role userRole1 = new User_role(user1, role1);
                User_role userRole2 = new User_role(user2, role2);
                User_role userRole3 = new User_role(user3, role3);

                em.persist(userRole1);
                em.persist(userRole2);
                em.persist(userRole3);

                em.flush();
                em.getTransaction().commit();

                Map<String, User_role> seeded = new LinkedHashMap<>();
                seeded.put("userRole1", userRole1);
                seeded.put("userRole2", userRole2);
                seeded.put("userRole3", userRole3);

                return seeded;

            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }
}
