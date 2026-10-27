package app.routes;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.config.HibernateTestConfig;
import app.persistence.SubjectDAO;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import app.entities.Subject;
import app.testUtils.Populator;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubjectRouteTest {
    private static final EntityManagerFactory emf =
        HibernateTestConfig.getEntityManagerFactory();

    private static Javalin app = ApplicationConfig.startServer(7070, emf);

    private static SubjectDAO subjectDAO;

    private Map<String, Subject> subjectMap;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI ="http://localhost:7070";
        subjectMap = Populator.subjectPopulate(emf);
        subjectDAO = new SubjectDAO(emf);
    }


    @AfterAll
    static void shutDown() {
        ApplicationConfig.stopServer(app);
    }

    @Test
    void getAllSubjects() {

        List<Subject> expected =
            subjectDAO.readAll();

        List<Subject> actual =
            given()
                .when()
                .get("/subjects")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("name",
                    containsInAnyOrder(
                        "History",
                        "Mathematics",
                        "Music"
                    ))
                .extract()
                .jsonPath()
                .getList("", Subject.class);

        assertEquals(expected.size(), actual.size());
    }

    @Test
    void getSubjectById() {

        Subject subject =
            subjectMap.get("subject1");

        given()
            .when()
            .get("/subjects/" + subject.getId())
            .then()
            .statusCode(200)
            .body("name", is("History"))
            .body("numberOfQuestions", is(10));
    }

    @Test
    void getSubjectByIdNotFound() {

        given()
            .when()
            .get("/subjects/9999")
            .then()
            .statusCode(404);
    }

    @Test
    void createSubject() {

        given()
            .contentType("application/json")
            .body("""
                        {
                            "name": "Programming",
                            "numberOfQuestions": 15
                        }
                        """)
            .when()
            .post("/subjects")
            .then()
            .statusCode(201)
            .body("name", is("Programming"))
            .body("numberOfQuestions", is(15));
    }

    @Test
    void updateSubject() {

        Subject subject =
            subjectMap.get("subject1");

        given()
            .contentType("application/json")
            .body("""
                        {
                            "name": "World History",
                            "numberOfQuestions": 20
                        }
                        """)
            .when()
            .put("/subjects/" + subject.getId())
            .then()
            .statusCode(200)
            .body("name", is("World History"))
            .body("numberOfQuestions", is(20));
    }

    @Test
    void updateSubjectNotFound() {

        given()
            .contentType("application/json")
            .body("""
                        {
                            "name": "Programming",
                            "numberOfQuestions": 10
                        }
                        """)
            .when()
            .put("/subjects/9999")
            .then()
            .statusCode(404);
    }

    @Test
    void deleteSubject() {

        given()
            .contentType("application/json")
            .body("""
                        {
                            "name": "Programming",
                            "numberOfQuestions": 15
                        }
                        """)
            .when()
            .post("/subjects")
            .then()
            .statusCode(201)
            .body("name", is("Programming"))
            .body("numberOfQuestions", is(15));

        Subject subject =
            subjectMap.get("subject1");

        given()
            .when()
            .delete("/subjects/" + subject.getId())
            .then()
            .statusCode(204);
    }

    @Test
    void deleteSubjectNotFound() {


        given()
            .when()
            .delete("/subjects/9999")
            .then()
            .statusCode(404);
    }
}